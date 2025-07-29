package com.example.market_follower.service;

import com.example.market_follower.dto.MemberDto;
import com.example.market_follower.model.Member;
import com.example.market_follower.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;

    private MemberDto mapToMemberDto(Member member) {
        return MemberDto.builder()
                .id(member.getId())
                .name(member.getName())
                .email(member.getEmail())
                .phoneNumber(member.getPhoneNumber())
                .birthday(member.getBirthday())
                .build();
    }

    public MemberDto findByEmail(String email) {
        return memberRepository.findByEmail(email).map(this::mapToMemberDto).orElseThrow();
    }
}
