package com.example.market_follower.dto.upbit.candle;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "30일 단위 업비트 캔들 DTO")
public class UpbitCandle30dDto {

    @Schema(description = "마켓 코드", example = "KRW-BTC")
    @JsonProperty("market")
    private String market;

    @Schema(description = "UTC 기준 캔들 시간", example = "2025-08-27T00:00:00")
    @JsonProperty("candle_date_time_utc")
    private String candleDateTimeUtc;

    @Schema(description = "KST 기준 캔들 시간", example = "2025-08-27T09:00:00")
    @JsonProperty("candle_date_time_kst")
    private String candleDateTimeKst;

    @Schema(description = "시가", example = "30000000.0")
    @JsonProperty("opening_price")
    private BigDecimal openingPrice;

    @Schema(description = "고가", example = "31000000.0")
    @JsonProperty("high_price")
    private BigDecimal highPrice;

    @Schema(description = "저가", example = "29500000.0")
    @JsonProperty("low_price")
    private BigDecimal lowPrice;

    @Schema(description = "종가", example = "30500000.0")
    @JsonProperty("trade_price")
    private BigDecimal tradePrice;

    @Schema(description = "타임스탬프", example = "1693084800000")
    @JsonProperty("timestamp")
    private Long timestamp;

    @Schema(description = "누적 거래 금액", example = "1500000000.0")
    @JsonProperty("candle_acc_trade_price")
    private BigDecimal candleAccTradePrice;

    @Schema(description = "누적 거래량", example = "50.0")
    @JsonProperty("candle_acc_trade_volume")
    private BigDecimal candleAccTradeVolume;

    @Schema(description = "캔들 단위", example = "1")
    @JsonProperty("unit")
    private Integer unit;

    @Schema(description = "데이터 생성 시각", example = "2025-08-27T09:00:01")
    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @Schema(description = "데이터 수정 시각", example = "2025-08-27T09:00:01")
    @JsonProperty("updated_at")
    private LocalDateTime updatedAt;
}
