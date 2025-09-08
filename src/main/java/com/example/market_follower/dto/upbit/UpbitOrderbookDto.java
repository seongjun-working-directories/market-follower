package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Upbit 호가 정보 DTO")
public class UpbitOrderbookDto {

    @Schema(description = "마켓 코드", example = "KRW-BTC")
    private String market;

    @Schema(description = "타임스탬프", example = "1757332586602")
    private long timestamp;

    @Schema(description = "총 매도 잔량", example = "3.58032812")
    @JsonProperty("total_ask_size")
    private double totalAskSize;

    @Schema(description = "총 매수 잔량", example = "1.79065811")
    @JsonProperty("total_bid_size")
    private double totalBidSize;

    @Schema(description = "호가 단위 리스트")
    @JsonProperty("orderbook_units")
    private List<OrderbookUnit> orderbookUnits;

    @Schema(description = "호가 레벨", example = "0")
    private int level;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "호가 단위")
    public static class OrderbookUnit {

        @Schema(description = "매도호가", example = "155978000")
        @JsonProperty("ask_price")
        private double askPrice;

        @Schema(description = "매수호가", example = "155952000")
        @JsonProperty("bid_price")
        private double bidPrice;

        @Schema(description = "매도 잔량", example = "0.00001081")
        @JsonProperty("ask_size")
        private double askSize;

        @Schema(description = "매수 잔량", example = "0.03361451")
        @JsonProperty("bid_size")
        private double bidSize;
    }
}