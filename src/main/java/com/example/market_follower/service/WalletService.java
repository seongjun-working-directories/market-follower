package com.example.market_follower.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import com.example.market_follower.dto.upbit.WalletDto;
import com.example.market_follower.model.Member;
import com.example.market_follower.model.Wallet;
import com.example.market_follower.repository.MemberRepository;
import com.example.market_follower.repository.WalletRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class WalletService {
    private final MemberRepository memberRepository;
    private final WalletRepository walletRepository;

    public WalletDto getMyWalletByUserDetails(org.springframework.security.core.userdetails.User user) {
        Member member = memberRepository.findByEmail(
            user.getUsername()
        ).orElseThrow(() -> new RuntimeException("회원 정보 없음"));

        Wallet wallet = walletRepository.findByMemberId(
            member.getId()
        ).orElseThrow(() -> new RuntimeException("지갑 정보 없음"));

        return WalletDto.builder()
                .walletId(wallet.getId())
                .memberId(member.getId())
                .balance(wallet.getBalance())
                .locked(wallet.getLocked())
                .build();
    }
}
