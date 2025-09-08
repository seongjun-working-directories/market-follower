package com.example.market_follower.model;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "trade_history")
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TradeHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Long memberId;

    @Column(length = 20, nullable = false)
    private String market;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Side side; // BUY, SELL

    @Column(precision = 30, scale = 8, nullable = false)
    private BigDecimal price;

    @Column(precision = 30, scale = 8, nullable = false)
    private BigDecimal size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TradeStatus status; // WAITING, SUCCESS, FAILED, CANCELLED

    @Builder.Default
    @Column(name = "request_at", nullable = false, updatable = false)
    private LocalDateTime requestAt = LocalDateTime.now();

    @Column(name = "matched_at")
    private LocalDateTime matchedAt;

    public enum Side {
        BUY, SELL
    }

    public enum TradeStatus {
        WAITING, SUCCESS, FAILED, CANCELLED
    }
}
