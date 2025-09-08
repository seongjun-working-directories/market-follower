package com.example.market_follower.repository;

import com.example.market_follower.model.Member;
import com.example.market_follower.model.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    // 멤버 ID로 지갑을 찾는 메서드
    Optional<Wallet> findByMember(Member member);
}
