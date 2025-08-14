package com.example.market_follower.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/websocket")
@Tag(name = "WebSocket API", description = "기본 설명 및 WebSocket 연결 방법 안내")
public class WebSocketController {

    @GetMapping("/docs")
    @Operation(
            summary = "WebSocket 사용 방법 안내",
            description = """
            실시간 암호화폐 시세를 구독하는 WebSocket 연결 방법에 대한 설명입니다.

            **접속 URL (SockJS)**
            ```
            http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/ws?token=<JWT토큰>
            ```
            (서버에서 STOMP + SockJS 사용)

            **연결 유지**
            - 서버와 클라이언트 모두 30초 이내에 heartbeat를 주고받아야 연결이 유지됩니다.
            - 클라이언트에서 `stompClient.heartbeat.outgoing = 30000` 및 `stompClient.heartbeat.incoming = 30000` 설정 권장.
            
            **절차**
            1. Google OAuth 로그인 → Access Token 발급
            2. `/auth/google` API로 JWT 발급
            3. **최초 화면 로딩 시** REST API(`/market/ticker/all` 또는 `/market/ticker/{market}`)로 초기 시세 데이터를 조회
            4. WebSocket 연결 시 URL에 `token` 파라미터로 JWT 전달
            5. 이후 WebSocket 연결로 생성된 구독 채널을 통해 실시간 업데이트 수신
            
            **STOMP 구독 채널**
            - `/topic/ticker/all` : 모든 코인 시세 (List<UpbitTickerDto>)
            - `/topic/ticker/KRW-BTC` : 특정 코인 시세 (UpbitTickerDto)

            **클라이언트 예시 (JavaScript)**:
            ```javascript
            const socket = new SockJS("http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/ws?token=" + jwtToken);
            const stompClient = Stomp.over(socket);
            stompClient.debug = () => {};

            stompClient.heartbeat.outgoing = 30000; // 30초마다 heartbeat 전송
            stompClient.heartbeat.incoming = 30000; // 30초 내 heartbeat 미수신 시 연결 끊김

            stompClient.connect({}, () => {
                stompClient.subscribe('/topic/ticker/all', message => {
                    console.log("전체 시세", JSON.parse(message.body));
                });
                stompClient.subscribe('/topic/ticker/KRW-BTC', message => {
                    console.log("BTC 시세", JSON.parse(message.body));
                });
            });
            ```
            """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "WebSocket 설명 제공",
                            content = @Content(
                                    mediaType = "text/plain",
                                    examples = @ExampleObject(
                                            name = "설명",
                                            summary = "WebSocket 문서 예시",
                                            value = "WebSocket 연결 방법과 구독 채널 설명"
                                    )
                            )
                    )
            }
    )
    public String getWebSocketDocs() {
        return "Swagger 문서에서 WebSocket 연결 방법을 확인하세요.";
    }
}