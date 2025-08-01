package com.example.market_follower.controller;

import com.example.market_follower.dto.MemberLoginResponseDto;
import com.example.market_follower.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

@Slf4j
@RestController
@RequestMapping(value = "/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "사용자 인증 API")
public class AuthController {
    private final AuthService authService;

    @Getter
    @Setter
    @Schema(description = "구글 액세스 토큰 요청")
    public static class GoogleTokenRequest {
        @Schema(description = "구글 OAuth2 액세스 토큰", example = "ya29.a0AfH6SMC...")
        private String accessToken;
    }

    @PostMapping("/google")
    @Operation(
            summary = "구글 Access Token을 기반으로 사용자 인증",
            description = "구글 Access Token을 받아 사용자 정보 확인 후 회원가입 또는 로그인 처리합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "인증 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = MemberLoginResponseDto.class),
                                    examples = {
                                            @ExampleObject(
                                                    name = "기존 회원",
                                                    value = """
                                                    {
                                                      "status": "REGISTERED",
                                                      "email": "user@example.com",
                                                      "name": "홍길동",
                                                      "memberId": 123
                                                    }
                                                    """
                                            ),
                                            @ExampleObject(
                                                    name = "신규 회원",
                                                    value = """
                                                    {
                                                      "status": "NOT_REGISTERED",
                                                      "email": "newuser@example.com",
                                                      "name": "신규사용자"
                                                    }
                                                    """
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (accessToken이 누락되거나 빈 값)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "요청 오류",
                                            description = "accessToken이 제공되지 않았거나 빈 문자열인 경우"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패 (유효하지 않은 구글 토큰)",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "인증 실패",
                                            description = "구글 API에서 토큰이 유효하지 않다고 응답한 경우"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "500",
                            description = "서버 내부 오류",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "string"),
                                    examples = @ExampleObject(
                                            name = "서버 오류",
                                            value = "인증 처리 중 오류가 발생했습니다"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<MemberLoginResponseDto> authenticateWithGoogle(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "구글 OAuth2 액세스 토큰을 포함한 JSON 요청",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GoogleTokenRequest.class),
                            examples = @ExampleObject(
                                    name = "요청 예시",
                                    value = """
                                    {
                                      "accessToken": "ya29.a0AfH6SMC_example_token"
                                    }
                                    """
                            )
                    )
            )
            @RequestBody GoogleTokenRequest request) {

        // 입력값 검증
        if (request == null || !StringUtils.hasText(request.getAccessToken())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }

        try {
            MemberLoginResponseDto memberLoginResponseDto = authService.loginWithGoogle(request.getAccessToken());
            return ResponseEntity.status(HttpStatus.OK).body(memberLoginResponseDto);
        } catch (Exception e) {
            log.error("Google 인증 처리 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("AuthController에서 예외 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("인증 처리 중 오류가 발생했습니다");
    }
}