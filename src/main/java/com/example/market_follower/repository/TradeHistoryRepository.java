package com.example.market_follower.repository;

import com.example.market_follower.model.Member;
import com.example.market_follower.model.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import jakarta.persistence.LockModeType;

import java.util.List;
import java.util.Optional;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {

    // member 객체로 모든 거래 조회
    Optional<List<TradeHistory>> findAllByMember(Member member);

    // 비관적 락을 사용한 조회 (동시성 제어)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TradeHistory t WHERE t.id = :orderId AND t.member = :member")
    Optional<TradeHistory> findByIdAndMemberWithLock(@Param("orderId") Long orderId, @Param("member") Member member);

    // 체결 처리용 락
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT t FROM TradeHistory t WHERE t.id = :orderId")
    Optional<TradeHistory> findByIdWithLock(@Param("orderId") Long orderId);

    // WAITING 상태인 주문 조회
    @Query("SELECT t FROM TradeHistory t WHERE t.status = :status ORDER BY t.requestAt ASC")
    List<TradeHistory> findByStatusOrderByRequestAtAsc(@Param("status") TradeHistory.TradeStatus status);
}
