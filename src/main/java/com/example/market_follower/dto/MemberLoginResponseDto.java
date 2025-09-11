package com.example.market_follower.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 로그인 응답 DTO")
public class MemberLoginResponseDto {

    @Schema(description = "로그인 상태", example = "REGISTERED", allowableValues = {"REGISTERED", "NOT_REGISTERED"})
    private String status;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;

    @Schema(description = "회원 이름", example = "홍길동")
    private String name;

    @Schema(description = "회원 ID (등록된 유저일 경우에만 존재)", example = "1")
    @JsonProperty("member_id")
    private Long memberId;

    @Schema(description = "JWT 토큰", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    @JsonProperty("jwt_token")
    private String jwtToken;
}