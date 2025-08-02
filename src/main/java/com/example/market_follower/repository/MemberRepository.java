package com.example.market_follower.repository;

import com.example.market_follower.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member, Long> {
    // 이메일에 해당하는 사용자를 반환하는 메서드
    Optional<Member> findByEmail(String email);

    // 이메일이 존재하는지 여부를 반환하는 메서드
    boolean existsByEmail(String email);
}
