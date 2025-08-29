package com.example.market_follower.controller;

import com.example.market_follower.service.CandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/candle")
@RequiredArgsConstructor
@Tag(name = "Candle API", description = "암호화폐 캔들 정보 조회 API")
public class CandleController {
    private final CandleService candleService;

    @PutMapping("/upsert/manual")
    public ResponseEntity<Void> upsertCandleData() {
        try {
            candleService.updateDailyCandleData();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }  catch (Exception e) {
            log.error("Error manually updating candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/old/manual")
    public ResponseEntity<Void> deleteOldCandleData() {
        try {
            candleService.removeOldCandles();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }  catch (Exception e) {
            log.error("Error manually deleting old candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/invalid/coins/manual")
    public ResponseEntity<Void> deleteInvalidCoins() {
        try {
            candleService.deleteInvalidCandles();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            log.error("Error deleting invalid candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}