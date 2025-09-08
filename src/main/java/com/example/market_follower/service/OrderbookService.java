package com.example.market_follower.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import com.example.market_follower.dto.upbit.TradeRequestDto;
import com.example.market_follower.model.Holding;
import com.example.market_follower.model.TradeHistory;
import com.example.market_follower.model.Wallet;
import com.example.market_follower.repository.HoldingRepository;
import com.example.market_follower.repository.TradeHistoryRepository;
import com.example.market_follower.repository.WalletRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderbookService {
    private final WalletRepository walletRepository;
    private final TradeHistoryRepository tradeHistoryRepository;
    private final HoldingRepository holdingRepository;

    @Transactional
    public void requestTrade(
        org.springframework.security.core.userdetails.User user,
        TradeRequestDto tradeRequestDto
    ) {
        // 1. memberId 추출 (예: username에 memberId 저장했다고 가정)
        Long memberId = Long.parseLong(user.getUsername());

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
        Wallet wallet = walletRepository.findByMemberId(memberId).orElseThrow(() -> new IllegalStateException("Wallet not found"));

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
                .memberId(memberId)
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
            Holding holding = holdingRepository.findByMemberIdAndMarket(memberId, tradeRequestDto.getMarket())
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
                .memberId(memberId)
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
}
