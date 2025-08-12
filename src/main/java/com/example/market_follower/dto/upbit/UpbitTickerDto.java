package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpbitTickerDto {
    private String market;

    @JsonProperty("trade_date")
    private String tradeDate;               // yyyyMMdd, UTC

    @JsonProperty("trade_time")
    private String tradeTime;               // HHmmss, UTC

    @JsonProperty("trade_date_kst")
    private String tradeDateKst;            // yyyyMMdd, KST

    @JsonProperty("trade_time_kst")
    private String tradeTimeKst;            // HHmmss, KST

    @JsonProperty("trade_timestamp")
    private long tradeTimestamp;            // Unix Timestamp, UTC

    @JsonProperty("opening_price")
    private double openingPrice;

    @JsonProperty("high_price")
    private double highPrice;

    @JsonProperty("low_price")
    private double lowPrice;

    @JsonProperty("trade_price")
    private double tradePrice;              // 종가(현재가)

    @JsonProperty("prev_closing_price")
    private double prevClosingPrice;

    private String change;                  // EVEN, RISE, FALL

    @JsonProperty("change_price")
    private double changePrice;             // 변화액 절대값

    @JsonProperty("change_rate")
    private double changeRate;              // 변화율 절대값

    @JsonProperty("signed_change_price")
    private double signedChangePrice;       // 부호 있는 변화액

    @JsonProperty("signed_change_rate")
    private double signedChangeRate;        // 부호 있는 변화율

    @JsonProperty("trade_volume")
    private double tradeVolume;             // 가장 최근 거래량

    @JsonProperty("acc_trade_price")
    private double accTradePrice;           // 누적 거래대금(UTC 0시 기준)

    @JsonProperty("acc_trade_price_24h")
    private double accTradePrice24h;        // 24시간 누적 거래대금

    @JsonProperty("acc_trade_volume")
    private double accTradeVolume;          // 누적 거래량(UTC 0시 기준)

    @JsonProperty("acc_trade_volume_24h")
    private double accTradeVolume24h;       // 24시간 누적 거래량

    @JsonProperty("highest_52_week_price")
    private double highest52WeekPrice;      // 52주 신고가

    @JsonProperty("highest_52_week_date")
    private String highest52WeekDate;       // yyyy-MM-dd

    @JsonProperty("lowest_52_week_price")
    private double lowest52WeekPrice;       // 52주 신저가

    @JsonProperty("lowest_52_week_date")
    private String lowest52WeekDate;        // yyyy-MM-dd

    @JsonProperty("timestamp")
    private long upbitTimestamp;                 // 타임스탬프
}