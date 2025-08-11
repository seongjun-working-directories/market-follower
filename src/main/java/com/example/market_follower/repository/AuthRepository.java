package com.example.market_follower.repository;

import com.example.market_follower.model.Auth;
import com.example.market_follower.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AuthRepository extends JpaRepository<Auth, Long> {
    List<Auth> findAllByMember(Member member);
}
