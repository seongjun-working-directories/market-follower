package com.example.market_follower.config;

import com.example.market_follower.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    private final JwtTokenProvider jwtTokenProvider;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request,
                                   ServerHttpResponse response,
                                   WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // URI query에서 token 추출
        URI uri = request.getURI();
        String query = uri.getQuery(); // 예: token=eyJhbGciOiJIUzI1NiJ9...
        Optional<String> tokenOpt = Optional.empty();

        if (query != null) {
            for (String param : query.split("&")) {
                if (param.startsWith("token=")) {
                    tokenOpt = Optional.of(param.substring(6));
                    break;
                }
            }
        }

        if (tokenOpt.isPresent() && jwtTokenProvider.validateToken(tokenOpt.get())) {
            attributes.put("jwt", tokenOpt.get()); // WebSocket session에 저장
            return true;
        }

        // 토큰 없거나 유효하지 않으면 연결 거부
        response.setStatusCode(org.springframework.http.HttpStatus.FORBIDDEN);
        return false;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request,
                               ServerHttpResponse response,
                               WebSocketHandler wsHandler,
                               Exception exception) {}
}
