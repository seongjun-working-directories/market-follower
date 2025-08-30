package com.example.market_follower.initializer;

import com.example.market_follower.service.CandleService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CandleInitializer implements ApplicationRunner {

    private final CandleService candleService;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        candleService.initializeAllDailyCandleData();
        Thread.sleep(5000);
        candleService.updateAllCandleData();
    }
}

