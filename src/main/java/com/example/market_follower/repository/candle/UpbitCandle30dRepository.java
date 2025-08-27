package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle30d;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;

public interface UpbitCandle30dRepository extends JpaRepository<UpbitCandle30d, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);
}
