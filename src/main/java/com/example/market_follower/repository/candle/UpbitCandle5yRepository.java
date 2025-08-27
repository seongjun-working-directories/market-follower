package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle5y;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UpbitCandle5yRepository extends JpaRepository<UpbitCandle5y, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);
}
