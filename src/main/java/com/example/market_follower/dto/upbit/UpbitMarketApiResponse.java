package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.*;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "Upbit 마켓 정보 DTO")
public class UpbitMarketApiResponse {

    @Schema(description = "마켓 코드", example = "KRW-BTC")
    private String market;

    @Schema(description = "코인 한글명", example = "비트코인")
    @JsonProperty("korean_name")
    private String koreanName;

    @Schema(description = "코인 영문명", example = "Bitcoin")
    @JsonProperty("english_name")
    private String englishName;

    @Schema(description = "마켓 이벤트 정보")
    @JsonProperty("market_event")
    private MarketEvent marketEvent;

    @Data
    @Schema(description = "마켓 이벤트 세부 정보")
    public static class MarketEvent {

        @Schema(description = "경고 여부", example = "false")
        private Boolean warning;

        @Schema(description = "주의 이벤트 상세 정보")
        private Caution caution;

        @Data
        @Schema(description = "주의 이벤트 세부 항목")
        public static class Caution {

            @Schema(description = "가격 변동성 주의", example = "true")
            @JsonProperty("PRICE_FLUCTUATIONS")
            private Boolean priceFluctuations;

            @Schema(description = "거래량 급증 주의", example = "false")
            @JsonProperty("TRADING_VOLUME_SOARING")
            private Boolean tradingVolumeSoaring;

            @Schema(description = "입금액 급증 주의", example = "false")
            @JsonProperty("DEPOSIT_AMOUNT_SOARING")
            private Boolean depositAmountSoaring;

            @Schema(description = "글로벌 가격 차이 주의", example = "false")
            @JsonProperty("GLOBAL_PRICE_DIFFERENCES")
            private Boolean globalPriceDifferences;

            @Schema(description = "소규모 계좌 집중 주의", example = "false")
            @JsonProperty("CONCENTRATION_OF_SMALL_ACCOUNTS")
            private Boolean concentrationOfSmallAccounts;
        }
    }
}
