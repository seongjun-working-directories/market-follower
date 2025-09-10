package com.example.market_follower.service;

import com.example.market_follower.dto.GoogleUserInfoDto;
import com.example.market_follower.dto.MemberLoginResponseDto;
import com.example.market_follower.dto.SignupRequest;
import com.example.market_follower.exception.DuplicateEmailException;
import com.example.market_follower.exception.InvalidGoogleTokenException;
import com.example.market_follower.model.Auth;
import com.example.market_follower.model.Member;
import com.example.market_follower.model.Wallet;
import com.example.market_follower.repository.AuthRepository;
import com.example.market_follower.repository.MemberRepository;
import com.example.market_follower.repository.WalletRepository;
import com.example.market_follower.security.JwtTokenProvider;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.time.LocalDateTime;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {
    private final MemberRepository memberRepository;
    private final AuthRepository authRepository;
    private final WalletRepository walletRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Transactional
    public void signup(SignupRequest request) {
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

        Auth auth = Auth.builder().member(member).role("ROLE_USER").build();
        authRepository.save(auth);

        Wallet wallet = Wallet.builder()
            .member(member)
            .build(); // balance는 @Builder.Default로 1억원
        walletRepository.save(wallet);
    }

    public MemberLoginResponseDto loginWithGoogle(String accessToken) {
        try {
            // 구글 토큰 검증 및 사용자 정보 불러오기
            GoogleUserInfoDto googleUserInfo = verifyGoogleAccessToken(accessToken);

            // 사용자 존재 여부 확인
            Optional<Member> optionalMember = memberRepository.findByEmail(googleUserInfo.getEmail());

            if (optionalMember.isPresent()) {
                Member member = optionalMember.get();

                // 비활성화된 사용자는 로그인 불가
                if (!member.getActivated()) {
                    throw new RuntimeException("비활성화된 계정입니다");
                }

                // lastLoginAt 업데이트
                member.setLastLoginAt(LocalDateTime.now());
                memberRepository.save(member);

                String jwt = jwtTokenProvider.generateToken(member.getEmail());

                return MemberLoginResponseDto.builder()
                        .status("REGISTERED")
                        .email(member.getEmail())
                        .name(member.getName())
                        .memberId(member.getId())
                        .jwtToken(jwt)
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
            if (e.getStatusCode().is4xxClientError()) {
                throw new InvalidGoogleTokenException("유효하지 않은 Google Access Token", e);
            }
            throw new RuntimeException("Google API 호출 중 클라이언트 오류 발생", e);
        } catch (Exception e) {
            log.error("Google 토큰 검증 중 오류 발생", e);
            throw new RuntimeException("Google 토큰 검증 실패", e);
        }
    }
}