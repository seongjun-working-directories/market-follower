package com.example.market_follower.initializer;

import com.example.market_follower.service.CandleService;
import com.example.market_follower.service.MarketService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class CandleInitializer implements ApplicationRunner {

    private final CandleService candleService;
    private final MarketService marketService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        marketService.updateTradableCoinsInDb();
        Thread.sleep(5000);

        candleService.initializeAllDailyCandleData();
        Thread.sleep(5000);

        int currentHour = LocalDateTime.now().getHour();
        if (currentHour <= 7) {
            candleService.updateAllCandleData();
        } else {
            log.info(currentHour + "시는 실행 범위(9~16시)가 아니므로 updateAllCandleData 생략");
        }
    }
}
