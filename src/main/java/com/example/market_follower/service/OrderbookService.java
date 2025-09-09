package com.example.market_follower.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import com.example.market_follower.model.Member;
import com.example.market_follower.repository.MemberRepository;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import com.example.market_follower.dto.upbit.HoldingDto;
import com.example.market_follower.dto.upbit.TradeHistoryDto;
import com.example.market_follower.dto.upbit.TradeRequestDto;
import com.example.market_follower.dto.upbit.UpbitOrderbookDto;
import com.example.market_follower.model.Holding;
import com.example.market_follower.model.TradeHistory;
import com.example.market_follower.model.Wallet;
import com.example.market_follower.repository.HoldingRepository;
import com.example.market_follower.repository.TradeHistoryRepository;
import com.example.market_follower.repository.WalletRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderbookService {
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final HoldingRepository holdingRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Transactional
    public void requestTrade(
        org.springframework.security.core.userdetails.User user,
        TradeRequestDto tradeRequestDto
    ) {
        // 1. email 추출 (username으로 사용자 이메일이 저장되어 있기 때문)
        String email = user.getUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        // 2. DTO 검증
        if (tradeRequestDto.getPrice() == null || tradeRequestDto.getPrice().doubleValue() <= 0) {
            throw new IllegalArgumentException("Price must be greater than 0");
        }
        if (tradeRequestDto.getSize() == null || tradeRequestDto.getSize().doubleValue() <= 0) {
            throw new IllegalArgumentException("Size must be greater than 0");
        }
        if (tradeRequestDto.getMarket() == null || tradeRequestDto.getMarket().isEmpty()) {
            throw new IllegalArgumentException("Market must be provided");
        }
        if (tradeRequestDto.getSide() == null ) {
            throw new IllegalArgumentException("Side must be provided");
        }

        // 3. 지갑 조회
        Wallet wallet = walletRepository.findByMember(member).orElseThrow(() -> new IllegalStateException("Wallet not found"));

        // 4. BUY 주문인 경우
        if (tradeRequestDto.getSide() == TradeHistory.Side.BUY) {
            BigDecimal requiredAmount = tradeRequestDto.getPrice().multiply(tradeRequestDto.getSize());
            if (wallet.getBalance().compareTo(requiredAmount) < 0) {
                throw new IllegalStateException("Insufficient balance");
            }

            // 5. balance 차감 후 locked로 이동
            wallet.setBalance(wallet.getBalance().subtract(requiredAmount));
            wallet.setLocked(wallet.getLocked().add(requiredAmount));

            // 6. 지갑 업데이트
            walletRepository.save(wallet);

            // 7. trade_history에 주문 내역 저장
            TradeHistory tradeHistory = TradeHistory.builder()
                .member(member)
                .market(tradeRequestDto.getMarket())
                .side(tradeRequestDto.getSide())
                .price(tradeRequestDto.getPrice())
                .size(tradeRequestDto.getSize())
                .status(TradeHistory.TradeStatus.WAITING)
                .build();
            tradeHistoryRepository.save(tradeHistory);
            log.info("Trade requested: {}", tradeHistory);
        }

        // 9. SELL 주문인 경우
        if (tradeRequestDto.getSide() == TradeHistory.Side.SELL) {
            // 10. 보유 여부 확인
            Holding holding = holdingRepository.findByMemberAndMarket(member, tradeRequestDto.getMarket())
                .orElseThrow(() -> new IllegalStateException("No holdings for the specified market"));
            
            // 11. 보유 수량 확인
            if (holding.getSize().compareTo(tradeRequestDto.getSize()) < 0) {
                throw new IllegalStateException("Insufficient holdings to sell");
            }

            // 12. size 차감 후 locked로 이동
            holding.setSize(holding.getSize().subtract(tradeRequestDto.getSize()));
            holding.setLocked(holding.getLocked().add(tradeRequestDto.getSize()));

            // 13. holding 업데이트
            holdingRepository.save(holding);

            // 14. trade_history에 주문 내역 저장
            TradeHistory tradeHistory = TradeHistory.builder()
                .member(member)
                .market(tradeRequestDto.getMarket())
                .side(tradeRequestDto.getSide())
                .price(tradeRequestDto.getPrice())
                .size(tradeRequestDto.getSize())
                .status(TradeHistory.TradeStatus.WAITING)
                .build();
            tradeHistoryRepository.save(tradeHistory);
            log.info("Trade requested: {}", tradeHistory);
        }
    }

    // 5초마다 대기 중인 주문 체결 확인
    @Scheduled(fixedRate = 5000)
    @Transactional
    public void checkOrderExecution() {
        try {
            // WAITING 상태인 주문들 조회
            List<TradeHistory> waitingOrders = tradeHistoryRepository
                .findByStatusOrderByRequestAtAsc(TradeHistory.TradeStatus.WAITING);

            log.debug("Checking {} waiting orders for execution", waitingOrders.size());

            for (TradeHistory order : waitingOrders) {
                try {
                    checkAndExecuteOrder(order);
                } catch (Exception e) {
                    log.error("Failed to check order execution for orderId: {}", order.getId(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to check order executions", e);
        }
    }

    private void checkAndExecuteOrder(TradeHistory order) {
        try {
            // Redis에서 최신 호가 정보 조회
            String redisKey = "upbit:orderbook:" + order.getMarket();
            String orderbookJson = redisTemplate.opsForValue().get(redisKey);
            
            if (orderbookJson == null) {
                log.debug("No orderbook data found for market: {}", order.getMarket());
                return;
            }

            UpbitOrderbookDto orderbook = objectMapper.readValue(orderbookJson, UpbitOrderbookDto.class);
            
            boolean shouldExecute = false;
            BigDecimal executionPrice = order.getPrice();

            // BUY 주문: 매도 호가가 주문 가격보다 낮거나 같으면 체결
            if (order.getSide() == TradeHistory.Side.BUY) {
                if (orderbook.getOrderbookUnits() != null && 
                    !orderbook.getOrderbookUnits().isEmpty()) {
                    
                    BigDecimal askPrice = orderbook.getOrderbookUnits().get(0).getAskPrice();
                    if (askPrice.compareTo(order.getPrice()) <= 0) {
                        shouldExecute = true;
                        executionPrice = askPrice; // 실제 체결가로 업데이트
                    }
                }
            }
            // SELL 주문: 매수 호가가 주문 가격보다 높거나 같으면 체결
            else if (order.getSide() == TradeHistory.Side.SELL) {
                if (orderbook.getOrderbookUnits() != null && 
                    !orderbook.getOrderbookUnits().isEmpty()) {
                    
                    BigDecimal bidPrice = orderbook.getOrderbookUnits().get(0).getBidPrice();
                    if (bidPrice.compareTo(order.getPrice()) >= 0) {
                        shouldExecute = true;
                        executionPrice = bidPrice; // 실제 체결가로 업데이트
                    }
                }
            }

            if (shouldExecute) {
                executeOrder(order, executionPrice);
            }

        } catch (Exception e) {
            log.error("Failed to check execution for order: {}", order.getId(), e);
        }
    }

    @Transactional
    private void executeOrder(TradeHistory order, BigDecimal executionPrice) {
        log.info("Executing order: {} at price: {}", order.getId(), executionPrice);

        try {
            // 주문 상태를 다시 조회하여 여전히 WAITING인지 확인 (동시성 제어)
            TradeHistory currentOrder = tradeHistoryRepository.findByIdWithLock(order.getId())
                .orElseThrow(() -> new IllegalStateException("Order not found"));

            // 이미 취소되거나 체결된 주문인지 확인
            if (currentOrder.getStatus() != TradeHistory.TradeStatus.WAITING) {
                log.info("Order {} is no longer waiting (status: {}), skipping execution", 
                    order.getId(), currentOrder.getStatus());
                return;
            }

            if (order.getSide() == TradeHistory.Side.BUY) {
                executeBuyOrder(currentOrder, executionPrice);
            } else {
                executeSellOrder(currentOrder, executionPrice);
            }

            // 주문 상태를 SUCCESS로 업데이트 (DB 스키마에 맞춤)
            currentOrder.setStatus(TradeHistory.TradeStatus.SUCCESS);
            currentOrder.setMatchedAt(LocalDateTime.now());
            tradeHistoryRepository.save(currentOrder);

            notifyOrderExecuted(currentOrder);

            log.info("Order executed successfully: {}", currentOrder.getId());

        } catch (Exception e) {
            log.error("Failed to execute order: {}", order.getId(), e);
            
            // 실행 실패 시에도 최신 상태 확인 후 처리
            try {
                TradeHistory currentOrder = tradeHistoryRepository.findById(order.getId())
                    .orElse(order);
                
                if (currentOrder.getStatus() == TradeHistory.TradeStatus.WAITING) {
                    currentOrder.setStatus(TradeHistory.TradeStatus.FAILED);
                    tradeHistoryRepository.save(currentOrder);
                    refundOrder(currentOrder);
                }
            } catch (Exception refundError) {
                log.error("Failed to handle execution failure for order: {}", order.getId(), refundError);
            }
        }
    }

    private void executeBuyOrder(TradeHistory order, BigDecimal executionPrice) {
        // 1. 지갑에서 locked 자금 해제
        Wallet wallet = walletRepository.findByMember(order.getMember())
            .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        BigDecimal lockedAmount = order.getPrice().multiply(order.getSize());
        BigDecimal actualAmount = executionPrice.multiply(order.getSize());
        BigDecimal difference = lockedAmount.subtract(actualAmount);

        // locked에서 실제 사용금액 차감
        wallet.setLocked(wallet.getLocked().subtract(lockedAmount));
        
        // 차액이 있으면 balance로 되돌림
        if (difference.compareTo(BigDecimal.ZERO) > 0) {
            wallet.setBalance(wallet.getBalance().add(difference));
        }

        walletRepository.save(wallet);

        // 2. 보유 자산에 추가 (avg_price로 컬럼명 맞춤)
        Optional<Holding> existingHolding = holdingRepository
            .findByMemberAndMarket(order.getMember(), order.getMarket());

        if (existingHolding.isPresent()) {
            // 기존 보유량에 추가
            Holding holding = existingHolding.get();
            
            // 평균 단가 계산 (avg_price 필드 사용)
            BigDecimal totalValue = holding.getAvgPrice().multiply(holding.getSize())
                .add(executionPrice.multiply(order.getSize()));
            BigDecimal totalSize = holding.getSize().add(order.getSize());
            BigDecimal newAveragePrice = totalValue.divide(totalSize, 8, RoundingMode.HALF_UP);
            
            holding.setSize(totalSize);
            holding.setAvgPrice(newAveragePrice);
            holdingRepository.save(holding);
        } else {
            // 새로운 보유 자산 생성
            Holding newHolding = Holding.builder()
                .member(order.getMember())
                .market(order.getMarket())
                .size(order.getSize())
                .avgPrice(executionPrice)
                .locked(BigDecimal.ZERO)
                .build();
            holdingRepository.save(newHolding);
        }
    }

    private void executeSellOrder(TradeHistory order, BigDecimal executionPrice) {
        // 1. 보유 자산에서 locked 해제
        Holding holding = holdingRepository.findByMemberAndMarket(order.getMember(), order.getMarket())
            .orElseThrow(() -> new IllegalStateException("Holding not found"));

        holding.setLocked(holding.getLocked().subtract(order.getSize()));
        holdingRepository.save(holding);

        // 2. 지갑에 매도 대금 추가
        Wallet wallet = walletRepository.findByMember(order.getMember())
            .orElseThrow(() -> new IllegalStateException("Wallet not found"));

        BigDecimal saleAmount = executionPrice.multiply(order.getSize());
        wallet.setBalance(wallet.getBalance().add(saleAmount));
        walletRepository.save(wallet);
    }

    private void notifyOrderExecuted(TradeHistory order) {
        TradeHistoryDto dto = new TradeHistoryDto();
        dto.setId(order.getId());
        dto.setMemberId(order.getMember().getId());
        dto.setMarket(order.getMarket());
        dto.setSide(order.getSide().name());
        dto.setPrice(order.getPrice());
        dto.setSize(order.getSize());
        dto.setStatus(order.getStatus().name());
        dto.setRequestAt(order.getRequestAt());
        dto.setMatchedAt(order.getMatchedAt());

        messagingTemplate.convertAndSend("/topic/orders/" + order.getMember().getEmail(), dto);
    }


    @Transactional
    public void cancelOrder(
        Long orderId,
        org.springframework.security.core.userdetails.User user
    ) {
        String email = user.getUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        // 비관적 락으로 주문 조회
        TradeHistory order = tradeHistoryRepository.findByIdAndMemberWithLock(orderId, member)
            .orElseThrow(() -> new IllegalArgumentException("Order not found"));

        // 상태 재확인 (락 이후에도 WAITING인지 확인)
        if (order.getStatus() != TradeHistory.TradeStatus.WAITING) {
            throw new IllegalStateException("Only waiting orders can be cancelled");
        }

        // 주문 상태를 CANCELLED로 변경
        order.setStatus(TradeHistory.TradeStatus.CANCELLED);
        tradeHistoryRepository.save(order);

        // 자금 해제
        refundOrder(order);

        log.info("Order cancelled: {}", orderId);
    }

    @Transactional
    private void refundOrder(TradeHistory order) {
        if (order.getSide() == TradeHistory.Side.BUY) {
            // BUY 주문 취소: locked 자금을 balance로 되돌림
            Wallet wallet = walletRepository.findByMember(order.getMember())
                .orElseThrow(() -> new IllegalStateException("Wallet not found"));

            BigDecimal refundAmount = order.getPrice().multiply(order.getSize());
            wallet.setLocked(wallet.getLocked().subtract(refundAmount));
            wallet.setBalance(wallet.getBalance().add(refundAmount));
            walletRepository.save(wallet);

        } else {
            // SELL 주문 취소: locked 자산을 size로 되돌림
            Holding holding = holdingRepository.findByMemberAndMarket(order.getMember(), order.getMarket())
                .orElseThrow(() -> new IllegalStateException("Holding not found"));

            holding.setLocked(holding.getLocked().subtract(order.getSize()));
            holding.setSize(holding.getSize().add(order.getSize()));
            holdingRepository.save(holding);
        }
    }

    public Optional<List<HoldingDto>> getAllHoldings(
        org.springframework.security.core.userdetails.User user
    ) {
        String email = user.getUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        Optional<List<Holding>> holdings = holdingRepository.findAllByMember(member);
        if (holdings.isEmpty() || holdings.get().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(holdings.get().stream().map(holding -> {
                HoldingDto dto = new HoldingDto();
                dto.setMarket(holding.getMarket());
                dto.setSize(holding.getSize());
                dto.setLocked(holding.getLocked());
                return dto;
            }).toList());
        }
    }

    public Optional<List<TradeHistoryDto>> getAllTradeHistories(
        org.springframework.security.core.userdetails.User user
    ) {
        String email = user.getUsername();
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("Member not found"));

        Optional<List<TradeHistory>> tradeHistories = tradeHistoryRepository.findAllByMember(member);
        if (tradeHistories.isEmpty() || tradeHistories.get().isEmpty()) {
            return Optional.empty();
        } else {
            return Optional.of(tradeHistories.get().stream().map(history -> {
                TradeHistoryDto dto = new TradeHistoryDto();
                dto.setId(history.getId());
                dto.setMemberId(history.getMember().getId());
                dto.setMarket(history.getMarket());
                dto.setSide(history.getSide().name());
                dto.setPrice(history.getPrice());
                dto.setSize(history.getSize());
                dto.setStatus(history.getStatus().name());
                dto.setRequestAt(history.getRequestAt());
                dto.setMatchedAt(history.getMatchedAt());
                return dto;
            }).toList());
        }
    }
}
