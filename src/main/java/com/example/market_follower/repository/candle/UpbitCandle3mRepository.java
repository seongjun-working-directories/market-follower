package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle3m;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface UpbitCandle3mRepository extends JpaRepository<UpbitCandle3m, Long> {
    boolean existsByMarketAndCandleDateTimeUtc(String market, LocalDateTime candleDateTimeUtc);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle7d c WHERE c.market NOT IN :markets")
    void deleteByMarketNotIn(@Param("markets") List<String> markets);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle3m c WHERE c.candleDateTimeUtc < :threshold")
    void deleteOlderThan3m(@Param("threshold") LocalDateTime threshold);

    Optional<UpbitCandle3m> findTopByMarketOrderByCandleDateTimeUtcDesc(String market);

    @Query("SELECT c.candleDateTimeUtc FROM UpbitCandle3m c WHERE c.market = :market")
    List<LocalDateTime> findCandleDateTimeUtcByMarket(@Param("market") String market);
}
