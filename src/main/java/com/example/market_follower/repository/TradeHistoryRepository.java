package com.example.market_follower.repository;

import com.example.market_follower.model.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {
    Optional<List<TradeHistory>> findAllByMemberId(Long memberId);

    // 비관적 락을 사용한 조회 (동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TradeHistory t WHERE t.id = :orderId AND t.memberId = :memberId")
    Optional<TradeHistory> findByIdAndMemberIdWithLock(@Param("orderId") Long orderId, @Param("memberId") Long memberId);

    // 체결 처리용 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TradeHistory t WHERE t.id = :orderId")
    Optional<TradeHistory> findByIdWithLock(@Param("orderId") Long orderId);

    // 기존 DB 스키마에 맞춘 쿼리들
    @Query("SELECT t FROM TradeHistory t WHERE t.status = :status ORDER BY t.requestAt ASC")
    List<TradeHistory> findByStatusOrderByRequestAtAsc(@Param("status") TradeHistory.TradeStatus status);
}
