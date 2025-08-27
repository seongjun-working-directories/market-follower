package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle3m;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpbitCandle3mRepository extends JpaRepository<UpbitCandle3m, Long> {
}
