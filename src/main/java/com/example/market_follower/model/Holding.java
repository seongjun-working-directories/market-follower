package com.example.market_follower.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "holding")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(length = 20, nullable = false)
    private String market;

    @Column(precision = 30, scale = 8, nullable = false)
    private BigDecimal size;      // 보유 수량

    @Column(precision = 30, scale = 8, nullable = false)
    private BigDecimal locked;    // 주문에 묶인 수량

    @Column(name = "avg_price", precision = 30, scale = 8, nullable = false)
    private BigDecimal avgPrice;  // 평균 매수가
}
