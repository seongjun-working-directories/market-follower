package com.example.market_follower.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Auth {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Member 엔티티와 다대일 관계 (하나의 회원이 여러 권한 가질 수 있음)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 권한 이름 예: ROLE_USER, ROLE_ADMIN 등
    @Column(nullable = false)
    private String role;
}
