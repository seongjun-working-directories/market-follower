package com.example.market_follower.security;

import com.example.market_follower.model.Auth;
import com.example.market_follower.model.Member;
import com.example.market_follower.repository.MemberRepository;
import com.example.market_follower.repository.AuthRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final MemberRepository memberRepository;
    private final AuthRepository authRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        // 회원의 권한들 조회
        List<Auth> authList = authRepository.findAllByMember(member);

        List<GrantedAuthority> authorities = authList.stream()
                .map(Auth::getRole)  // role String, 예: "ROLE_USER"
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        return org.springframework.security.core.userdetails.User.builder()
                .username(member.getEmail())
                .password("") // 소셜 로그인이라 비밀번호는 사용 안 함 (필요 시 비워두거나 임의값)
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }
}
