package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle30d;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpbitCandle30dRepository extends JpaRepository<UpbitCandle30d, Long> {
}
