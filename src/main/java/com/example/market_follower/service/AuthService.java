package com.example.market_follower.service;

import com.example.market_follower.controller.AuthController;
import com.example.market_follower.dto.GoogleUserInfoDto;
import com.example.market_follower.dto.MemberLoginResponseDto;
import com.example.market_follower.exception.DuplicateEmailException;
import com.example.market_follower.model.Member;
import com.example.market_follower.repository.AuthRepository;
import com.example.market_follower.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final AuthRepository authRepository;

    public void signup(AuthController.SignupRequest request) {
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException(request.getEmail());
        }

        Member member = Member.builder()
                .email(request.getEmail())
                .name(request.getName())
                .phoneNumber(request.getPhoneNumber())
                .birthday(request.getBirthday())
                .build();

        memberRepository.save(member);
    }

    public MemberLoginResponseDto loginWithGoogle(String accessToken) {
        try {
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
        } catch (Exception e) {
            log.error("Google 로그인 처리 중 오류 발생", e);
            throw new RuntimeException("Google 로그인 처리 실패", e);
        }
    }

    private GoogleUserInfoDto verifyGoogleAccessToken(String accessToken) {
        try {
            String url = "https://www.googleapis.com/oauth2/v3/userinfo";

            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            RestTemplate restTemplate = new RestTemplate();
            ResponseEntity<GoogleUserInfoDto> response = restTemplate.exchange(
                    url, HttpMethod.GET, entity, GoogleUserInfoDto.class);

            if (response.getBody() == null) {
                throw new RuntimeException("Google API에서 사용자 정보를 가져올 수 없습니다");
            }

            return response.getBody();

        } catch (HttpClientErrorException e) {
            log.error("Google API 호출 실패: {} - {}", e.getStatusCode(), e.getResponseBodyAsString());
            throw new RuntimeException("유효하지 않은 Google Access Token", e);
        } catch (Exception e) {
            log.error("Google 토큰 검증 중 오류 발생", e);
            throw new RuntimeException("Google 토큰 검증 실패", e);
        }
    }
}