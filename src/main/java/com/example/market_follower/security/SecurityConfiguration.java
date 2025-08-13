package com.example.market_follower.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@RequiredArgsConstructor
public class SecurityConfiguration {
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth-> auth
                        // health checker는 항상 허용
                        .requestMatchers("/actuator/health").permitAll()

                        // WebSocket도 인증을 JwtHandshakerInterceptor에 위임
                        .requestMatchers("/ws/**").permitAll()

                        // 정적 리소스는 접근을 항상 허용
                        .requestMatchers("/css/**", "/js/**", "/image/**", "/images/**", "/webjars/**").permitAll()

                        .requestMatchers(
                                "/swagger-ui/**",
                                "/v3/api-docs/**",
                                "/swagger-resources/**",
                                "/swagger-resources/configuration/ui",
                                "/swagger-resources/configuration/security"
                        ).permitAll()

                        .requestMatchers("/h2-console/**").permitAll()

                        .requestMatchers("/auth/**").permitAll()

                        .requestMatchers("/member/**").hasAuthority("ROLE_ADMIN")

                        .anyRequest().authenticated()
                )
                .headers(headers -> headers
                                .frameOptions(frameOptionsConfig -> frameOptionsConfig.sameOrigin())
                )

                //  JWT 인증 필터를 UsernamePasswordAuthenticationFilter 전에 등록
                .addFilterBefore(
                        jwtAuthenticationFilter, org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class
                );

        return httpSecurity.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
