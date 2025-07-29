package com.example.market_follower.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    @Column(unique = true)
    private String email;

    private String phoneNumber;

    private LocalDate birthday;

    private LocalDateTime createdAt;

    private LocalDateTime lastLoginAt;

    // 저장되기 직전에 자동으로 호출
    @PrePersist
    public void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
