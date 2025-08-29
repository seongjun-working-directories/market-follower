package com.example.market_follower.service;

import com.example.market_follower.dto.upbit.TradableCoinDto;
import com.example.market_follower.dto.upbit.candle.*;
import com.example.market_follower.model.candle.*;
import com.example.market_follower.repository.candle.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final UpbitCandle7dRepository upbitCandle7dRepository;
    private final UpbitCandle30dRepository upbitCandle30dRepository;
    private final UpbitCandle3mRepository upbitCandle3mRepository;
    private final UpbitCandle1yRepository upbitCandle1yRepository;
    private final UpbitCandle5yRepository upbitCandle5yRepository;
    private final MarketService marketService;

    private LocalDateTime parseDateTime(String dateTimeString) {
        if (dateTimeString == null) {  return null; }
        try { return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ISO_DATE_TIME); }
        catch (Exception e1) {
            try { return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")); }
            catch (Exception e2) {
                log.error("Failed to parse datetime: {}", dateTimeString);
                throw new RuntimeException("Cannot parse datetime: " + dateTimeString, e2);
            }
        }
    }

    // 7일 데이터 동기화 - 오늘 00시 기준 정확히 7일 범위
    private void sync7d(String coin) throws InterruptedException {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime sevenDaysAgoStart = todayStart.minusDays(7);

        log.info("7일 캔들 동기화 범위: {} ~ {}", sevenDaysAgoStart, todayStart);

        // 범위를 벗어나는 데이터 삭제
        upbitCandle7dRepository.deleteByMarketAndCandleDateTimeUtcNotBetween(
                coin, sevenDaysAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle7dRepository.findCandleDateTimeUtcByMarketAndDateRange(
                        coin, sevenDaysAgoStart, todayStart));

        // API에서 178개 요청 (1시간 간격, 7일치)
        String url = "https://api.upbit.com/v1/candles/minutes/60?market=" + coin + "&count=178";
        UpbitCandle7dDto[] dtos = restTemplate.getForObject(url, UpbitCandle7dDto[].class);

        if (dtos != null) {
            List<UpbitCandle7d> saveList = new ArrayList<>();

            for (UpbitCandle7dDto dto : dtos) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                if (candleDateTimeUtc != null &&
                        !candleDateTimeUtc.isBefore(sevenDaysAgoStart) &&
                        candleDateTimeUtc.isBefore(todayStart) &&
                        !existingDates.contains(candleDateTimeUtc)) {

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
                    saveList.add(entity);
                }
            }

            if (!saveList.isEmpty()) {
                upbitCandle7dRepository.saveAll(saveList);
            }

            log.info("7일 캔들 데이터 동기화 완료 - {} 코인, 저장: {}개", coin, saveList.size());
        }
        Thread.sleep(150);
    }

    // 30일 데이터 동기화 - 오늘 00시 기준 정확히 30일 범위
    private void sync30d(String coin) throws InterruptedException {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime thirtyDaysAgoStart = todayStart.minusDays(30);

        log.info("30일 캔들 동기화 범위: {} ~ {}", thirtyDaysAgoStart, todayStart);

        // 범위를 벗어나는 데이터 삭제
        upbitCandle30dRepository.deleteByMarketAndCandleDateTimeUtcNotBetween(
                coin, thirtyDaysAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle30dRepository.findCandleDateTimeUtcByMarketAndDateRange(
                        coin, thirtyDaysAgoStart, todayStart));

        // API에서 30일 데이터 요청 (4시간 단위)
        String url = "https://api.upbit.com/v1/candles/minutes/240?market=" + coin + "&count=190";
        UpbitCandle30dDto[] dtos = restTemplate.getForObject(url, UpbitCandle30dDto[].class);

        if (dtos != null) {
            List<UpbitCandle30d> saveList = new ArrayList<>();

            for (UpbitCandle30dDto dto : dtos) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                if (candleDateTimeUtc != null &&
                        !candleDateTimeUtc.isBefore(thirtyDaysAgoStart) &&
                        candleDateTimeUtc.isBefore(todayStart) &&
                        !existingDates.contains(candleDateTimeUtc)) {

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
                    saveList.add(entity);
                }
            }

            if (!saveList.isEmpty()) {
                upbitCandle30dRepository.saveAll(saveList);
            }

            log.info("30일 캔들 데이터 동기화 완료 - {} 코인, 저장: {}개", coin, saveList.size());
        }
        Thread.sleep(150);
    }

    // 3달 데이터 동기화 - 오늘 00시 기준 정확히 90일 범위
    private void sync3m(String coin) throws InterruptedException {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime ninetyDaysAgoStart = todayStart.minusDays(90);

        log.info("3달 캔들 동기화 범위: {} ~ {}", ninetyDaysAgoStart, todayStart);

        // 범위를 벗어나는 데이터 삭제
        upbitCandle3mRepository.deleteByMarketAndCandleDateTimeUtcNotBetween(
                coin, ninetyDaysAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle3mRepository.findCandleDateTimeUtcByMarketAndDateRange(
                        coin, ninetyDaysAgoStart, todayStart));

        // API에서 90일 데이터 요청 (일 단위)
        String url = "https://api.upbit.com/v1/candles/days?market=" + coin + "&count=100";
        UpbitCandle3mDto[] dtos = restTemplate.getForObject(url, UpbitCandle3mDto[].class);

        if (dtos != null) {
            List<UpbitCandle3m> saveList = new ArrayList<>();

            for (UpbitCandle3mDto dto : dtos) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                if (candleDateTimeUtc != null &&
                        !candleDateTimeUtc.isBefore(ninetyDaysAgoStart) &&
                        candleDateTimeUtc.isBefore(todayStart) &&
                        !existingDates.contains(candleDateTimeUtc)) {

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
                    saveList.add(entity);
                }
            }

            if (!saveList.isEmpty()) {
                upbitCandle3mRepository.saveAll(saveList);
            }

            log.info("3달 캔들 데이터 동기화 완료 - {} 코인, 저장: {}개", coin, saveList.size());
        }
        Thread.sleep(150);
    }

    // 1년 데이터 동기화 - 오늘 00시 기준 정확히 365일 범위
    private void sync1y(String coin) throws InterruptedException {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime oneYearAgoStart = todayStart.minusDays(365);

        log.info("1년 캔들 동기화 범위: {} ~ {}", oneYearAgoStart, todayStart);

        // 범위를 벗어나는 데이터 삭제
        upbitCandle1yRepository.deleteByMarketAndCandleDateTimeUtcNotBetween(
                coin, oneYearAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle1yRepository.findCandleDateTimeUtcByMarketAndDateRange(
                        coin, oneYearAgoStart, todayStart));

        List<UpbitCandle1y> entities = new ArrayList<>();

        // 첫 번째 요청: 최신 200일
        String url1 = "https://api.upbit.com/v1/candles/days?market=" + coin + "&count=200";
        UpbitCandle1yDto[] dtos1 = restTemplate.getForObject(url1, UpbitCandle1yDto[].class);

        if (dtos1 != null && dtos1.length > 0) {
            // 첫 번째 배치 처리
            for (UpbitCandle1yDto dto : dtos1) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                if (candleDateTimeUtc != null &&
                        !candleDateTimeUtc.isBefore(oneYearAgoStart) &&
                        candleDateTimeUtc.isBefore(todayStart) &&
                        !existingDates.contains(candleDateTimeUtc)) {

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
                    existingDates.add(candleDateTimeUtc);
                }
            }
            Thread.sleep(150);

            // 두 번째 요청: 나머지 165일
            String lastDateTimeUtc = dtos1[dtos1.length - 1].getCandleDateTimeUtc();
            LocalDateTime lastDateTime = parseDateTime(lastDateTimeUtc);
            LocalDateTime beforeDateTime = lastDateTime.minusDays(1);
            String beforeDate = beforeDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            String url2 = "https://api.upbit.com/v1/candles/days?market=" + coin + "&count=175&to=" + beforeDate;
            UpbitCandle1yDto[] dtos2 = restTemplate.getForObject(url2, UpbitCandle1yDto[].class);

            if (dtos2 != null) {
                for (UpbitCandle1yDto dto : dtos2) {
                    LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                    if (candleDateTimeUtc != null &&
                            !candleDateTimeUtc.isBefore(oneYearAgoStart) &&
                            candleDateTimeUtc.isBefore(todayStart) &&
                            !existingDates.contains(candleDateTimeUtc)) {

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
                        existingDates.add(candleDateTimeUtc);
                    }
                }
            }
            Thread.sleep(150);
        }

        // 배치 저장
        if (!entities.isEmpty()) {
            int batchSize = 100;
            for (int i = 0; i < entities.size(); i += batchSize) {
                int end = Math.min(i + batchSize, entities.size());
                upbitCandle1yRepository.saveAll(entities.subList(i, end));
            }
            log.info("1년 캔들 데이터 동기화 완료 - {} 코인, 저장: {}개", coin, entities.size());
        }
    }

    // 5년 데이터 동기화 - 오늘 00시 기준 정확히 5년 범위
    private void sync5y(String coin) throws InterruptedException {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();
        LocalDateTime fiveYearsAgoStart = todayStart.minusYears(5);

        log.info("5년 캔들 동기화 범위: {} ~ {}", fiveYearsAgoStart, todayStart);

        // 범위를 벗어나는 데이터 삭제
        upbitCandle5yRepository.deleteByMarketAndCandleDateTimeUtcNotBetween(
                coin, fiveYearsAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle5yRepository.findCandleDateTimeUtcByMarketAndDateRange(
                        coin, fiveYearsAgoStart, todayStart));

        List<UpbitCandle5y> entities = new ArrayList<>();

        // 첫 번째 요청: 최신 200주
        String url1 = "https://api.upbit.com/v1/candles/weeks?market=" + coin + "&count=200";
        UpbitCandle5yDto[] dtos1 = restTemplate.getForObject(url1, UpbitCandle5yDto[].class);

        if (dtos1 != null && dtos1.length > 0) {
            for (UpbitCandle5yDto dto : dtos1) {
                LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                if (candleDateTimeUtc != null &&
                        !candleDateTimeUtc.isBefore(fiveYearsAgoStart) &&
                        candleDateTimeUtc.isBefore(todayStart) &&
                        !existingDates.contains(candleDateTimeUtc)) {

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
                    existingDates.add(candleDateTimeUtc);
                }
            }
            Thread.sleep(150);

            // 두 번째 요청: 나머지 주 데이터 (5년은 약 260주이므로 추가 60주)
            String lastDateTimeUtc = dtos1[dtos1.length - 1].getCandleDateTimeUtc();
            LocalDateTime lastDateTime = parseDateTime(lastDateTimeUtc);
            LocalDateTime beforeDateTime = lastDateTime.minusDays(7);
            String beforeDate = beforeDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));

            String url2 = "https://api.upbit.com/v1/candles/weeks?market=" + coin + "&count=70&to=" + beforeDate;
            UpbitCandle5yDto[] dtos2 = restTemplate.getForObject(url2, UpbitCandle5yDto[].class);

            if (dtos2 != null) {
                for (UpbitCandle5yDto dto : dtos2) {
                    LocalDateTime candleDateTimeUtc = parseDateTime(dto.getCandleDateTimeUtc());

                    if (candleDateTimeUtc != null &&
                            !candleDateTimeUtc.isBefore(fiveYearsAgoStart) &&
                            candleDateTimeUtc.isBefore(todayStart) &&
                            !existingDates.contains(candleDateTimeUtc)) {

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
                        existingDates.add(candleDateTimeUtc);
                    }
                }
            }
            Thread.sleep(150);
        }

        // 배치 저장
        if (!entities.isEmpty()) {
            int batchSize = 100;
            for (int i = 0; i < entities.size(); i += batchSize) {
                int end = Math.min(i + batchSize, entities.size());
                upbitCandle5yRepository.saveAll(entities.subList(i, end));
            }
            log.info("5년 캔들 데이터 동기화 완료 - {} 코인, 저장: {}개", coin, entities.size());
        }
    }

    @Transactional
    public void removeOldCandles() {
        LocalDateTime todayStart = LocalDateTime.now().toLocalDate().atStartOfDay();

        // 각 타입별로 정확한 범위 기준으로 오래된 데이터 삭제
        LocalDateTime sevenDaysAgoStart = todayStart.minusDays(7);
        LocalDateTime thirtyDaysAgoStart = todayStart.minusDays(30);
        LocalDateTime ninetyDaysAgoStart = todayStart.minusDays(90);
        LocalDateTime oneYearAgoStart = todayStart.minusDays(365);
        LocalDateTime fiveYearsAgoStart = todayStart.minusYears(5);

        upbitCandle7dRepository.deleteByMarketAndCandleDateTimeUtcBefore(null, sevenDaysAgoStart);
        upbitCandle30dRepository.deleteByMarketAndCandleDateTimeUtcBefore(null, thirtyDaysAgoStart);
        upbitCandle3mRepository.deleteByMarketAndCandleDateTimeUtcBefore(null, ninetyDaysAgoStart);
        upbitCandle1yRepository.deleteByMarketAndCandleDateTimeUtcBefore(null, oneYearAgoStart);
        upbitCandle5yRepository.deleteByMarketAndCandleDateTimeUtcBefore(null, fiveYearsAgoStart);

        log.info("오래된 캔들 데이터 삭제 완료 - 7일: {}, 30일: {}, 3달: {}, 1년: {}, 5년: {}",
                sevenDaysAgoStart, thirtyDaysAgoStart, ninetyDaysAgoStart, oneYearAgoStart, fiveYearsAgoStart);
    }

    private void syncCandlesByType(String type) throws InterruptedException {
        List<String> coins = marketService.getAllTradableCoins().stream()
                .map(TradableCoinDto::getMarket)
                .toList();

        int count = 1;
        for (String coin : coins) {
            log.info("{} 캔들 동기화 - {}/{}", type, count, coins.size());

            switch (type) {
                case "7d" -> sync7d(coin);
                case "30d" -> sync30d(coin);
                case "3m" -> sync3m(coin);
                case "1y" -> sync1y(coin);
                case "5y" -> sync5y(coin);
            }
            count++;
        }
    }

    // 매일 새벽 2시에 실행 (0초 0분 2시 매일)
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void updateDailyCandleData() {
        log.info("매일 캔들 데이터 업데이트 시작");

        try {
            deleteInvalidCandles();     // 유효하지 않은 캔들 데이터 삭제 (상장폐지된 코인 등)
            removeOldCandles();         // 오래된 데이터 삭제

            // 각 타입별 동기화
            syncCandlesByType("7d");
            syncCandlesByType("30d");
            syncCandlesByType("3m");
            syncCandlesByType("1y");
            syncCandlesByType("5y");

            log.info("매일 캔들 데이터 업데이트 완료");
        } catch (Exception e) {
            log.error("캔들 데이터 업데이트 중 오류 발생", e);
            throw new RuntimeException("캔들 데이터 업데이트 실패", e);
        }
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
            log.error("Exception during invalid candles deletion, rolling back", e);
            throw new RuntimeException(e);
        }
    }
}