package com.example.market_follower.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.market_follower.dto.upbit.TradeRequestDto;
import com.example.market_follower.service.OrderbookService;

@Slf4j
@RestController
@RequestMapping(value = "/orderbook")
@RequiredArgsConstructor
@Tag(name = "Orderbook API", description = "암호화폐 호가 API")
public class OrderbookController {
    private final OrderbookService orderbookService;

    @PostMapping("/request")
    public ResponseEntity<Void> requestTrade(
        @RequestBody TradeRequestDto tradeRequestDto,
        @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            orderbookService.requestTrade(user, tradeRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } catch (Exception e) {
            log.error("Failed to place trade: {}", tradeRequestDto, e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
