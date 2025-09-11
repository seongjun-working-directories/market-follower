package com.example.market_follower.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GoogleUserInfoDto {
    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;

    @Schema(description = "회원 이름", example = "홍길동")
    private String name;
}
