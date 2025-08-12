package com.example.market_follower.service;

import com.example.market_follower.model.UpbitTicker;
import com.example.market_follower.dto.upbit.UpbitTickerDto;
import com.example.market_follower.repository.UpbitTickerRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final ObjectMapper objectMapper;
    private final UpbitTickerRepository upbitTickerRepository;

    @KafkaListener(topics = "upbit-ticker-topic", groupId = "upbit-group", concurrency = "3")
    public void consume(String message) {
        try {
            log.info("Kafka Consumer received message: {}", message);

            List<UpbitTickerDto> tickers = objectMapper.readValue(message, new TypeReference<List<UpbitTickerDto>>() {});

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

                // DB 저장
                upbitTickerRepository.save(entity);
            }

        } catch (Exception e) {
            log.error("Failed to process Kafka message", e);
        }
    }
}
