package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

@Data
@Schema(description = "Upbit 시세 정보 DTO")
public class UpbitTickerDto {

    @Schema(description = "마켓 코드", example = "KRW-BTC")
    private String market;

    @Schema(description = "거래일 (UTC, yyyyMMdd)", example = "20250814")
    @JsonProperty("trade_date")
    private String tradeDate;

    @Schema(description = "거래시각 (UTC, HHmmss)", example = "103012")
    @JsonProperty("trade_time")
    private String tradeTime;

    @Schema(description = "거래일 (KST, yyyyMMdd)", example = "20250814")
    @JsonProperty("trade_date_kst")
    private String tradeDateKst;

    @Schema(description = "거래시각 (KST, HHmmss)", example = "193012")
    @JsonProperty("trade_time_kst")
    private String tradeTimeKst;

    @Schema(description = "거래 타임스탬프 (UTC, Unix Timestamp)", example = "1691989812000")
    @JsonProperty("trade_timestamp")
    private long tradeTimestamp;

    @Schema(description = "시가", example = "43500000")
    @JsonProperty("opening_price")
    private double openingPrice;

    @Schema(description = "고가", example = "45000000")
    @JsonProperty("high_price")
    private double highPrice;

    @Schema(description = "저가", example = "43000000")
    @JsonProperty("low_price")
    private double lowPrice;

    @Schema(description = "종가(현재가)", example = "44800000")
    @JsonProperty("trade_price")
    private double tradePrice;

    @Schema(description = "전일 종가", example = "44000000")
    @JsonProperty("prev_closing_price")
    private double prevClosingPrice;

    @Schema(description = "변동 유형 (RISE, FALL, EVEN)", example = "RISE")
    private String change;

    @Schema(description = "변화액 절대값", example = "800000")
    @JsonProperty("change_price")
    private double changePrice;

    @Schema(description = "변화율 절대값", example = "0.0182")
    @JsonProperty("change_rate")
    private double changeRate;

    @Schema(description = "부호 있는 변화액", example = "800000")
    @JsonProperty("signed_change_price")
    private double signedChangePrice;

    @Schema(description = "부호 있는 변화율", example = "0.0182")
    @JsonProperty("signed_change_rate")
    private double signedChangeRate;

    @Schema(description = "최근 거래량", example = "12.5")
    @JsonProperty("trade_volume")
    private double tradeVolume;

    @Schema(description = "누적 거래대금 (UTC 0시 기준)", example = "5400000000")
    @JsonProperty("acc_trade_price")
    private double accTradePrice;

    @Schema(description = "24시간 누적 거래대금", example = "12500000000")
    @JsonProperty("acc_trade_price_24h")
    private double accTradePrice24h;

    @Schema(description = "누적 거래량 (UTC 0시 기준)", example = "150.5")
    @JsonProperty("acc_trade_volume")
    private double accTradeVolume;

    @Schema(description = "24시간 누적 거래량", example = "300.75")
    @JsonProperty("acc_trade_volume_24h")
    private double accTradeVolume24h;

    @Schema(description = "52주 최고가", example = "50000000")
    @JsonProperty("highest_52_week_price")
    private double highest52WeekPrice;

    @Schema(description = "52주 최고가 달성일 (yyyy-MM-dd)", example = "2025-05-12")
    @JsonProperty("highest_52_week_date")
    private String highest52WeekDate;

    @Schema(description = "52주 최저가", example = "30000000")
    @JsonProperty("lowest_52_week_price")
    private double lowest52WeekPrice;

    @Schema(description = "52주 최저가 달성일 (yyyy-MM-dd)", example = "2024-08-20")
    @JsonProperty("lowest_52_week_date")
    private String lowest52WeekDate;

    @Schema(description = "업비트 서버 타임스탬프", example = "1691989812000")
    @JsonProperty("timestamp")
    private long upbitTimestamp;
}
