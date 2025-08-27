package com.example.market_follower.model.candle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

@Entity
@Table(
        name = "upbit_candle_1y",
        uniqueConstraints = @UniqueConstraint(columnNames = {"market", "candle_date_time_utc"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpbitCandle1y extends BaseCandle {
    @Column(name = "prev_closing_price", precision = 20, scale = 8)
    private BigDecimal prevClosingPrice;

    @Column(name = "change_price", precision = 20, scale = 8)
    private BigDecimal changePrice;

    @Column(name = "change_rate", precision = 10, scale = 8)
    private BigDecimal changeRate;
}