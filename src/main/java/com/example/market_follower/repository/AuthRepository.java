package com.example.market_follower.repository;

import com.example.market_follower.model.Auth;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthRepository extends JpaRepository<Auth, Long> {
}
