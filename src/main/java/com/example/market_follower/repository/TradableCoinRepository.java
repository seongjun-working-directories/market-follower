package com.example.market_follower.repository;

import com.example.market_follower.model.TradableCoin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradableCoinRepository extends JpaRepository<TradableCoin, String> {
    @Query("SELECT t.market FROM TradableCoin t")
    List<String> findAllMarkets();

    void deleteByMarketNotIn(List<String> markets);
}
