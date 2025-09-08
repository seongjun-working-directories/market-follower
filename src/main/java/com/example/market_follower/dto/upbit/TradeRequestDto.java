package com.example.market_follower.dto.upbit;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import com.example.market_follower.model.TradeHistory.Side;

import java.math.BigDecimal;

@Data
@Schema(description = "거래 요청 DTO")
public class TradeRequestDto {
    @Schema(description = "마켓 코드", example = "KRW-BTC")
    private String market;

    @Schema(description = "거래 종류", example = "BUY")
    private Side side; // BUY 또는 SELL

    @Schema(description = "거래 가격", example = "6000000")
    private BigDecimal price;

    @Schema(description = "거래 수량", example = "0.5")
    private BigDecimal size;
}