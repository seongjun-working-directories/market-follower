package com.example.market_follower.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;

@Slf4j
@Component
public class IpRateLimiterInterceptor implements HandlerInterceptor {

    private final StringRedisTemplate redisTemplate;

    // 부하테스트를 위한 허용 20회 -> 2000회
    private static final int MAX_REQUESTS = 2000; // 분당 최대 20회
    private static final Duration WINDOW = Duration.ofMinutes(1);

    public IpRateLimiterInterceptor(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 프록시 환경 고려: 실제 클라이언트 IP 확인
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim(); // 첫 번째 IP가 실제 클라이언트
        }

        String key = "rate:" + ip;
        Long currentCount = redisTemplate.opsForValue().increment(key);

        // 최초 생성 시 TTL 설정
        if (currentCount == 1) {
            redisTemplate.expire(key, WINDOW);
        }

        if (currentCount > MAX_REQUESTS) {
            log.warn("IP {} has exceeded rate limit ({} requests per {})", ip, MAX_REQUESTS, WINDOW);
            response.setStatus(429); // Too Many Requests
            response.getWriter().write("Too many requests");
            return false;
        }

        return true;
    }
}
