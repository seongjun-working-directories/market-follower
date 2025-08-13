package com.example.market_follower.controller;

import com.example.market_follower.dto.upbit.TradableCoinDto;
import com.example.market_follower.dto.upbit.UpbitTickerDto;
import com.example.market_follower.service.MarketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(value = "/market")
@RequiredArgsConstructor
@Tag(name = "Market API", description = "주식 시세 API")
public class MarketController {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private final MarketService marketService;

    @GetMapping("/list")
    public ResponseEntity<List<TradableCoinDto>> getTradableCoinList() {
        try {
            List<TradableCoinDto> coins = marketService.getAllTradableCoins();
            return ResponseEntity.status(HttpStatus.OK).body(coins);
        } catch (Exception e) {
            log.error("Error fetching tradable coins", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/ticker/{market}")
    public ResponseEntity<UpbitTickerDto> getTicker(@PathVariable String market) throws Exception {
        try {
            String key = "upbit:ticker:" + market;
            String json = redisTemplate.opsForValue().get(key);
            UpbitTickerDto dto = json != null ? objectMapper.readValue(json, UpbitTickerDto.class) : null;
            if (dto != null) {
                return ResponseEntity.status(HttpStatus.OK).body(dto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("Error fetching ticker for market {}", market, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ticker/all")
    public ResponseEntity<List<UpbitTickerDto>> getAllTickers() throws Exception {
        try {
            // Redis에서 전체 마켓 키 패턴 가져오기
            Set<String> keys = redisTemplate.keys("upbit:ticker:*");
            List<UpbitTickerDto> tickers = new ArrayList<>();
            if (keys != null) {
                for (String key : keys) {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json != null) {
                        tickers.add(objectMapper.readValue(json, UpbitTickerDto.class));
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(tickers);
        } catch (Exception e) {
            log.error("Error fetching all tickers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
