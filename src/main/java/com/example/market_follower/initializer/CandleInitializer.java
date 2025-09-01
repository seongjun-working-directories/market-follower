package com.example.market_follower.initializer;

import com.example.market_follower.service.CandleService;
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

    @Override
    public void run(ApplicationArguments args) throws Exception {
        candleService.initializeAllDailyCandleData();
        Thread.sleep(5000);

        int currentHour = LocalDateTime.now().getHour();
        if (currentHour <= 18) {
            candleService.updateAllCandleData();
        } else {
            log.info(currentHour + "시는 실행 범위(0~18시)가 아니므로 updateAllCandleData 생략");
        }
    }
}

