package com.example.market_follower.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.example.market_follower.dto.upbit.TradeRequestDto;
import com.example.market_follower.dto.upbit.TradeHistoryDto;
import com.example.market_follower.dto.upbit.HoldingDto;
import com.example.market_follower.service.OrderbookService;

@Slf4j
@RestController
@RequestMapping(value = "/orderbook")
@RequiredArgsConstructor
@Tag(name = "Orderbook API", description = "암호화폐 거래 주문, 보유량 조회, 거래 내역 관리 API")
public class OrderbookController {
    private final OrderbookService orderbookService;

    @PostMapping("/request")
    @Operation(
            summary = "암호화폐 매수/매도 주문 요청",
            description = """
            암호화폐 매수 또는 매도 주문을 생성합니다.
            
            **매수 주문 (BUY):**
            - 지갑의 balance에서 필요한 금액(price × size)을 차감
            - 차감된 금액은 locked 상태로 이동
            - 주문 상태는 WAITING으로 설정
            
            **매도 주문 (SELL):**
            - 보유 중인 암호화폐에서 해당 수량을 차감
            - 차감된 수량은 locked 상태로 이동
            - 주문 상태는 WAITING으로 설정
            
            **자동 체결 시스템:**
            - 5초마다 대기 중인 주문의 체결 조건을 자동 확인
            - BUY 주문: 매도 호가 ≤ 주문가격일 때 체결
            - SELL 주문: 매수 호가 ≥ 주문가격일 때 체결
            - 체결 시 WebSocket `/topic/orders/{memberId}`로 실시간 알림 전송
            
            **주의사항:**
            - 매수 시 충분한 잔액 필요
            - 매도 시 충분한 보유량 필요
            - 체결까지 최대 5초의 지연 시간 발생 가능
            """,
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "주문 접수 성공",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    name = "성공 메시지",
                                    value = "성공적으로 접수되었습니다."
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 요청 데이터 또는 잔액/보유량 부족",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = {
                                    @ExampleObject(
                                            name = "유효성 검증 실패",
                                            summary = "필수 파라미터 누락 또는 잘못된 값",
                                            value = "Price must be greater than 0"
                                    ),
                                    @ExampleObject(
                                            name = "잔액 부족",
                                            summary = "매수 주문 시 잔액 부족",
                                            value = "Insufficient balance"
                                    ),
                                    @ExampleObject(
                                            name = "보유량 부족",
                                            summary = "매도 주문 시 보유량 부족",
                                            value = "Insufficient holdings to sell"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "JWT token is missing or invalid")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "주문 처리 중 오류가 발생했습니다.")
                    )
            )
    })
    public ResponseEntity<String> requestTrade(
            @Parameter(
                    description = "거래 주문 요청 정보",
                    required = true,
                    schema = @Schema(implementation = TradeRequestDto.class)
            )
            @RequestBody TradeRequestDto tradeRequestDto,
            @Parameter(description = "JWT 인증 사용자 정보", hidden = true)
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            orderbookService.requestTrade(user, tradeRequestDto);
            return ResponseEntity.status(HttpStatus.CREATED).body("성공적으로 접수되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("Invalid trade request: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (IllegalStateException e) {
            log.warn("Trade request failed: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to place trade: {}", tradeRequestDto, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 처리 중 오류가 발생했습니다.");
        }
    }

    @PostMapping("/cancel/{orderId}")
    @Operation(
            summary = "대기 중인 주문 취소",
            description = """
            WAITING 상태인 주문을 취소하고 잠긴 자금/수량을 복구합니다.
            
            **매수 주문 취소:**
            - locked에서 balance로 자금 복구
            - 주문 상태를 CANCELLED로 변경
            
            **매도 주문 취소:**
            - locked에서 size로 수량 복구
            - 주문 상태를 CANCELLED로 변경
            
            **제약 조건:**
            - 본인의 주문만 취소 가능
            - WAITING 상태인 주문만 취소 가능
            - 이미 체결되거나 취소된 주문은 취소 불가
            
            **동시성 제어:**
            - 비관적 락으로 주문 상태 확인 후 처리
            - 체결과 취소가 동시에 발생하는 경우 안전하게 처리
            """,
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "주문 취소 성공",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "주문이 성공적으로 취소되었습니다.")
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "취소할 수 없는 주문 상태",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(
                                    name = "취소 불가 상태",
                                    value = "Only waiting orders can be cancelled"
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "주문을 찾을 수 없음",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "주문을 찾을 수 없습니다.")
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "JWT token is missing or invalid")
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "text/plain",
                            examples = @ExampleObject(value = "주문 취소 중 오류가 발생했습니다.")
                    )
            )
    })
    public ResponseEntity<String> cancelOrder(
            @Parameter(
                    description = "취소할 주문 ID",
                    required = true,
                    example = "123"
            )
            @PathVariable Long orderId,
            @Parameter(description = "JWT 인증 사용자 정보", hidden = true)
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            orderbookService.cancelOrder(orderId, user);
            return ResponseEntity.status(HttpStatus.OK).body("주문이 성공적으로 취소되었습니다.");
        } catch (IllegalArgumentException e) {
            log.warn("Cancel order failed - order not found: orderId={}, error={}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("주문을 찾을 수 없습니다.");
        } catch (IllegalStateException e) {
            log.warn("Cancel order failed - invalid status: orderId={}, error={}", orderId, e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (Exception e) {
            log.error("Failed to cancel order: orderId={}", orderId, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("주문 취소 중 오류가 발생했습니다.");
        }
    }

    @GetMapping("/holding/all")
    @Operation(
            summary = "전체 암호화폐 보유량 조회",
            description = """
            현재 로그인한 사용자의 모든 암호화폐 보유량을 조회합니다.
            
            **응답 데이터:**
            - market: 마켓 코드 (예: KRW-BTC)
            - size: 즉시 매도 가능한 수량
            - locked: 매도 주문에 사용되어 잠긴 수량
            - avg_price: 평균 매수가 (매수할 때마다 자동 계산)
            
            **보유량 계산:**
            - 총 보유량 = size + locked
            - 매수 시: size 증가, avg_price 재계산
            - 매도 주문 시: size에서 locked로 이동
            - 매도 체결 시: locked에서 차감
            
            **빈 결과:**
            - 보유 중인 암호화폐가 없으면 Optional.empty() 반환
            - 프론트엔드에서 null 체크 필요
            
            **실시간 업데이트:**
            - 주문 체결 시 보유량이 자동으로 업데이트됩니다
            - WebSocket 알림 수신 후 재조회 권장
            """,
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "보유량 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = HoldingDto.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "보유량 있음",
                                            summary = "암호화폐를 보유하고 있는 경우",
                                            value = """
                                            [
                                                {
                                                    "member_id": 123,
                                                    "market": "KRW-BTC",
                                                    "size": 0.5,
                                                    "locked": 0.1,
                                                    "avg_price": 52000000.00
                                                },
                                                {
                                                    "member_id": 123,
                                                    "market": "KRW-ETH",
                                                    "size": 2.3,
                                                    "locked": 0.0,
                                                    "avg_price": 3200000.00
                                                }
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "보유량 없음",
                                            summary = "보유 중인 암호화폐가 없는 경우",
                                            value = "null"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "error": "Unauthorized",
                                        "message": "JWT token is missing or invalid"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Optional<List<HoldingDto>>> getAllHoldings(
            @Parameter(description = "JWT 인증 사용자 정보", hidden = true)
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            Optional<List<HoldingDto>> holdings = orderbookService.getAllHoldings(user);
            return ResponseEntity.status(HttpStatus.OK).body(holdings);
        } catch (Exception e) {
            log.error("Failed to retrieve holdings for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/history/all")
    @Operation(
            summary = "전체 거래 내역 조회",
            description = """
            현재 로그인한 사용자의 모든 거래 내역을 조회합니다.
            
            **응답 데이터:**
            - id: 거래 고유 ID
            - market: 마켓 코드 (예: KRW-BTC)
            - side: 거래 종류 (BUY/SELL)
            - price: 주문 가격
            - size: 거래 수량
            - status: 거래 상태 (WAITING/SUCCESS/FAILED/CANCELLED)
            - request_at: 주문 요청 시각
            - matched_at: 체결 시각 (체결된 경우만)
            
            **거래 상태 설명:**
            - WAITING: 체결 대기 중
            - SUCCESS: 체결 완료
            - FAILED: 체결 실패
            - CANCELLED: 사용자가 취소
            
            **정렬 순서:**
            - 요청 시각 기준 오름차순 정렬
            - 최신 주문이 뒤에 표시
            
            **빈 결과:**
            - 거래 내역이 없으면 Optional.empty() 반환
            - 프론트엔드에서 null 체크 필요
            
            **실시간 업데이트:**
            - 새로운 주문이나 체결 시 내역이 추가/업데이트됩니다
            - WebSocket 알림 수신 후 재조회 권장
            """,
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "거래 내역 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(
                                    type = "array",
                                    implementation = TradeHistoryDto.class
                            ),
                            examples = {
                                    @ExampleObject(
                                            name = "거래 내역 있음",
                                            summary = "다양한 상태의 거래 내역",
                                            value = """
                                            [
                                                {
                                                    "id": 1,
                                                    "member_id": 123,
                                                    "market": "KRW-BTC",
                                                    "side": "BUY",
                                                    "price": 50000000.00,
                                                    "size": 0.001,
                                                    "status": "SUCCESS",
                                                    "request_at": "2025-09-09T10:00:00",
                                                    "matched_at": "2025-09-09T10:05:30"
                                                },
                                                {
                                                    "id": 2,
                                                    "member_id": 123,
                                                    "market": "KRW-ETH",
                                                    "side": "SELL",
                                                    "price": 3200000.00,
                                                    "size": 0.5,
                                                    "status": "WAITING",
                                                    "request_at": "2025-09-09T11:30:00",
                                                    "matched_at": null
                                                },
                                                {
                                                    "id": 3,
                                                    "member_id": 123,
                                                    "market": "KRW-BTC",
                                                    "side": "BUY",
                                                    "price": 48000000.00,
                                                    "size": 0.002,
                                                    "status": "CANCELLED",
                                                    "request_at": "2025-09-09T09:15:00",
                                                    "matched_at": null
                                                }
                                            ]
                                            """
                                    ),
                                    @ExampleObject(
                                            name = "거래 내역 없음",
                                            summary = "거래 내역이 없는 경우",
                                            value = "null"
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    value = """
                                    {
                                        "error": "Unauthorized",
                                        "message": "JWT token is missing or invalid"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(mediaType = "application/json")
            )
    })
    public ResponseEntity<Optional<List<TradeHistoryDto>>> getAllTradeHistories(
            @Parameter(description = "JWT 인증 사용자 정보", hidden = true)
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        try {
            Optional<List<TradeHistoryDto>> tradeHistories = orderbookService.getAllTradeHistories(user);
            return ResponseEntity.status(HttpStatus.OK).body(tradeHistories);
        } catch (Exception e) {
            log.error("Failed to retrieve trade histories for user: {}", user.getUsername(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}