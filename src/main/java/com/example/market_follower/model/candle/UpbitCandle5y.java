package com.example.market_follower.model.candle;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(
        name = "upbit_candle_5y",
        uniqueConstraints = @UniqueConstraint(columnNames = {"market", "candle_date_time_utc"})
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class UpbitCandle5y extends BaseCandle {
    @Column(name = "first_day_of_period")
    private LocalDate firstDayOfPeriod;
}
