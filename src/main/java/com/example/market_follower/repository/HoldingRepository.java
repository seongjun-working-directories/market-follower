package com.example.market_follower.repository;

import com.example.market_follower.model.Holding;
import com.example.market_follower.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, Long> {
    Optional<Holding> findByMemberAndMarket(Member member, String market);
    Optional<List<Holding>> findAllByMember(Member member);
}
