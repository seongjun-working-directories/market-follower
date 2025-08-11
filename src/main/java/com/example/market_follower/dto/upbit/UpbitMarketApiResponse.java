package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class UpbitMarketApiResponse {
    private String market;
    @JsonProperty("korean_name")
    private String koreanName;
    @JsonProperty("english_name")
    private String englishName;
    @JsonProperty("market_event")
    private MarketEvent marketEvent;

    @Data
    public static class MarketEvent {
        private Boolean warning;
        private Caution caution;

        @Data
        public static class Caution {
            @JsonProperty("PRICE_FLUCTUATIONS")
            private Boolean priceFluctuations;
            @JsonProperty("TRADING_VOLUME_SOARING")
            private Boolean tradingVolumeSoaring;
            @JsonProperty("DEPOSIT_AMOUNT_SOARING")
            private Boolean depositAmountSoaring;
            @JsonProperty("GLOBAL_PRICE_DIFFERENCES")
            private Boolean globalPriceDifferences;
            @JsonProperty("CONCENTRATION_OF_SMALL_ACCOUNTS")
            private Boolean concentrationOfSmallAccounts;
        }
    }
}
