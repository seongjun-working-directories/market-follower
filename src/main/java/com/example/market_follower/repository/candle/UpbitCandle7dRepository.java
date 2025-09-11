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
    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle7d c WHERE c.market NOT IN :markets")
    void deleteByMarketNotIn(@Param("markets") List<String> markets);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle7d c WHERE c.market = :market AND (c.candleDateTimeUtc < :start OR c.candleDateTimeUtc > :end)")
    void deleteByMarketAndCandleDateTimeUtcOutsideRange(
            @Param("market") String market,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT c.candleDateTimeUtc FROM UpbitCandle7d c WHERE c.market = :market AND c.candleDateTimeUtc BETWEEN :start AND :end")
    List<LocalDateTime> findCandleDateTimeUtcByMarketAndCandleDateTimeUtcBetween(
            @Param("market") String market,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end);

    void deleteByMarketAndCandleDateTimeUtcBefore(String market, LocalDateTime dateTime);

    // KRW 마켓만 조회
    List<UpbitCandle7d> findByMarketStartingWith(String prefix);

    // 비KRW 마켓 조회
    @Query("SELECT u FROM UpbitCandle7d u WHERE u.market NOT LIKE :prefix%")
    List<UpbitCandle7d> findByMarketNotStartingWith(@Param("prefix") String prefix);

    // KRW 마켓 + 날짜 조건
    List<UpbitCandle7d> findByMarketStartingWithAndCandleDateTimeKstGreaterThanEqual(String prefix, LocalDateTime dateTime);

    // 비KRW 마켓 + 날짜 조건
    @Query("SELECT u FROM UpbitCandle7d u WHERE u.market NOT LIKE :prefix% AND u.candleDateTimeKst >= :dateTime")
    List<UpbitCandle7d> findByMarketNotStartingWithAndCandleDateTimeKstGreaterThanEqual(@Param("prefix") String prefix, @Param("dateTime") LocalDateTime dateTime);
}
