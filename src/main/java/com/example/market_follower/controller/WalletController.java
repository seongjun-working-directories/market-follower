package com.example.market_follower.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.market_follower.dto.upbit.WalletDto;
import com.example.market_follower.service.WalletService;
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

@Slf4j
@RestController
@RequestMapping("/wallet")
@RequiredArgsConstructor
@Tag(name = "Wallet API", description = "지갑 정보 조회 및 관리 API")
public class WalletController {
    private final WalletService walletService;

    @GetMapping("/me")
    @Operation(
            summary = "내 지갑 정보 조회",
            description = """
            현재 로그인한 사용자의 지갑 정보를 조회합니다.
            
            **주요 기능:**
            - 사용 가능한 잔액(balance) 조회
            - 주문 대기 중인 잠금 자금(locked) 조회
            - 지갑 고유 ID 및 소유자 정보 제공
            
            **잔액 정보:**
            - `balance`: 즉시 사용 가능한 KRW 잔액
            - `locked`: 대기 중인 매수 주문에 사용된 잠금 자금
            - 총 보유 자산 = balance + locked
            
            **주문과의 관계:**
            - 매수 주문 시: balance에서 차감되어 locked로 이동
            - 주문 체결 시: locked에서 차감되고 암호화폐 보유량 증가
            - 주문 취소 시: locked에서 balance로 복구
            - 매도 체결 시: 매도 대금이 balance에 추가
            
            **실시간 업데이트:**
            - 주문 체결/취소 시 자동으로 잔액이 업데이트됩니다
            - WebSocket `/topic/orders/{memberId}` 채널을 통해 실시간 알림 수신 후 지갑 정보를 다시 조회하는 것을 권장합니다
            """,
            security = @SecurityRequirement(name = "JWT")
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "지갑 정보 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = WalletDto.class),
                            examples = @ExampleObject(
                                    name = "지갑 정보 응답 예시",
                                    summary = "일반적인 지갑 상태",
                                    value = """
                                    {
                                        "wallet_id": 1,
                                        "member_id": 123,
                                        "balance": 1500000.00,
                                        "locked": 250000.00
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "인증 실패 - JWT 토큰이 없거나 유효하지 않음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "인증 오류",
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
                    responseCode = "404",
                    description = "지갑을 찾을 수 없음 - 사용자에게 연결된 지갑이 없음",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "지갑 없음",
                                    value = """
                                    {
                                        "error": "Not Found",
                                        "message": "Wallet not found for the user"
                                    }
                                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            mediaType = "application/json",
                            examples = @ExampleObject(
                                    name = "서버 오류",
                                    value = """
                                    {
                                        "error": "Internal Server Error",
                                        "message": "An unexpected error occurred"
                                    }
                                    """
                            )
                    )
            )
    })
    public ResponseEntity<WalletDto> getMyWallet(
            @Parameter(
                    description = "JWT 인증을 통해 자동으로 주입되는 사용자 정보",
                    hidden = true
            )
            @AuthenticationPrincipal org.springframework.security.core.userdetails.User user
    ) {
        WalletDto walletDto = walletService.getMyWalletByUserDetails(user);
        return ResponseEntity.status(HttpStatus.OK).body(walletDto);
    }
}