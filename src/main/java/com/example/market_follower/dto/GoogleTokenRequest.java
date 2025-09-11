package com.example.market_follower.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "구글 액세스 토큰 요청")
public class GoogleTokenRequest {
    @NotBlank(message = "액세스 토큰은 필수입니다.")
    @Schema(description = "구글 OAuth2 액세스 토큰", example = "ya29.a0AfH6SMC_example_token", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty("access_token")
    private String accessToken;
}
