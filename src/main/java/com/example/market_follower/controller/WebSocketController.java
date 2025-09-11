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
@Tag(name = "WebSocket API", description = "실시간 암호화폐 시세, 호가, 주문 체결 알림 WebSocket 연결 방법 안내")
public class WebSocketController {

    @GetMapping("/docs")
    @Operation(
            summary = "WebSocket 사용 방법 안내",
            description = """
            실시간 암호화폐 시세, 호가 데이터 및 주문 체결 알림을 구독하는 WebSocket 연결 방법에 대한 설명입니다.
            웹소켓은 5 ~ 10초 간격으로 데이터를 보내기 때문에 초기 데이터는 무조건 관련 API를 호출하여 먼저 채워주시기 바랍니다.

            **접속 URL (SockJS)**
            ```
            http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/ws?token=<JWT토큰>
            ```
            (서버에서 STOMP + SockJS 사용)

            **데이터 업데이트 주기**
            - Ticker 데이터: 10초마다 브로드캐스팅 (서버 시작 후 130초 후 첫 전송)
            - Orderbook 데이터: 10초마다 브로드캐스팅 (서버 시작 후 140초 후 첫 전송)
            - 주문 체결 알림: 실시간 (주문이 체결되는 즉시 전송)
            - 주문 체결 확인: 5초마다 대기 중인 주문 체결 여부 확인
            - Redis 캐시 TTL: 3분 (데이터 유효성 보장)

            **연결 유지**
            - 서버와 클라이언트 모두 30초 이내에 heartbeat를 주고받아야 연결이 유지됩니다.
            - 클라이언트에서 `stompClient.heartbeat.outgoing = 30000` 및 `stompClient.heartbeat.incoming = 30000` 설정 권장.
            
            **절차**
            1. Google OAuth 로그인 → Access Token 발급
            2. `/auth/google` API로 JWT 발급
            3. **최초 화면 로딩 시** REST API(`/market/ticker/all` 또는 `/market/ticker/{market}`)로 초기 시세 데이터를 조회
            4. **최초 화면 로딩 시** REST API(`/orderbook/get/{market}`)로 초기 호가 데이터를 조회
            5. WebSocket 연결 시 URL에 `token` 파라미터로 JWT 전달
            6. 이후 WebSocket 연결로 생성된 구독 채널을 통해 실시간 업데이트 수신
            
            **STOMP 구독 채널**
            
            **1. 시세(Ticker) 채널**
            - `/topic/ticker/all` : 모든 코인 시세 일괄 전송 (List<UpbitTickerDto>)
            - `/topic/ticker/{market}` : 특정 코인 시세 (UpbitTickerDto)
              - 예시: `/topic/ticker/KRW-BTC`, `/topic/ticker/KRW-ETH`
            
            **2. 호가(Orderbook) 채널**
            - `/topic/orderbook/{market}` : 특정 코인 호가 (UpbitOrderbookDto)
              - 예시: `/topic/orderbook/KRW-BTC`, `/topic/orderbook/KRW-ETH`
              - 주의: 호가는 개별 코인별로만 제공되며, 전체 일괄 전송은 없음

            **3. 주문 체결 알림 채널 (개인별)**
            - `/topic/orders/{email}` : 개인별 주문 체결 알림 (TradeHistoryDto)
              - JWT에서 추출된 email을 사용하여 개인별 채널 구독
              - 주문이 체결될 때마다 실시간으로 알림 전송
              - 포함 정보: 주문ID, 마켓, 매수/매도, 체결가격, 수량, 상태, 체결시간 등

            **데이터 처리 흐름**
            1. Kafka Consumer가 Upbit API로부터 데이터 수신
            2. Redis에 최신 데이터 저장 (TTL: 3분)
            3. 스케줄러가 Redis에서 최신 데이터를 조회하여 WebSocket으로 브로드캐스팅
            4. 주문 체결 시스템이 5초마다 대기 중인 주문의 체결 조건을 확인
            5. 체결 조건이 맞으면 개인별 채널로 체결 알림 전송

            **주문 체결 조건**
            - BUY 주문: 매도 호가(Ask Price) ≤ 주문 가격일 때 체결
            - SELL 주문: 매수 호가(Bid Price) ≥ 주문 가격일 때 체결
            - 실제 체결가는 호가 기준으로 결정됨 (주문가격과 다를 수 있음)

            **클라이언트 예시 (JavaScript)**:
            ```javascript
            const socket = new SockJS("http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/ws?token=" + jwtToken);
            const stompClient = Stomp.over(socket);
            stompClient.debug = () => {};

            stompClient.heartbeat.outgoing = 30000; // 30초마다 heartbeat 전송
            stompClient.heartbeat.incoming = 30000; // 30초 내 heartbeat 미수신 시 연결 끊김

            stompClient.connect({}, () => {
                // 전체 코인 시세 구독
                stompClient.subscribe('/topic/ticker/all', message => {
                    const allTickers = JSON.parse(message.body);
                    console.log("전체 시세 업데이트:", allTickers.length + "개 코인");
                });
                
                // 특정 코인 시세 구독
                stompClient.subscribe('/topic/ticker/KRW-BTC', message => {
                    const btcTicker = JSON.parse(message.body);
                    console.log("BTC 시세:", btcTicker.trade_price);
                });
                
                // 특정 코인 호가 구독
                stompClient.subscribe('/topic/orderbook/KRW-BTC', message => {
                    const btcOrderbook = JSON.parse(message.body);
                    console.log("BTC 호가:", btcOrderbook.orderbook_units);
                });

                // 개인 주문 체결 알림 구독 (JWT에서 email 추출 필요)
                const email = extractEmailFromJWT(jwtToken); // JWT 파싱 함수 필요
                stompClient.subscribe('/topic/orders/' + email, message => {
                    const tradeNotification = JSON.parse(message.body);
                    console.log("주문 체결:", tradeNotification);
                    // 체결 알림 UI 업데이트
                    updateOrderStatus(tradeNotification);
                });
                
            }, error => {
                console.error("WebSocket 연결 오류:", error);
            });

            // JWT에서 email 추출하는 예시 함수 (실제 구현 필요)
            function extractEmailFromJWT(token) {
                try {
                    const base64Url = token.split('.')[1];
                    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
                    const jsonPayload = decodeURIComponent(
                        atob(base64)
                            .split('')
                            .map(c => '%' + ('00' + c.charCodeAt(0).toString(16)).slice(-2))
                            .join('')
                    );
                    const payload = JSON.parse(jsonPayload);
                    return payload?.sub;
                } catch (e) {
                    console.error("JWT 파싱 오류:", e);
                    return null;
                }
            }

            // 주문 상태 업데이트 UI 함수 예시
            function updateOrderStatus(tradeData) {
                // 주문 리스트 업데이트
                // 포트폴리오 업데이트
                // 알림 표시 등
                alert(`주문 체결: ${tradeData.market} ${tradeData.side} ${tradeData.size}개 @ ${tradeData.price}원`);
            }
            ```
            
            **주문 체결 알림 데이터 구조 (TradeHistoryDto)**:
            ```json
            {
                "id": 123,
                "memberId": 456,
                "market": "KRW-BTC",
                "side": "BUY",
                "price": 50000000.0,
                "size": 0.001,
                "status": "SUCCESS",
                "requestAt": "2025-09-09T10:00:00",
                "matchedAt": "2025-09-09T10:05:30"
            }
            ```
            
            **권장사항**
            - 성능 최적화를 위해 필요한 채널만 구독하세요
            - 전체 시세가 필요한 경우 `/topic/ticker/all` 사용
            - 특정 코인만 모니터링하는 경우 개별 채널 구독
            - 호가 데이터는 데이터량이 많으므로 필요한 코인만 선택적 구독 권장
            - 주문 체결 알림은 로그인한 사용자만 자신의 채널을 구독해야 함
            - 주문 체결 알림을 통해 실시간으로 포트폴리오와 지갑 상태를 업데이트하세요

            **주의사항**
            - 주문 체결 시스템은 5초마다 체결 조건을 확인하므로 최대 5초의 지연이 발생할 수 있습니다
            - 체결가는 주문가격과 다를 수 있습니다 (시장가 기준으로 체결됨)
            - 네트워크 지연 등으로 인해 실제 거래소와 체결 시점에 차이가 있을 수 있습니다
            - Redis 캐시 TTL(3분) 내에서만 데이터가 유효합니다
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
                                            value = "WebSocket 연결 방법과 구독 채널 설명 (시세, 호가, 주문 체결 알림 포함)"
                                    )
                            )
                    )
            }
    )
    public String getWebSocketDocs() {
        return "Swagger 문서에서 WebSocket 연결 방법 및 시세/호가/주문 체결 알림 구독 채널을 확인하세요.";
    }
}
