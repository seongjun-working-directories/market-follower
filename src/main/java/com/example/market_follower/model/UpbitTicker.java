package com.example.market_follower.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "upbit_ticker")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpbitTicker {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "market")
    private String market;

    @Column(name = "trade_date")
    @JsonProperty("trade_date")
    private String tradeDate;

    @Column(name = "trade_time")
    @JsonProperty("trade_time")
    private String tradeTime;

    @Column(name = "trade_date_kst")
    @JsonProperty("trade_date_kst")
    private String tradeDateKst;

    @Column(name = "trade_time_kst")
    @JsonProperty("trade_time_kst")
    private String tradeTimeKst;

    @Column(name = "trade_timestamp")
    @JsonProperty("trade_timestamp")
    private long tradeTimestamp;

    @Column(name = "opening_price")
    @JsonProperty("opening_price")
    private double openingPrice;

    @Column(name = "high_price")
    @JsonProperty("high_price")
    private double highPrice;

    @Column(name = "low_price")
    @JsonProperty("low_price")
    private double lowPrice;

    @Column(name = "trade_price")
    @JsonProperty("trade_price")
    private double tradePrice;

    @Column(name = "prev_closing_price")
    @JsonProperty("prev_closing_price")
    private double prevClosingPrice;

    @Column(name = "change_direction")
    private String change;

    @Column(name = "change_price")
    @JsonProperty("change_price")
    private double changePrice;

    @Column(name = "change_rate")
    @JsonProperty("change_rate")
    private double changeRate;

    @Column(name = "signed_change_price")
    @JsonProperty("signed_change_price")
    private double signedChangePrice;

    @Column(name = "signed_change_rate")
    @JsonProperty("signed_change_rate")
    private double signedChangeRate;

    @Column(name = "trade_volume")
    @JsonProperty("trade_volume")
    private double tradeVolume;

    @Column(name = "acc_trade_price")
    @JsonProperty("acc_trade_price")
    private double accTradePrice;

    @Column(name = "acc_trade_price_24h")
    @JsonProperty("acc_trade_price_24h")
    private double accTradePrice24h;

    @Column(name = "acc_trade_volume")
    @JsonProperty("acc_trade_volume")
    private double accTradeVolume;

    @Column(name = "acc_trade_volume_24h")
    @JsonProperty("acc_trade_volume_24h")
    private double accTradeVolume24h;

    @Column(name = "highest_52_week_price")
    @JsonProperty("highest_52_week_price")
    private double highest52WeekPrice;

    @Column(name = "highest_52_week_date")
    @JsonProperty("highest_52_week_date")
    private String highest52WeekDate;

    @Column(name = "lowest_52_week_price")
    @JsonProperty("lowest_52_week_price")
    private double lowest52WeekPrice;

    @Column(name = "lowest_52_week_date")
    @JsonProperty("lowest_52_week_date")
    private String lowest52WeekDate;

    @Column(name = "upbit_timestamp")
    @JsonProperty("timestamp")
    private long upbitTimestamp;
}
