package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle7d;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UpbitCandle7dRepository extends JpaRepository<UpbitCandle7d, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle7d c WHERE c.market NOT IN :markets")
    void deleteByMarketNotIn(@Param("markets") List<String> markets);
}
