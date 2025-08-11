package com.example.market_follower.controller;

import com.example.market_follower.dto.TradableCoinDto;
import com.example.market_follower.service.MarketService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping(value = "/market")
@RequiredArgsConstructor
@Tag(name = "Market API", description = "주식 시세 API")
public class MarketController {
    private final MarketService marketService;

    @GetMapping("/all")
    public ResponseEntity<List<TradableCoinDto>> getAllTradableCoins() {
        try {
            List<TradableCoinDto> coins = marketService.getAllTradableCoins();
            return ResponseEntity.status(HttpStatus.OK).body(coins);
        } catch (Exception e) {
            log.error("Error fetching tradable coins", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
