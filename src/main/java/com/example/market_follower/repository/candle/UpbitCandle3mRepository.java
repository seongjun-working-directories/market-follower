package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle3m;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UpbitCandle3mRepository extends JpaRepository<UpbitCandle3m, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);
}
