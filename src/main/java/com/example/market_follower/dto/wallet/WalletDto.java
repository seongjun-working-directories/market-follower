package com.example.market_follower.dto.wallet;

import lombok.*;
import java.math.BigDecimal;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class WalletDto {
    private Long walletId;
    private Long memberId;
    private BigDecimal balance;
}
