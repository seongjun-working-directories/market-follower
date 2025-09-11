package com.example.market_follower.repository.candle;

import com.example.market_follower.model.candle.UpbitCandle30d;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface UpbitCandle30dRepository extends JpaRepository<UpbitCandle30d, Long> {
    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle30d c WHERE c.market NOT IN :markets")
    void deleteByMarketNotIn(@Param("markets") List<String> markets);

    @Modifying
    @Transactional
    @Query("DELETE FROM UpbitCandle30d c WHERE c.market = :market AND (c.candleDateTimeUtc < :start OR c.candleDateTimeUtc > :end)")
    void deleteByMarketAndCandleDateTimeUtcOutsideRange(
            @Param("market") String market,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    @Query("SELECT c.candleDateTimeUtc FROM UpbitCandle30d c WHERE c.market = :market AND c.candleDateTimeUtc BETWEEN :start AND :end")
    List<LocalDateTime> findCandleDateTimeUtcByMarketAndCandleDateTimeUtcBetween(
            @Param("market") String market,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end
    );

    void deleteByMarketAndCandleDateTimeUtcBefore(String market, LocalDateTime dateTime);

    // KRW 마켓만 조회
    List<UpbitCandle30d> findByMarketStartingWith(String prefix);

    // 비KRW 마켓 조회
    @Query("SELECT u FROM UpbitCandle30d u WHERE u.market NOT LIKE :prefix%")
    List<UpbitCandle30d> findByMarketNotStartingWith(@Param("prefix") String prefix);

    // KRW 마켓 + 날짜 조건
    List<UpbitCandle30d> findByMarketStartingWithAndCandleDateTimeKstGreaterThanEqual(String prefix, LocalDateTime dateTime);

    // 비KRW 마켓 + 날짜 조건
    @Query("SELECT u FROM UpbitCandle30d u WHERE u.market NOT LIKE :prefix% AND u.candleDateTimeKst >= :dateTime")
    List<UpbitCandle30d> findByMarketNotStartingWithAndCandleDateTimeKstGreaterThanEqual(@Param("prefix") String prefix, @Param("dateTime") LocalDateTime dateTime);
}
