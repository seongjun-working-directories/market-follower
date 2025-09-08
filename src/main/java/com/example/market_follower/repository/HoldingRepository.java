package com.example.market_follower.repository;

import com.example.market_follower.model.Holding;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByMemberIdAndMarket(Long memberId, String market);
}
