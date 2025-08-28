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
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {
    private final ObjectMapper objectMapper;     // JSON 파싱
    private final RestTemplate restTemplate = new RestTemplate();     // HTTP 호출
    private final UpbitCandle7dRepository upbitCandle7dRepository;
    private final UpbitCandle30dRepository upbitCandle30dRepository;
    private final UpbitCandle3mRepository upbitCandle3mRepository;
    private final UpbitCandle1yRepository upbitCandle1yRepository;
    private final UpbitCandle5yRepository upbitCandle5yRepository;
    private final MarketService marketService;

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null) {
            return null;
        }

        try {
            // ISO_DATE_TIME 형식 시도
            return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME);
        } catch (Exception e1) {
            try {
                // 기본 형식 시도
                return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
            } catch (Exception e2) {
                log.error("Failed to parse datetime: {}", dateTimeString);
                throw new RuntimeException("Cannot parse datetime: " + dateTimeString, e2);
            }
        }
    }

    @Transactional
    public void initializeCandles(String type) {
        List<String> coins = marketService.getAllTradableCoins().stream()
                .map(TradableCoinDto::getMarket)
                .toList();

        try {
            int count = 1;

            switch (type) {
                case "A":
                    for (String coin : coins) {
                        log.info("7일 캔들 - {}/{}", count, coins.size());
                        process7d(coin);
                        count += 1;
                    }
                    break;
                case "B":
                    for (String coin : coins) {
                        log.info("30일 캔들 - {}/{}", count, coins.size());
                        process30d(coin);
                        count += 1;
                    }
                    break;
                case "C":
                    for (String coin : coins) {
                        log.info("3달 캔들 - {}/{}", count, coins.size());
                        process3m(coin);
                        count += 1;
                    }
                    break;
                case "D":
                    for (String coin : coins) {
                        log.info("1년 캔들 - {}/{}", count, coins.size());
                        process1y(coin);
                        count += 1;
                    }
                    break;
                case "E":
                    for (String coin : coins) {
                        log.info("5년 캔들 - {}/{}", count, coins.size());
                        process5y(coin);
                        count += 1;
                    }
                    break;
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
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                // 중복 체크 추가
                if (!upbitCandle7dRepository.existsByMarketAndCandleDateTimeUtc(coin, candleDateTimeUtc)) {
                    UpbitCandle7d entity = UpbitCandle7d.builder()
                            .market(dto.getMarket())
                            .candleDateTimeUtc(candleDateTimeUtc)
                            .candleDateTimeKst(parseDateTime(dto.getCandleDateTimeKst()))
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
            log.info("CandleService에서 캔들 데이터(7일) 초기 세팅 완료 - {}", coin);
        }
        Thread.sleep(150);
    }

    private void process30d(String coin) throws InterruptedException {
        String url = "https://api.upbit.com/v1/candles/minutes/240?market=" + coin + "&count=180";
        UpbitCandle30dDto[] dtos = restTemplate.getForObject(url, UpbitCandle30dDto[].class);
        if (dtos != null) {
            for (UpbitCandle30dDto dto : dtos) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                // 중복 체크 추가
                if (!upbitCandle30dRepository.existsByMarketAndCandleDateTimeUtc(coin, candleDateTimeUtc)) {
                    UpbitCandle30d entity = UpbitCandle30d.builder()
                            .market(dto.getMarket())
                            .candleDateTimeUtc(candleDateTimeUtc)
                            .candleDateTimeKst(parseDateTime(dto.getCandleDateTimeKst()))
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
            log.info("CandleService에서 캔들 데이터(30일) 초기 세팅 완료 - {}", coin);
        }
        Thread.sleep(150);
    }

    private void process3m(String coin) throws InterruptedException {
        String url = "https://api.upbit.com/v1/candles/days?market=" + coin + "&count=90";
        UpbitCandle3mDto[] dtos = restTemplate.getForObject(url, UpbitCandle3mDto[].class);
        if (dtos != null) {
            for (UpbitCandle3mDto dto : dtos) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                // 중복 체크 추가
                if (!upbitCandle3mRepository.existsByMarketAndCandleDateTimeUtc(coin, candleDateTimeUtc)) {
                    UpbitCandle3m entity = UpbitCandle3m.builder()
                            .market(dto.getMarket())
                            .candleDateTimeUtc(candleDateTimeUtc)
                            .candleDateTimeKst(parseDateTime(dto.getCandleDateTimeKst()))
                            .openingPrice(dto.getOpeningPrice())
                            .highPrice(dto.getHighPrice())
                            .lowPrice(dto.getLowPrice())
                            .tradePrice(dto.getTradePrice())
                            .timestamp(dto.getTimestamp())
                            .candleAccTradePrice(dto.getCandleAccTradePrice())
                            .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                            .prevClosingPrice(dto.getPrevClosingPrice())
                            .changePrice(dto.getChangePrice())
                            .changeRate(dto.getChangeRate())
                            .build();
                    upbitCandle3mRepository.save(entity);
                }
            }
            log.info("CandleService에서 캔들 데이터(3달) 초기 세팅 완료 - {}", coin);
        }
        Thread.sleep(150);
    }

    private void process1y(String coin) throws InterruptedException {
        List<UpbitCandle1y> entities = new ArrayList<>();

        // 첫 번째 요청: 최신 200일
        String url1 = "https://api.upbit.com/v1/candles/days?market=" + coin + "&count=200";
        UpbitCandle1yDto[] dtos1 = restTemplate.getForObject(url1, UpbitCandle1yDto[].class);

        if (dtos1 != null && dtos1.length > 0) {
            // 첫 번째 배치 처리
            for (UpbitCandle1yDto dto : dtos1) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                // 중복 체크 (선택사항 → 필수로 변경)
                if (!upbitCandle1yRepository.existsByMarketAndCandleDateTimeUtc(coin, candleDateTimeUtc)) {
                    UpbitCandle1y entity = UpbitCandle1y.builder()
                            .market(dto.getMarket())
                            .candleDateTimeUtc(candleDateTimeUtc)
                            .candleDateTimeKst(parseDateTime(dto.getCandleDateTimeKst()))
                            .openingPrice(dto.getOpeningPrice())
                            .highPrice(dto.getHighPrice())
                            .lowPrice(dto.getLowPrice())
                            .tradePrice(dto.getTradePrice())
                            .timestamp(dto.getTimestamp())
                            .candleAccTradePrice(dto.getCandleAccTradePrice())
                            .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                            .prevClosingPrice(dto.getPrevClosingPrice())
                            .changePrice(dto.getChangePrice())
                            .changeRate(dto.getChangeRate())
                            .build();
                    entities.add(entity);
                }
            }
            Thread.sleep(150); // API 요청 간격

            // 두 번째 요청: 나머지 165일
            String lastDateTimeUtc = dtos1[dtos1.length - 1].getCandleDateTimeUtc();
            LocalDateTime lastDateTime = parseDateTime(lastDateTimeUtc);
            LocalDateTime beforeDateTime = lastDateTime.minusDays(1);
            String beforeDate = beforeDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            String url2 = "https://api.upbit.com/v1/candles/days?market=" + coin + "&count=165&to=" + beforeDate;
            UpbitCandle1yDto[] dtos2 = restTemplate.getForObject(url2, UpbitCandle1yDto[].class);

            if (dtos2 != null) {
                // 두 번째 배치 처리
                for (UpbitCandle1yDto dto : dtos2) {
                    LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                    if (!upbitCandle1yRepository.existsByMarketAndCandleDateTimeUtc(coin, candleDateTimeUtc)) {
                        UpbitCandle1y entity = UpbitCandle1y.builder()
                                .market(dto.getMarket())
                                .candleDateTimeUtc(candleDateTimeUtc)
                                .candleDateTimeKst(parseDateTime(dto.getCandleDateTimeKst()))
                                .openingPrice(dto.getOpeningPrice())
                                .highPrice(dto.getHighPrice())
                                .lowPrice(dto.getLowPrice())
                                .tradePrice(dto.getTradePrice())
                                .timestamp(dto.getTimestamp())
                                .candleAccTradePrice(dto.getCandleAccTradePrice())
                                .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                                .prevClosingPrice(dto.getPrevClosingPrice())
                                .changePrice(dto.getChangePrice())
                                .changeRate(dto.getChangeRate())
                                .build();
                        entities.add(entity);
                    }
                }
            }

            // 배치로 한 번에 저장 (성능 향상)
            if (!entities.isEmpty()) {
                upbitCandle1yRepository.saveAll(entities);
                log.info("CandleService에서 캔들 데이터(1년) 초기 세팅 완료 - {} 코인, 총 {}일", coin, entities.size());
            }
        }
        Thread.sleep(150);
    }

    private void process5y(String coin) throws InterruptedException {
        List<UpbitCandle5y> entities = new ArrayList<>();

        // 첫 번째 요청: 최신 200주
        String url1 = "https://api.upbit.com/v1/candles/weeks?market=" + coin + "&count=200";
        UpbitCandle5yDto[] dtos1 = restTemplate.getForObject(url1, UpbitCandle5yDto[].class);

        if (dtos1 != null && dtos1.length > 0) {
            // 첫 번째 배치 처리
            for (UpbitCandle5yDto dto : dtos1) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                // 중복 체크 추가
                if (!upbitCandle5yRepository.existsByMarketAndCandleDateTimeUtc(coin, candleDateTimeUtc)) {
                    UpbitCandle5y entity = UpbitCandle5y.builder()
                            .market(dto.getMarket())
                            .candleDateTimeUtc(candleDateTimeUtc)
                            .candleDateTimeKst(parseDateTime(dto.getCandleDateTimeKst()))
                            .openingPrice(dto.getOpeningPrice())
                            .highPrice(dto.getHighPrice())
                            .lowPrice(dto.getLowPrice())
                            .tradePrice(dto.getTradePrice())
                            .timestamp(dto.getTimestamp())
                            .candleAccTradePrice(dto.getCandleAccTradePrice())
                            .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                            .firstDayOfPeriod(dto.getFirstDayOfPeriod())
                            .build();
                    entities.add(entity);
                }
            }
            Thread.sleep(150);

            // 두 번째 요청: 나머지 60주
            String lastDateTimeUtc = dtos1[dtos1.length - 1].getCandleDateTimeUtc();
            LocalDateTime lastDateTime = parseDateTime(lastDateTimeUtc);
            LocalDateTime beforeDateTime = lastDateTime.minusDays(7);
            String beforeDate = beforeDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            String url2 = "https://api.upbit.com/v1/candles/weeks?market=" + coin + "&count=60&to=" + beforeDate;
            UpbitCandle5yDto[] dtos2 = restTemplate.getForObject(url2, UpbitCandle5yDto[].class);

            if (dtos2 != null) {
                // 두 번째 배치 처리
                for (UpbitCandle5yDto dto : dtos2) {
                    LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                    if (!upbitCandle5yRepository.existsByMarketAndCandleDateTimeUtc(coin, candleDateTimeUtc)) {
                        UpbitCandle5y entity = UpbitCandle5y.builder()
                                .market(dto.getMarket())
                                .candleDateTimeUtc(candleDateTimeUtc)
                                .candleDateTimeKst(parseDateTime(dto.getCandleDateTimeKst()))
                                .openingPrice(dto.getOpeningPrice())
                                .highPrice(dto.getHighPrice())
                                .lowPrice(dto.getLowPrice())
                                .tradePrice(dto.getTradePrice())
                                .timestamp(dto.getTimestamp())
                                .candleAccTradePrice(dto.getCandleAccTradePrice())
                                .candleAccTradeVolume(dto.getCandleAccTradeVolume())
                                .firstDayOfPeriod(dto.getFirstDayOfPeriod())
                                .build();
                        entities.add(entity);
                    }
                }
            }

            // 배치 저장
            if (!entities.isEmpty()) {
                upbitCandle5yRepository.saveAll(entities);
                log.info("CandleService에서 캔들 데이터(5년) 초기 세팅 완료 - {} 코인, 총 {}주", coin, entities.size());
            }
        }
        Thread.sleep(150);
    }

    @Transactional
    public void deleteInvalidCandles() {
        List<String> coins = marketService.getAllTradableCoins().stream()
                .map(TradableCoinDto::getMarket)
                .toList();

        if (coins.isEmpty()) {
            log.warn("No tradable coins, skipping deletion");
            return;
        }

        try {
            upbitCandle7dRepository.deleteByMarketNotIn(coins);
            upbitCandle30dRepository.deleteByMarketNotIn(coins);
            upbitCandle3mRepository.deleteByMarketNotIn(coins);
            upbitCandle1yRepository.deleteByMarketNotIn(coins);
            upbitCandle5yRepository.deleteByMarketNotIn(coins);
        } catch (Exception e) {
            log.error("Timeout or other exception, rolling back", e);
            throw new RuntimeException(e); // 모든 예외를 런타임으로 감싸 롤백
        }
    }
}
