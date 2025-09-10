package com.example.market_follower.dto.upbit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "서버에서 가공한 거래 가능 코인 정보 DTO")
public class TradableCoinDto {

    @Schema(description = "마켓 코드", example = "KRW-BTC")
    @JsonProperty("market")
    private String market;

    @Schema(description = "코인 한글명", example = "비트코인")
    @JsonProperty("korean_name")
    private String koreanName;

    @Schema(description = "코인 영문명", example = "Bitcoin")
    @JsonProperty("english_name")
    private String englishName;

    @Schema(description = "마켓 경고 여부", example = "false")
    @JsonProperty("is_warning")
    private Boolean isWarning;

    @Schema(description = "가격 급등락 경보 여부", example = "true")
    @JsonProperty("is_caution_price_fluctuations")
    private Boolean isCautionPriceFluctuations;

    @Schema(description = "거래량 급등 여부", example = "false")
    @JsonProperty("is_caution_trading_volume_soaring")
    private Boolean isCautionTradingVolumeSoaring;

    @Schema(description = "입금량 급등 여부", example = "false")
    @JsonProperty("is_caution_deposit_amount_soaring")
    private Boolean isCautionDepositAmountSoaring;

    @Schema(description = "글로벌 가격 차이 여부", example = "false")
    @JsonProperty("is_caution_global_price_differences")
    private Boolean isCautionGlobalPriceDifferences;

    @Schema(description = "소수 계정 집중 여부", example = "false")
    @JsonProperty("is_caution_concentration_of_small_accounts")
    private Boolean isCautionConcentrationOfSmallAccounts;
}
