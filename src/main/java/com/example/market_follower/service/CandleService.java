package com.example.market_follower.service;

import com.example.market_follower.dto.upbit.TradableCoinDto;
import com.example.market_follower.dto.upbit.candle.*;
import com.example.market_follower.model.candle.*;
import com.example.market_follower.repository.candle.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {
    private final ObjectMapper objectMapper;     // JSON 파싱
    private final RestTemplate restTemplate;     // HTTP 호출
    private final UpbitCandle7dRepository upbitCandle7dRepository;
    private final UpbitCandle30dRepository upbitCandle30dRepository;
    private final UpbitCandle3mRepository upbitCandle3mRepository;
    private final UpbitCandle1yRepository upbitCandle1yRepository;
    private final UpbitCandle5yRepository upbitCandle5yRepository;
    private final MarketService marketService;

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Transactional
    public void initializeCandles() {
        List<String> coins = marketService.getAllTradableCoins().stream()
                .map(TradableCoinDto::getMarket)
                .toList();

        try {
            for (String coin : coins) {
                process7d(coin);
            }

            for (String coin : coins) {
                process30d(coin);
            }

            for (String coin : coins) {
                process3m(coin);
            }

            for (String coin : coins) {
                process1y(coin);
            }

            for (String coin : coins) {
                process5y(coin);
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Initialization interrupted, rolling back", e);

            // 런타임 예외를 던져 트랜잭션 롤백
            throw new RuntimeException("Candle initialization interrupted", e);
        } catch (Exception e) {
            log.error("Timeout or other exception, rolling back", e);
            throw new RuntimeException(e); // 모든 예외를 런타임으로 감싸 롤백
        }
    }

    private void process7d(String coin) throws InterruptedException {
        String url = "https://api.upbit.com/v1/candles/minutes/60?market=" + coin + "&count=168";
        UpbitCandle7dDto[] dtos = restTemplate.getForObject(url, UpbitCandle7dDto[].class);
        if (dtos != null) {
            for (UpbitCandle7dDto dto : dtos) {
                UpbitCandle7d entity = UpbitCandle7d.builder()
                        .market(dto.getMarket())
                        .candleDateTimeUtc(LocalDateTime.parse(dto.getCandleDateTimeUtc(), DATE_TIME_FORMATTER))
                        .candleDateTimeKst(LocalDateTime.parse(dto.getCandleDateTimeKst(), DATE_TIME_FORMATTER))
                        .openingPrice(dto.getOpeningPrice())
                        .highPrice(dto.getHighPrice())
                        .lowPrice(dto.getLowPrice())
                        .tradePrice(dto.getTradePrice())
                        .timestamp(dto.getTimestamp())
                        .candleAccTradePrice(dto.getCandleAccTradePrice())
                        .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                        .unit(dto.getUnit())
                        .build();
                upbitCandle7dRepository.save(entity);
            }
        }
        Thread.sleep(200);
    }

    private void process30d(String coin) throws InterruptedException {
        String url = "https://api.upbit.com/v1/candles/minutes/240?market=" + coin + "&count=180";
        UpbitCandle30dDto[] dtos = restTemplate.getForObject(url, UpbitCandle30dDto[].class);
        if (dtos != null) {
            for (UpbitCandle30dDto dto : dtos) {
                UpbitCandle30d entity = UpbitCandle30d.builder()
                        .market(dto.getMarket())
                        .candleDateTimeUtc(LocalDateTime.parse(dto.getCandleDateTimeUtc(), DATE_TIME_FORMATTER))
                        .candleDateTimeKst(LocalDateTime.parse(dto.getCandleDateTimeKst(), DATE_TIME_FORMATTER))
                        .openingPrice(dto.getOpeningPrice())
                        .highPrice(dto.getHighPrice())
                        .lowPrice(dto.getLowPrice())
                        .tradePrice(dto.getTradePrice())
                        .timestamp(dto.getTimestamp())
                        .candleAccTradePrice(dto.getCandleAccTradePrice())
                        .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                        .unit(dto.getUnit())
                        .build();
                upbitCandle30dRepository.save(entity);
            }
        }
        Thread.sleep(200);
    }

    private void process3m(String coin) throws InterruptedException {
        String url = "https://api.upbit.com/v1/candles/days?market=" + coin + "&count=90";
        UpbitCandle3mDto[] dtos = restTemplate.getForObject(url, UpbitCandle3mDto[].class);
        if (dtos != null) {
            for (UpbitCandle3mDto dto : dtos) {
                UpbitCandle3m entity = UpbitCandle3m.builder()
                        .market(dto.getMarket())
                        .candleDateTimeUtc(LocalDateTime.parse(dto.getCandleDateTimeUtc(), DATE_TIME_FORMATTER))
                        .candleDateTimeKst(LocalDateTime.parse(dto.getCandleDateTimeKst(), DATE_TIME_FORMATTER))
                        .openingPrice(dto.getOpeningPrice())
                        .highPrice(dto.getHighPrice())
                        .lowPrice(dto.getLowPrice())
                        .tradePrice(dto.getTradePrice())
                        .timestamp(dto.getTimestamp())
                        .candleAccTradePrice(dto.getCandleAccTradePrice())
                        .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                        .unit(dto.getUnit())
                        .prevClosingPrice(dto.getPrevClosingPrice())
                        .changePrice(dto.getChangePrice())
                        .changeRate(dto.getChangeRate())
                        .build();
                upbitCandle3mRepository.save(entity);
            }
        }
        Thread.sleep(200);
    }

    private void process1y(String coin) throws InterruptedException {
        String url = "https://api.upbit.com/v1/candles/days?market=" + coin + "&count=365";
        UpbitCandle1yDto[] dtos = restTemplate.getForObject(url, UpbitCandle1yDto[].class);
        if (dtos != null) {
            for (UpbitCandle1yDto dto : dtos) {
                UpbitCandle1y entity = UpbitCandle1y.builder()
                        .market(dto.getMarket())
                        .candleDateTimeUtc(LocalDateTime.parse(dto.getCandleDateTimeUtc(), DATE_TIME_FORMATTER))
                        .candleDateTimeKst(LocalDateTime.parse(dto.getCandleDateTimeKst(), DATE_TIME_FORMATTER))
                        .openingPrice(dto.getOpeningPrice())
                        .highPrice(dto.getHighPrice())
                        .lowPrice(dto.getLowPrice())
                        .tradePrice(dto.getTradePrice())
                        .timestamp(dto.getTimestamp())
                        .candleAccTradePrice(dto.getCandleAccTradePrice())
                        .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                        .unit(dto.getUnit())
                        .prevClosingPrice(dto.getPrevClosingPrice())
                        .changePrice(dto.getChangePrice())
                        .changeRate(dto.getChangeRate())
                        .build();
                upbitCandle1yRepository.save(entity);
            }
        }
        Thread.sleep(200);
    }

    private void process5y(String coin) throws InterruptedException {
        String url = "https://api.upbit.com/v1/candles/weeks?market=" + coin + "&count=260";
        UpbitCandle5yDto[] dtos = restTemplate.getForObject(url, UpbitCandle5yDto[].class);
        if (dtos != null) {
            for (UpbitCandle5yDto dto : dtos) {
                UpbitCandle5y entity = UpbitCandle5y.builder()
                        .market(dto.getMarket())
                        .candleDateTimeUtc(LocalDateTime.parse(dto.getCandleDateTimeUtc(), DATE_TIME_FORMATTER))
                        .candleDateTimeKst(LocalDateTime.parse(dto.getCandleDateTimeKst(), DATE_TIME_FORMATTER))
                        .openingPrice(dto.getOpeningPrice())
                        .highPrice(dto.getHighPrice())
                        .lowPrice(dto.getLowPrice())
                        .tradePrice(dto.getTradePrice())
                        .timestamp(dto.getTimestamp())
                        .candleAccTradePrice(dto.getCandleAccTradePrice())
                        .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                        .unit(dto.getUnit())
                        .firstDayOfPeriod(dto.getFirstDayOfPeriod())
                        .build();
                upbitCandle5yRepository.save(entity);
            }
        }
        Thread.sleep(200);
    }
}
