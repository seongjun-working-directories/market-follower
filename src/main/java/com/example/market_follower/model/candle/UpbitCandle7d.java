package com.example.market_follower.model.candle;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(
        name = "upbit_candle_7d",
        uniqueConstraints = @UniqueConstraint(columnNames = {"market", "candle_date_time_utc"})
)
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class UpbitCandle7d extends BaseCandle {

}