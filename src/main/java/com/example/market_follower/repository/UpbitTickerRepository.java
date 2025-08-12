package com.example.market_follower.repository;

import com.example.market_follower.model.UpbitTicker;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UpbitTickerRepository extends JpaRepository<UpbitTicker, Long> {

}
