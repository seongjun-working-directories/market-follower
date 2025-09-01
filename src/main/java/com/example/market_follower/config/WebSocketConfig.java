package com.example.market_follower.config;

import com.example.market_follower.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // TaskScheduler 생성
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.setPoolSize(5);
        taskScheduler.setThreadNamePrefix("wss-heartbeat-thread-");
        taskScheduler.initialize();

        // SimpleBroker에 하트비트와 스케줄러 연결
        config.enableSimpleBroker("/topic")
                .setHeartbeatValue(new long[]{5000, 30000})
                .setTaskScheduler(taskScheduler);               // 구독 prefix

        // 클라이언트 -> 서버로 메시지 prefix
        config.setApplicationDestinationPrefixes("/app");       // 발행 prefix
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
                .setAllowedOriginPatterns("*")
                .addInterceptors(new JwtHandshakeInterceptor(jwtTokenProvider))
                .withSockJS();
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registration) {
        // Send buffer 크기 제한 (바이트 단위)
        registration.setSendBufferSizeLimit(2048 * 1024); // 2MB
        // Send time limit (ms) - 오래 걸리는 send 강제 종료
        registration.setSendTimeLimit(10000); // 10초 안에 못 보내면 버림
    }
}
