package com.example.market_follower.service;

import com.example.market_follower.dto.upbit.TradableCoinDto;
import com.example.market_follower.dto.upbit.candle.*;
import com.example.market_follower.model.candle.*;
import com.example.market_follower.repository.candle.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class CandleService {
    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;
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
        upbitCandle7dRepository.deleteByMarketAndCandleDateTimeUtcOutsideRange(
                coin, sevenDaysAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle7dRepository.findCandleDateTimeUtcByMarketAndCandleDateTimeUtcBetween(
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
        upbitCandle30dRepository.deleteByMarketAndCandleDateTimeUtcOutsideRange(
                coin, thirtyDaysAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle30dRepository.findCandleDateTimeUtcByMarketAndCandleDateTimeUtcBetween(
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
        upbitCandle3mRepository.deleteByMarketAndCandleDateTimeUtcOutsideRange(
                coin, ninetyDaysAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle3mRepository.findCandleDateTimeUtcByMarketAndCandleDateTimeUtcBetween(
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
        upbitCandle1yRepository.deleteByMarketAndCandleDateTimeUtcOutsideRange(
                coin, oneYearAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle1yRepository.findCandleDateTimeUtcByMarketAndCandleDateTimeUtcBetween(
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
        upbitCandle5yRepository.deleteByMarketAndCandleDateTimeUtcOutsideRange(
                coin, fiveYearsAgoStart, todayStart);

        // 현재 DB에 있는 시간들 조회 (범위 내)
        Set<LocalDateTime> existingDates = new HashSet<>(
                upbitCandle5yRepository.findCandleDateTimeUtcByMarketAndCandleDateTimeUtcBetween(
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
    public void updateAllCandleData() {
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

    // 전체 캔들 데이터를 리턴
    public Map<String, Object> getAllCandleData() throws IOException {
        File dir = new File("src/main/resources/candles");
        if (!dir.exists()) {
            dir.mkdirs(); // candles 폴더가 없으면 폴더 생성
        }

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File file = new File(dir, "candles_" + today + ".json");
        Map<String, Object> data;

        if (!file.exists()) {
            data = new HashMap<>();
            data.put("upbit_candle_7d", jdbcTemplate.queryForList("SELECT * FROM upbit_candle_7d"));
            data.put("upbit_candle_30d", jdbcTemplate.queryForList("SELECT * FROM upbit_candle_30d"));
            data.put("upbit_candle_3m", jdbcTemplate.queryForList("SELECT * FROM upbit_candle_3m"));
            data.put("upbit_candle_1y", jdbcTemplate.queryForList("SELECT * FROM upbit_candle_1y"));
            data.put("upbit_candle_5y", jdbcTemplate.queryForList("SELECT * FROM upbit_candle_5y"));

            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, data);
            log.info("{} 파일 생성 완료", file.getName());
        } else {
            data = objectMapper.readValue(
                    file, new TypeReference<Map<String, Object>>() {}
            );
            log.info("{} 파일 읽기 완료", file.getName());
        }

        return data;
    }

    // 특정 시점 이후의 캔들 데이터를 리턴
    public Map<String, Object> getAllCandleDataSince(String period) throws IOException {
        File dir = new File("src/main/resources/candles");
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        File file = new File(dir, "candles_" + today + ".json");

        if (!file.exists()) {
            // 오늘 JSON 파일이 없으면 기존 getAllCandleData로 생성
            getAllCandleData();
        }

        Map<String, Object> allData = objectMapper.readValue(
                file, new TypeReference<Map<String, Object>>() {}
        );

        LocalDate fromDate = LocalDate.parse(period, DateTimeFormatter.ISO_DATE);
        Map<String, Object> filteredData = new HashMap<>();

        for (Map.Entry<String, Object> entry : allData.entrySet()) {
            List<Map<String, Object>> candles = (List<Map<String, Object>>) entry.getValue();

            List<Map<String, Object>> filteredCandles = candles.stream()
                    .filter(c -> {
                        // JSON 컬럼에 맞춰서 날짜 추출
                        String candleDateStr = (String) c.get("candle_date_time_utc");
                        LocalDate candleDate = LocalDate.parse(
                                candleDateStr.substring(0, 10), DateTimeFormatter.ISO_DATE
                        );
                        return !candleDate.isBefore(fromDate); // fromDate 포함 이후
                    })
                    .toList();

            filteredData.put(entry.getKey(), filteredCandles);
        }

        return filteredData;
    }

    // 특정 코인의 1 Day 캔들 반환
    public List<Map<String, Object>> getDailyCandleData(String market) {
        try {
            String redisKey = "upbit:daily:" + market;
            String jsonData = redisTemplate.opsForValue().get(redisKey);

            if (jsonData == null) {
                log.error("Redis에 데이터가 없음 - 시스템 오류 의심: {}", market);
                throw new RuntimeException("1Day 캔들 데이터를 찾을 수 없습니다: " + market);
            }

            List<Map<String, Object>> candles = objectMapper.readValue(
                    jsonData, new TypeReference<List<Map<String, Object>>>() {}
            );

            log.info("Redis에서 1Day 캔들 반환 - {} : {}개", market, candles.size());
            return candles;
        } catch (Exception e) {
            log.error("Redis에서 1Day 캔들 조회 실패: {}", market, e);
            return new ArrayList<>();
        }
    }

    // 모든 코인에 대해 00시부터 현재까지 5분 캔들을 모두 가져와서 Redis에 저장
    public void initializeAllDailyCandleData() {
        log.info("모든 코인 Redis 1Day 캔들 초기화 시작");

        try {
            List<String> coins = marketService.getAllTradableCoins().stream()
                    .map(TradableCoinDto::getMarket)
                    .toList();

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime todayStart = now.toLocalDate().atStartOfDay();

            // 현재 시간을 5분 단위로 내림
            int minute = now.getHour() * 60 + now.getMinute();
            int roundedTotalMinutes = (minute / 5) * 5;
            LocalDateTime currentRoundedTime = now
                    .withHour(0).withMinute(0).withSecond(0).withNano(0).plusMinutes(roundedTotalMinutes);

            int count = 1;
            for (String market : coins) {
                log.info("Redis 초기화 진행 - {}/{} : {}", count, coins.size(), market);
                setDailyCandleData(market, todayStart, currentRoundedTime);
                count++;
                Thread.sleep(150); // API 호출 제한 고려
            }

            log.info("모든 코인 Redis 1Day 캔들 초기화 완료 - {}개 코인", coins.size());

        } catch (Exception e) {
            log.error("Redis 1Day 캔들 초기화 실패", e);
            throw new RuntimeException("Redis 초기화 실패", e);
        }
    }

    // 특정 코인의 00시부터 현재까지 5분 캔들을 모두 가져와서 Redis에 저장
    private void setDailyCandleData(String market, LocalDateTime todayStart, LocalDateTime currentTime) {
        try {
            // 00시부터 현재시간까지 필요한 5분 캔들 개수 계산
            long minutesFromStart = java.time.Duration.between(todayStart, currentTime).toMinutes();
            int candleCount = (int) (minutesFromStart / 5);

            if (candleCount <= 0) {
                log.warn("유효하지 않은 캔들 개수: {} for {}", candleCount, market);
                return;
            }

            // Upbit API에서 5분 캔들 데이터 가져오기 (최대 200개씩)
            List<Map<String, Object>> allCandles = new ArrayList<>();
            int remainingCount = candleCount;
            LocalDateTime toTime = currentTime;

            while (remainingCount > 0) {
                // 캔들 요청 최대 개수가 200임을 반영
                int requestCount = Math.min(remainingCount, 200);
                String url = "https://api.upbit.com/v1/candles/minutes/5?market=" + market
                        + "&count=" + requestCount;

                // 처음엔 현재 시간부터 시작, 이후에는 이전 캔들의 시간으로 갱신
                if (!toTime.equals(currentTime)) {
                    String toTimeStr = toTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"));
                    url += "&to=" + toTimeStr;
                }
                log.info("API 요청: {} (count: {})", market, requestCount);

                UpbitCandle1dDto[] dtos = restTemplate.getForObject(url, UpbitCandle1dDto[].class);

                if (dtos == null || dtos.length == 0) { break; }
                log.info("UpbitCandle1dDto[] 의 크기: {}", dtos != null ? dtos.length : 0);

                List<UpbitCandle1dDto> batch = Arrays.asList(dtos);
                Collections.reverse(batch);

                for (UpbitCandle1dDto dto : batch) {
                    LocalDateTime candleTime = parseDateTime(dto.getCandleDateTimeKst());

                    if (candleTime != null && candleTime.toLocalDate().equals(todayStart.toLocalDate())) {
                        Map<String, Object> candleMap = convertUpbitCandle1dDtoToMap(dto);
                        allCandles.add(candleMap);
                    } else {
                        log.info("캔들 제외: market={}, candleTime={}, todayStart={}",
                                dto.getMarket(),
                                candleTime != null ? candleTime.toString() : "null",
                                todayStart.toLocalDate());
                    }
                }

                // 200개 넘는 경우에만 대응하면 됨
                remainingCount -= 200;
                toTime = toTime.minusMinutes(1000);

                // 오늘 00시 이전으로 가면 중단
                if (toTime.isBefore(todayStart)) {
                    break;
                }

                Thread.sleep(200); // API 호출 제한 고려
            }

            // 시간순 정렬 (오래된 것부터)
            allCandles.sort((a, b) -> {
                String timeA = (String) a.get("candle_date_time_kst");
                String timeB = (String) b.get("candle_date_time_kst");
                return timeA.compareTo(timeB);
            });

            // Redis에 저장
            if (!allCandles.isEmpty()) {
                String redisKey = "upbit:daily:" + market;
                String jsonData = objectMapper.writeValueAsString(allCandles);
                redisTemplate.opsForValue().set(redisKey, jsonData);

                log.info("Redis 저장 완료 - {} : {}개 캔들 ({}~{})",
                        market, allCandles.size(),
                        allCandles.get(0).get("candle_date_time_kst"),
                        allCandles.get(allCandles.size() - 1).get("candle_date_time_kst"));
            }
        }  catch (Exception e) {
            log.error("전체 일일 캔들 데이터 갱신 실패: {}", market, e);
        }
    }

    // UpbitCandle1dDto 를 Map<String, Object> 형태로 변환
    private Map<String, Object> convertUpbitCandle1dDtoToMap(UpbitCandle1dDto dto) {
        Map<String, Object> map = new HashMap<>();
        map.put("market", dto.getMarket());
        map.put("candle_date_time_utc", dto.getCandleDateTimeUtc());
        map.put("candle_date_time_kst", dto.getCandleDateTimeKst());
        map.put("opening_price", dto.getOpeningPrice());
        map.put("high_price", dto.getHighPrice());
        map.put("low_price", dto.getLowPrice());
        map.put("trade_price", dto.getTradePrice());
        map.put("timestamp", dto.getTimestamp());
        map.put("candle_acc_trade_price", dto.getCandleAccTradePrice());
        map.put("candle_acc_trade_volume", dto.getCandleAccTradeVolume());
        map.put("unit", dto.getUnit());
        return map;
    }

    // 매일 23:59 에 Redis 1 Day 캔들 정리
    @Scheduled(cron = "0 59 23 * * ?")
    public void cleanupDailyCandleData() {
        log.info("Redis 1Day 캔들 데이터 정리 시작");

        try {
            List<String> coins = marketService.getAllTradableCoins().stream()
                    .map(TradableCoinDto::getMarket)
                    .toList();

            for (String coin : coins) {
                String redisKey = "upbit:daily:" + coin;
                redisTemplate.delete(redisKey);
            }

            log.info("Redis 1Day 캔들 데이터 정리 완료 - {}개 코인", coins.size());
        } catch (Exception e) {
            log.error("Redis 1Day 캔들 데이터 정리 실패", e);
        }
    }

    // 5분마다 실행되는 스케줄러 - 모든 코인의 최신 5분 캔들을 Redis에 저장
    @Scheduled(initialDelay = 300000, fixedRate = 300000) // 5분마다 실행 (300,000ms)
    public void updateAllDailyCandleData() {
        log.info("Redis 1Day 캔들 데이터 업데이트 시작");

        try {
            List<String> coins = marketService.getAllTradableCoins().stream()
                    .map(TradableCoinDto::getMarket)
                    .toList();

            for (String coin : coins) {
                updateDailyCandleData(coin);
                Thread.sleep(100); // API 호출 제한 고려
            }

            log.info("Redis 1Day 캔들 데이터 업데이트 완료");
        } catch (Exception e) {
            log.error("Redis 1Day 캔들 데이터 업데이트 중 오류 발생", e);
        }
    }

    // 특정 코인의 최신 5분 캔들을 Redis에 저장
    private void updateDailyCandleData(String coin) {
        try {
            // 최신 5분 캔들 1개만 가져오기
            String url = "https://api.upbit.com/v1/candles/minutes/5?market=" + coin + "&count=1";
            UpbitCandle1dDto[] dtos = restTemplate.getForObject(url, UpbitCandle1dDto[].class);

            if (dtos != null && dtos.length > 0) {
                UpbitCandle1dDto dto = dtos[0];

                // Redis에서 기존 데이터 가져오기
                String redisKey = "upbit:daily:" + coin;
                String existingData = redisTemplate.opsForValue().get(redisKey);

                List<Map<String, Object>> candles;
                if (existingData != null) {
                    candles = objectMapper.readValue(existingData, new TypeReference<List<Map<String, Object>>>() {});
                } else {
                    candles = new ArrayList<>();
                }

                // 새 캔들 추가 (중복 체크는 시간으로)
                Map<String, Object> newCandle = convertUpbitCandle1dDtoToMap(dto);
                String newCandleTime = (String) newCandle.get("candle_date_time_kst");

                // 마지막 캔들과 시간이 다르면 추가
                if (candles.isEmpty() ||
                        !newCandleTime.equals(candles.get(candles.size() - 1).get("candle_date_time_kst"))) {
                    candles.add(newCandle);
                }

                // Redis에 다시 저장
                String jsonData = objectMapper.writeValueAsString(candles);
                redisTemplate.opsForValue().set(redisKey, jsonData);
            }
        } catch (Exception e) {
            log.error("5분봉 캔들 데이터 업데이트 실패: {}", coin, e);
        }
    }
}