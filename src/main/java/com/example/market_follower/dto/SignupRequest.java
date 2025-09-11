package com.example.market_follower.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.*;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "회원가입 요청")
public class SignupRequest {
    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "유효한 이메일 형식이어야 합니다.")
    @Schema(description = "이메일 주소", example = "example@gmail.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String email;

    @NotBlank(message = "이름은 필수입니다.")
    @Schema(description = "사용자 이름", example = "홍길동", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "핸드폰 번호는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "핸드폰 번호 형식이 올바르지 않습니다. 예: 010-1234-5678")
    @Schema(description = "핸드폰 번호 (하이픈 포함)", example = "010-1234-5678", pattern = "^\\d{2,3}-\\d{3,4}-\\d{4}$", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("phone_number")
    private String phoneNumber;

    @NotNull(message = "생일은 필수입니다.")
    @Schema(description = "생년월일", example = "1990-01-15", requiredMode = Schema.RequiredMode.REQUIRED, format = "date")
    private LocalDate birthday;
}