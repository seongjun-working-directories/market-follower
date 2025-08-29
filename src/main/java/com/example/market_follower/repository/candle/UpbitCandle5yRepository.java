package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle5y;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UpbitCandle5yRepository extends JpaRepository<UpbitCandle5y, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle7d c WHERE c.market NOT IN :markets")
    void deleteByMarketNotIn(@Param("markets") List<String> markets);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle5y c WHERE c.candleDateTimeUtc < :threshold")
    void deleteOlderThan5y(@Param("threshold") LocalDateTime threshold);

    Optional<UpbitCandle5y> findTopByMarketOrderByCandleDateTimeUtcDesc(String market);

    @Query("SELECT c.candleDateTimeUtc FROM UpbitCandle5y c WHERE c.market = :market")
    List<LocalDateTime> findCandleDateTimeUtcByMarket(@Param("market") String market);

    List<UpbitCandle5y> findByMarket(String market);

    void deleteByMarketAndCandleDateTimeUtcNotBetween(String market, LocalDateTime start, LocalDateTime end);

    List<LocalDateTime> findCandleDateTimeUtcByMarketAndDateRange(String market, LocalDateTime start, LocalDateTime end);

    void deleteByMarketAndCandleDateTimeUtcBefore(String market, LocalDateTime dateTime);
}
