package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle7d;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UpbitCandle7dRepository extends JpaRepository<UpbitCandle7d, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);
}
