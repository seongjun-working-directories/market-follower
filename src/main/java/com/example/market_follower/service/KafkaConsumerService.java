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

            for (UpbitTickerDto dto : tickers) {
                try {
                    // Redis 저장 (Key: market, Value: JSON)
                    String key = "upbit:ticker:" + dto.getMarket();
                    String jsonValue = objectMapper.writeValueAsString(dto);
                    redisTemplate.opsForValue().set(key, jsonValue, Duration.ofMinutes(10));

                    // WebSocket으로 즉시 푸시
                    messagingTemplate.convertAndSend("/topic/ticker/all", tickers);
                    messagingTemplate.convertAndSend("/topic/ticker/" + dto.getMarket(), dto);
                } catch (Exception e) {
                    log.error("Failed processing ticker {}", dto.getMarket(), e);
                }
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }
}
