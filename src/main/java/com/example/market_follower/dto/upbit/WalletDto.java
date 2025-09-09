package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "사용자 지갑 정보 DTO")
public class WalletDto {

    @Schema(description = "지갑 고유 ID", example = "1")
    @JsonProperty("wallet_id")
    private Long walletId;

    @Schema(description = "소유자 회원 ID", example = "123")
    @JsonProperty("member_id")
    private Long memberId;

    @Schema(
            description = "사용 가능한 KRW 잔액 (즉시 매수 주문에 사용 가능)",
            example = "1500000.00",
            minimum = "0"
    )
    @JsonProperty("balance")
    private BigDecimal balance;

    @Schema(
            description = "주문 대기 중인 잠금 자금 (매수 주문 시 일시적으로 잠긴 KRW)",
            example = "250000.00",
            minimum = "0"
    )
    @JsonProperty("locked")
    private BigDecimal locked;
}