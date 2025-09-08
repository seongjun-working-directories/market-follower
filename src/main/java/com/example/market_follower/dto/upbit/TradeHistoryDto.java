package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Schema(description = "거래 내역 DTO")
public class TradeHistoryDto {

    @Schema(description = "거래 ID", example = "1")
    private Long id;

    @Schema(description = "회원 ID", example = "100")
    @JsonProperty("member_id")
    private Long memberId;

    @Schema(description = "마켓 코드", example = "KRW-BTC")
    private String market;

    @Schema(description = "거래 종류", example = "BUY")
    private String side; // BUY, SELL

    @Schema(description = "거래 가격", example = "6000000")
    private BigDecimal price;

    @Schema(description = "거래 수량", example = "0.5")
    private BigDecimal size;

    @Schema(description = "거래 상태", example = "WAITING")
    private String status; // WAITING, SUCCESS, FAILED

    @Schema(description = "주문 요청 시각")
    @JsonProperty("request_at")
    private LocalDateTime requestAt;

    @Schema(description = "체결 시각")
    @JsonProperty("matched_at")
    private LocalDateTime matchedAt;
}