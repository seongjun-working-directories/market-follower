package com.example.market_follower.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tradable_coin")
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradableCoin {
    @Id
    @Column(name = "market", length = 20)
    private String market;

    @Column(name = "korean_name", length = 50)
    private String koreanName;

    @Column(name = "english_name", length = 50)
    private String englishName;

    @Column(name = "is_warning")
    private Boolean isWarning;

    @Column(name = "is_caution_price_fluctuations")
    private Boolean isCautionPriceFluctuations;

    @Column(name = "is_caution_trading_volume_soaring")
    private Boolean isCautionTradingVolumeSoaring;

    @Column(name = "is_caution_deposit_amount_soaring")
    private Boolean isCautionDepositAmountSoaring;

    @Column(name = "is_caution_global_price_differences")
    private Boolean isCautionGlobalPriceDifferences;

    @Column(name = "is_caution_concentration_of_small_accounts")
    private Boolean isCautionConcentrationOfSmallAccounts;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    public void prePersist() {
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}
