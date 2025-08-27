package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle1y;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UpbitCandle1yRepository extends JpaRepository<UpbitCandle1y, Long> {
}
