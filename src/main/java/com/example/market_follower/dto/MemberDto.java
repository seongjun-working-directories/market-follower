package com.example.market_follower.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "회원 정보 DTO")
public class MemberDto {

    @Schema(description = "회원 ID", example = "1")
    private Long id;

    @Schema(description = "회원 이름", example = "홍길동")
    private String name;

    @Schema(description = "이메일 주소", example = "hong@example.com")
    private String email;

    @Schema(description = "전화번호", example = "010-1234-5678")
    @JsonProperty("phone_number")
    private String phoneNumber;

    @Schema(description = "생년월일", example = "1990-01-01")
    private LocalDate birthday;

    @Schema(description = "활성화 상태", example = "true")
    @Builder.Default
    private Boolean activated = true;
}