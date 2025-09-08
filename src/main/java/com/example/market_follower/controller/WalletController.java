package com.example.market_follower.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.example.market_follower.dto.wallet.WalletDto;
import com.example.market_follower.service.WalletService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet API", description = "지갑 정보 조회 및 관리 API")
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/me")
    public ResponseEntity<WalletDto> getMyWallet(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user) {
        WalletDto walletDto = walletService.getMyWalletByUserDetails(user);
        return ResponseEntity.status(HttpStatus.OK).body(walletDto);
    }
}
