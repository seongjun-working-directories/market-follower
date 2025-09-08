package com.example.market_follower.dto.wallet;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletBalacneUpdateDto {
    // 입금(+), 출금(-)
    private BigDecimal amount;
}
