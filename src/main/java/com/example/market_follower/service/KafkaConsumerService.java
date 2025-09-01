package com.example.market_follower.service;

import com.example.market_follower.dto.upbit.UpbitTickerDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;
    private final StringRedisTemplate redisTemplate;       // Redis로 인메모리 캐시에 저장 후 Websocket으로 발송
    private final SimpMessagingTemplate messagingTemplate; // STOMP WebSocket 발송용

    // Kafka에서 받은 메시지를 Redis에 최신 상태로 저장만 하고 WebSocket 발송은 주기별로 처리
    @KafkaListener(topics = "upbit-ticker-topic", groupId = "upbit-group", concurrency = "3")
    public void consume(String message) {
        try {
            // Kafka에서 받은 메시지를 객체로 변환
            List<UpbitTickerDto> tickers = objectMapper.readValue(message, new TypeReference<List<UpbitTickerDto>>() {
            });
            log.info("Kafa Consumer received {} tickers", tickers.size());

            // 1. 먼저 모든 데이터를 Redis에 저장 (최신 상태 유지)
            for (UpbitTickerDto dto : tickers) {
                try {
                    String key = "upbit:ticker:" + dto.getMarket();
                    String jsonValue = objectMapper.writeValueAsString(dto);
                    redisTemplate.opsForValue().set(key, jsonValue, Duration.ofMinutes(5));
                } catch (Exception e) {
                    log.error("Failed to save ticker to Redis: {}", dto.getMarket(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }

    // 2. WebSocket 발송을 10초마다 실행, Redis에서 최신 데이터만 조회
    @Scheduled(initialDelay = 130000, fixedRate = 10000) // 10초마다 발송
    public void broadcastLatestTickers() {
        try {
            List<UpbitTickerDto> latestTickers = new ArrayList<>();

            // SCAN으로 안전하게 키 조회
            try (Cursor<byte[]> cursor = redisTemplate.getConnectionFactory()
                    .getConnection()
                    .scan(ScanOptions.scanOptions().match("upbit:ticker:*").count(1000).build())) {

                while (cursor.hasNext()) {
                    String key = new String(cursor.next());
                    String json = redisTemplate.opsForValue().get(key);
                    if (json != null) {
                        latestTickers.add(objectMapper.readValue(json, UpbitTickerDto.class));
                    }
                }
            }

            if (latestTickers.isEmpty()) {
                log.warn("No tickers found in Redis to broadcast");
                return;
            }

            // 전체 리스트는 한 번만 전송
            try {
                messagingTemplate.convertAndSend("/topic/ticker/all", latestTickers);
                log.debug("Broadcasted latest tickers to /topic/ticker/all: {} coins", latestTickers.size());
            } catch (Exception e) {
                log.error("Failed to send all tickers via WebSocket", e);
            }

            // 개별 코인 데이터도 각각 전송
            for (UpbitTickerDto dto : latestTickers) {
                try {
                    messagingTemplate.convertAndSend("/topic/ticker/" + dto.getMarket(), dto);
                } catch (Exception e) {
                    log.error("Failed to send individual ticker {}", dto.getMarket(), e);
                }
            }
        } catch (Exception e) {
            log.error("Failed to broadcast latest tickers via WebSocket", e);
        }
    }
}
