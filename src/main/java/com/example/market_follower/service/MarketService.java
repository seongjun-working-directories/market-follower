package com.example.market_follower.service;

import com.example.market_follower.dto.upbit.TradableCoinDto;
import com.example.market_follower.dto.upbit.UpbitMarketApiResponse;
import com.example.market_follower.dto.upbit.UpbitTickerDto;
import com.example.market_follower.model.TradableCoin;
import com.example.market_follower.repository.TradableCoinRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.cdimascio.dotenv.Dotenv;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {
    private final TradableCoinRepository tradableCoinRepository;
    private final Dotenv dotenv;
    private final ObjectMapper objectMapper;    // JSON 파싱
    private final RestTemplate restTemplate = new RestTemplate();   // HTTP 호출
    private final StringRedisTemplate redisTemplate;

    private String uuidGenerator() {
        return UUID.randomUUID().toString();
    }

    private String sha512Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-512");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    private TradableCoin toEntity(TradableCoinDto dto) {
        return TradableCoin.builder()
                .market(dto.getMarket())
                .koreanName(dto.getKoreanName())
                .englishName(dto.getEnglishName())
                .isWarning(dto.getIsWarning())
                .isCautionPriceFluctuations(dto.getIsCautionPriceFluctuations())
                .isCautionTradingVolumeSoaring(dto.getIsCautionTradingVolumeSoaring())
                .isCautionDepositAmountSoaring(dto.getIsCautionDepositAmountSoaring())
                .isCautionGlobalPriceDifferences(dto.getIsCautionGlobalPriceDifferences())
                .isCautionConcentrationOfSmallAccounts(dto.getIsCautionConcentrationOfSmallAccounts())
                .updatedAt(LocalDateTime.now())
                .build();
    }

    // 추후 삭제 예정
    public void exampleKeys() {
        String accessKey = dotenv.get("API_ACCESS");
        String secretKey = dotenv.get("API_SECRET");
    }

    @Transactional
    public void updateTradableCoinsInDb() {
        List<TradableCoinDto> dtos = getAllTradableCoins();  // API 호출 + DTO 반환

        List<TradableCoin> entities = dtos.stream()
                .map(this::toEntity)
                .collect(Collectors.toList());

        tradableCoinRepository.saveAll(entities);
        log.info("Updated tradable_coin table with {} entries", entities.size());

        List<String> dtoMarkets = dtos.stream()
            .map(TradableCoinDto::getMarket)
            .toList();
        
        tradableCoinRepository.deleteByMarketNotIn(dtoMarkets);
        log.info("Deleted tradable_coin entries not in the latest API response");
    }

    public List<TradableCoinDto> getAllTradableCoins() {
        try {
            // 1. Upbit 마켓 목록 API 호출
            String url = "https://api.upbit.com/v1/market/all?isDetails=true";
            String jsonString = restTemplate.getForObject(url, String.class);

            List<UpbitMarketApiResponse> apiResponses =
                    objectMapper.readValue(jsonString, new TypeReference<List<UpbitMarketApiResponse>>() {});

            return apiResponses.stream()
                    .map(r -> TradableCoinDto.builder()
                            .market(r.getMarket())
                            .koreanName(r.getKoreanName())
                            .englishName(r.getEnglishName())
                            .isWarning(r.getMarketEvent().getWarning())
                            .isCautionPriceFluctuations(r.getMarketEvent().getCaution().getPriceFluctuations())
                            .isCautionTradingVolumeSoaring(r.getMarketEvent().getCaution().getTradingVolumeSoaring())
                            .isCautionDepositAmountSoaring(r.getMarketEvent().getCaution().getDepositAmountSoaring())
                            .isCautionGlobalPriceDifferences(r.getMarketEvent().getCaution().getGlobalPriceDifferences())
                            .isCautionConcentrationOfSmallAccounts(r.getMarketEvent().getCaution().getConcentrationOfSmallAccounts())
                            .build()
                    )
                    .collect(Collectors.toList());

        } catch (Exception e) {
            log.error("Failed to fetch tradable coins from Upbit API", e);
            throw new RuntimeException("Failed to fetch tradable coins", e);
        }
    }

    public UpbitTickerDto getTicker(String market) {
        try {
            String key = "upbit:ticker:" + market;
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) return null;
            return objectMapper.readValue(json, UpbitTickerDto.class);
        } catch(Exception e) {
            throw new RuntimeException("Error fetching ticker for market: " + market, e);
        }
    }

    public List<UpbitTickerDto> getAllTickers() {
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

            return tickers;
        } catch (Exception e) {
            throw new RuntimeException("Error fetching tickers from Redis", e);
        }
    }
}
