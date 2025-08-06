package com.example.market_follower.service;

import io.github.cdimascio.dotenv.Dotenv;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class MarketService {
    private final Dotenv dotenv;

    // 추후 삭제 예정
    public void exampleKeys() {
        String accessKey = dotenv.get("API_ACCESS");
        String secretKey = dotenv.get("API_SECRET");
    }
}
