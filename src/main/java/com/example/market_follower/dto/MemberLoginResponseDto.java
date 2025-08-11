package com.example.market_follower.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberLoginResponseDto {
    // "REGISTERED" or "NOT_REGISTERED"
    private String status;

    private String email;

    private String name;

    // 등록된 유저일 경우에만 존재
    private Long memberId;

    private String jwtToken;
}