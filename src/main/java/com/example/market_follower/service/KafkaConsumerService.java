package com.example.market_follower.service;

import com.example.market_follower.model.UpbitTicker;
import com.example.market_follower.dto.upbit.UpbitTickerDto;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

// DB에 upbit_ticker를 저장하지 않음
// import com.example.market_follower.repository.UpbitTickerRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;

    // DB에 upbit_ticker를 저장하지 않음
    // // private final UpbitTickerRepository upbitTickerRepository;

    // 대신 Redis로 인메모리 캐시에 저장 후 Websocket으로 발송
    private final StringRedisTemplate redisTemplate;
    private final SimpMessagingTemplate messagingTemplate; // STOMP WebSocket 발송용

    @KafkaListener(topics = "upbit-ticker-topic", groupId = "upbit-group", concurrency = "3")
    public void consume(String message) {
        try {
            log.info("Kafka Consumer received message: {}", message);

            List<UpbitTickerDto> tickers = objectMapper.readValue(message, new TypeReference<List<UpbitTickerDto>>() {});

            /* DB에 upbit_ticker를 저장하지 않음
            for (UpbitTickerDto dto : tickers) {
                log.debug("Processing ticker: {}", dto);

                // DTO -> Entity 변환 (직접 생성자 또는 빌더 이용)
                UpbitTicker entity = UpbitTicker.builder()
                        .market(dto.getMarket())
                        .tradeDate(dto.getTradeDate())
                        .tradeTime(dto.getTradeTime())
                        .tradeDateKst(dto.getTradeDateKst())
                        .tradeTimeKst(dto.getTradeTimeKst())
                        .tradeTimestamp(dto.getTradeTimestamp())
                        .openingPrice(dto.getOpeningPrice())
                        .highPrice(dto.getHighPrice())
                        .lowPrice(dto.getLowPrice())
                        .tradePrice(dto.getTradePrice())
                        .prevClosingPrice(dto.getPrevClosingPrice())
                        .change(dto.getChange())
                        .changePrice(dto.getChangePrice())
                        .changeRate(dto.getChangeRate())
                        .signedChangePrice(dto.getSignedChangePrice())
                        .signedChangeRate(dto.getSignedChangeRate())
                        .tradeVolume(dto.getTradeVolume())
                        .accTradePrice(dto.getAccTradePrice())
                        .accTradePrice24h(dto.getAccTradePrice24h())
                        .accTradeVolume(dto.getAccTradeVolume())
                        .accTradeVolume24h(dto.getAccTradeVolume24h())
                        .highest52WeekPrice(dto.getHighest52WeekPrice())
                        .highest52WeekDate(dto.getHighest52WeekDate())
                        .lowest52WeekPrice(dto.getLowest52WeekPrice())
                        .lowest52WeekDate(dto.getLowest52WeekDate())
                        .upbitTimestamp(dto.getUpbitTimestamp())
                        .build();

                upbitTickerRepository.save(entity); // DB에 저장
            }
             */

            // 1. 먼저 모든 데이터를 Redis에 저장
            for (UpbitTickerDto dto : tickers) {
                try {
                    String key = "upbit:ticker:" + dto.getMarket();
                    String jsonValue = objectMapper.writeValueAsString(dto);
                    redisTemplate.opsForValue().set(key, jsonValue, Duration.ofMinutes(10));
                } catch (Exception e) {
                    log.error("Failed to save ticker to Redis: {}", dto.getMarket(), e);
                }
            }

            // 2. 전체 리스트는 딱 한 번만 전송
            try {
                messagingTemplate.convertAndSend("/topic/ticker/all", tickers);
                log.debug("Sent all tickers to /topic/ticker/all: {} coins", tickers.size());
            } catch (Exception e) {
                log.error("Failed to send all tickers via WebSocket", e);
            }

            // 3. 개별 코인 데이터도 각각 한 번씩만 전송
            for (UpbitTickerDto dto : tickers) {
                try {
                    messagingTemplate.convertAndSend("/topic/ticker/" + dto.getMarket(), dto);
                } catch (Exception e) {
                    log.error("Failed to send individual ticker {}", dto.getMarket(), e);
                    // 개별 코인 전송 실패해도 전체 처리는 계속
                }
            }

            log.info("Successfully processed {} tickers", tickers.size());
        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }
}
