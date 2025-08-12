package com.example.market_follower.dto.upbit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradableCoinDto {
    private String market;
    private String koreanName;
    private String englishName;
    private Boolean isWarning;
    private Boolean isCautionPriceFluctuations;             // 가격 급등락 경보 여부
    private Boolean isCautionTradingVolumeSoaring;          // 거래량 급등 여부
    private Boolean isCautionDepositAmountSoaring;          // 입금량 급등 여부
    private Boolean isCautionGlobalPriceDifferences;        // 글로벌 가격 차이 여부
    private Boolean isCautionConcentrationOfSmallAccounts;  // 소수 계정 집중 여부
}