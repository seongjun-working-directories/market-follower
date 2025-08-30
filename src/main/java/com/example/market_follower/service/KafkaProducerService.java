package com.example.market_follower.service;

import com.example.market_follower.dto.upbit.UpbitTickerDto;
import com.example.market_follower.model.TradableCoin;
import com.example.market_follower.repository.TradableCoinRepository;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final RestTemplate restTemplate = new RestTemplate();
    private final String topic = "upbit-ticker-topic";
    private final TradableCoinRepository tradableCoinRepository;
    private final MarketService marketService;

    private volatile boolean isUpdating = false;

    @Scheduled(cron = "0 0 1 * * *")  // 매일 1시 정각 실행 (초, 분, 시, 일, 월, 요일)
    public void scheduledUpdateTradableCoins() {
        if (isUpdating) {
            log.info("DB update already in progress, skipping scheduled update.");
            return;
        }
        isUpdating = true;
        try {
            log.info("Scheduled task started: Updating tradable coins");
            marketService.updateTradableCoinsInDb();
            log.info("Scheduled task finished: tradable coins updated");
        } finally {
            isUpdating = false;
        }
    }

    @Scheduled(initialDelay = 120000, fixedDelay = 10000)   // 2분 후 첫 실행, 10초마다 실행
    public void fetchAndSendTickerData() {
        if (isUpdating) {
            log.info("DB update in progress, skipping ticker fetch.");
            return;  // DB 업데이트 중이면 바로 종료
        }

        try {
            List<String> markets = tradableCoinRepository.findAll()
                    .stream()
                    .map(TradableCoin::getMarket)
                    .toList();

            if (markets.isEmpty()) {
                log.warn("No tradable coins found in DB, updating market list first...");
                if (!isUpdating) {  // 혹시 중복 호출 방지용 플래그 확인
                    isUpdating = true;
                    try {
                        marketService.updateTradableCoinsInDb();  // 즉시 업데이트 실행
                    } finally {
                        isUpdating = false;
                    }
                } else {
                    log.info("Update already in progress, skipping redundant update call.");
                }
                return;  // 업데이트 끝나면 다음 스케줄 주기까지 대기
            }

            String marketsParam = String.join(",", markets);
            String url = "https://api.upbit.com/v1/ticker?markets=" + marketsParam;
            String jsonResponse = restTemplate.getForObject(url, String.class);

            List<UpbitTickerDto> tickerList = objectMapper.readValue(jsonResponse, new TypeReference<List<UpbitTickerDto>>() {});
            String message = objectMapper.writeValueAsString(tickerList);
            kafkaTemplate.send(topic, message);

            log.info("Sent ticker data for {} coins to Kafka", tickerList.size());
        } catch (Exception e) {
            log.error("Error fetching or sending ticker data", e);
        }
    }
}
