package com.example.market_follower.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .authorizeHttpRequests(auth-> auth
                        // 정적 리소스는 접근을 항상 허용
                        .requestMatchers("/css/**", "/js/**", "/image/**", "/images/**", "/webjars/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-resources/configuration/ui",
                                "/swagger-resources/configuration/security"
                        ).permitAll()

                        .requestMatchers("/signup").permitAll()

                        .requestMatchers("/member/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .httpBasic(withDefaults())  // Authorization: Basic base64(username:password) 형식으로 인증하겠다는 선언
                .headers(headers -> headers
                                .frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin())
                );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
