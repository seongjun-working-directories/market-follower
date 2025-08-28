package com.example.market_follower.model.candle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "upbit_candle_30d",
        uniqueConstraints = @UniqueConstraint(columnNames = {"market", "candle_date_time_utc"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpbitCandle30d extends BaseCandle {
    @Column(name = "unit", nullable = false)
    private Integer unit;
}
