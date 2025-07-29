package com.example.market_follower.service;

import com.example.market_follower.dto.GoogleUserInfoDto;
import com.example.market_follower.dto.MemberLoginResponseDto;
import com.example.market_follower.model.Member;
import com.example.market_follower.repository.AuthRepository;
import com.example.market_follower.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final AuthRepository authRepository;

    public MemberLoginResponseDto loginWithGoogle(String accessToken) {
        // 구글 토큰 검증 및 사용자 정보 불러오기
        GoogleUserInfoDto googleUserInfo = verifyGoogleAccessToken(accessToken);

        // 사용자 존재 여부 확인
        Optional<Member> optionalMember = memberRepository.findByEmail(googleUserInfo.getEmail());

        if (optionalMember.isPresent()) {
            Member member = optionalMember.get();
            return MemberLoginResponseDto.builder()
                    .status("REGISTERED")
                    .email(member.getEmail())
                    .name(member.getName())
                    .memberId(member.getId())
                    .build();
        } else {
            return MemberLoginResponseDto.builder()
                    .status("NOT_REGISTERED")
                    .email(googleUserInfo.getEmail())
                    .name(googleUserInfo.getName())
                    .build();
        }
    }

    private GoogleUserInfoDto verifyGoogleAccessToken(String accessToken) {
        String url = "https://www.googleapis.com/oauth2/v3/userinfo";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<GoogleUserInfoDto> response = restTemplate.exchange(url, HttpMethod.GET, entity, GoogleUserInfoDto.class);

        return response.getBody();
    }
}
