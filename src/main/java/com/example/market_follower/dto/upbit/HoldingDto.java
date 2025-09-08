package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "사용자 코인 보유 정보 DTO")
public class HoldingDto {

    @Schema(description = "회원 ID", example = "1")
    @JsonProperty("member_id")
    private Long memberId;

    @Schema(description = "마켓 코드", example = "KRW-BTC")
    private String market;

    @Schema(description = "보유 수량", example = "0.53214567")
    private BigDecimal size;

    @Schema(description = "주문 걸린 수량", example = "0.10000000")
    private BigDecimal locked;

    @Schema(description = "평균 매수가", example = "52000000.00000000")
    @JsonProperty("avg_price")
    private BigDecimal avgPrice;
}
