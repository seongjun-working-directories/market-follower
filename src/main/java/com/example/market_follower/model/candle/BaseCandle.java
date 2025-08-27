package com.example.market_follower.model.candle;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public abstract class BaseCandle {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "market", nullable = false, length = 20)
    private String market;

    @Column(name = "candle_date_time_utc", nullable = false)
    private LocalDateTime candleDateTimeUtc;

    @Column(name = "candle_date_time_kst", nullable = false)
    private LocalDateTime candleDateTimeKst;

    @Column(name = "opening_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal openingPrice;

    @Column(name = "high_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal highPrice;

    @Column(name = "low_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal lowPrice;

    @Column(name = "trade_price", nullable = false, precision = 20, scale = 8)
    private BigDecimal tradePrice;

    @Column(name = "timestamp", nullable = false)
    private Long timestamp;

    @Column(name = "candle_acc_trade_price", nullable = false, precision = 30, scale = 8)
    private BigDecimal candleAccTradePrice;

    @Column(name = "candle_acc_trade_volume", nullable = false, precision = 30, scale = 8)
    private BigDecimal candleAccTradeVolume;

    @Column(name = "unit", nullable = false)
    private Integer unit;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        LocalDateTime now = LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}