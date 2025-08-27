package com.example.market_follower.controller;

import com.example.market_follower.service.CandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping(value = "/candle")
@RequiredArgsConstructor
@Tag(name = "Candle API", description = "암호화폐 캔들 정보 조회 API")
public class CandleController {
    private final CandleService candleService;

    @GetMapping("/initialization")
    public ResponseEntity<Void> initialize() {
        try {
            candleService.initializeCandles();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            log.error("Error initializing candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}
