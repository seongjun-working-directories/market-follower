package com.example.market_follower.repository;

import com.example.market_follower.model.TradeHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TradeHistoryRepository extends JpaRepository<TradeHistory, Long> {
    // 멤버 ID로 거래 내역을 찾는 메서드
    Optional<TradeHistory> findByMemberId(Long memberId);
}
