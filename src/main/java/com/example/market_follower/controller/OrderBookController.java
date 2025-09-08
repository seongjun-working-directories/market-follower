package com.example.market_follower.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.market_follower.dto.upbit.TradeRequestDto;
import com.example.market_follower.dto.upbit.TradeHistoryDto;
import com.example.market_follower.dto.upbit.HoldingDto;
import com.example.market_follower.service.OrderbookService;

@Slf4j
@RestController
@RequestMapping(value = "/orderbook")
@RequiredArgsConstructor
@Tag(name = "Orderbook API", description = "암호화폐 호가 API")
public class OrderbookController {
    private final OrderbookService orderbookService;

    @PostMapping("/request")
    public ResponseEntity<String> requestTrade(
        @RequestBody TradeRequestDto tradeRequestDto,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            orderbookService.requestTrade(user, tradeRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("성공적으로 접수되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid trade request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Trade request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to place trade: {}", tradeRequestDto, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 처리 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/cancel/{orderId}")
    public ResponseEntity<String> cancelOrder(
        @PathVariable Long orderId,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            orderbookService.cancelOrder(orderId, user);
            return ResponseEntity.status(HttpStatus.OK).body("주문이 성공적으로 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("Cancel order failed - order not found: orderId={}, error={}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("주문을 찾을 수 없습니다.");
        } catch (IllegalStateException e) {
            log.warn("Cancel order failed - invalid status: orderId={}, error={}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to cancel order: orderId={}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 취소 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/holding/all")
    public ResponseEntity<Optional<List<HoldingDto>>> getAllHoldings(
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            Optional<List<HoldingDto>> holdings = orderbookService.getAllHoldings(user);
            return ResponseEntity.status(HttpStatus.OK).body(holdings);
        } catch (Exception e) {
            log.error("Failed to retrieve holdings for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/history/all")
    public ResponseEntity<Optional<List<TradeHistoryDto>>> getAllTradeHistories(
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            Optional<List<TradeHistoryDto>> tradeHistories = orderbookService.getAllTradeHistories(user);
            return ResponseEntity.status(HttpStatus.OK).body(tradeHistories);
        } catch (Exception e) {
            log.error("Failed to retrieve trade histories for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

}
