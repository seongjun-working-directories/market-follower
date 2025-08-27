package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle1y;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UpbitCandle1yRepository extends JpaRepository<UpbitCandle1y, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);
}
