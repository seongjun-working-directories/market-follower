package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle30d;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UpbitCandle30dRepository extends JpaRepository<UpbitCandle30d, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle7d c WHERE c.market NOT IN :markets")
    void deleteByMarketNotIn(@Param("markets") List<String> markets);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle30d c WHERE c.market = :market AND (c.candleDateTimeUtc < :start OR c.candleDateTimeUtc > :end)")
    void deleteByMarketAndCandleDateTimeUtcOutsideRange(
            @Param("market") String market,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    List<LocalDateTime> findCandleDateTimeUtcByMarketAndDateRange(String market, LocalDateTime start, LocalDateTime end);

    void deleteByMarketAndCandleDateTimeUtcBefore(String market, LocalDateTime dateTime);
}
