package com.example.market_follower.controller;

import com.example.market_follower.dto.MemberLoginResponseDto;
import com.example.market_follower.exception.DuplicateEmailException;
import com.example.market_follower.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDate;

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

    @Getter
    @Setter
    @Schema(description = "회원가입 요청")
    public static class SignupRequest {
        @NotBlank(message = "이메일은 필수입니다.")
        @Email(message = "유효한 이메일 형식이어야 합니다.")
        @Schema(description = "이메일", example = "example@gmail.com")
        private String email;

        @NotBlank(message = "이름은 필수입니다.")
        @Schema(description = "이름", example = "홍길동")
        private String name;

        @NotBlank(message = "핸드폰 번호는 필수입니다.")
        @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "핸드폰 번호 형식이 올바르지 않습니다. 예: 010-1234-5678")
        @Schema(description = "핸드폰 번호", example = "010-1234-1234")
        private String phoneNumber;

        @NotNull(message = "생일은 필수입니다.")
        @Schema(description = "생일", example = "2000-01-01")
        private LocalDate birthday;
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

    @PostMapping("/signup")
    @Operation(
            summary = "회원가입",
            description = "이메일, 이름, 핸드폰 번호, 생일 정보를 받아 새로운 회원을 등록합니다.",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "회원가입 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "성공",
                                            description = "회원가입이 성공적으로 완료된 경우"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청 (유효성 검사 실패)",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "string"),
                                    examples = {
                                            @ExampleObject(
                                                    name = "이메일 형식 오류",
                                                    value = "유효한 이메일 형식이어야 합니다."
                                            ),
                                            @ExampleObject(
                                                    name = "필수 입력값 누락",
                                                    value = "이름은 필수입니다."
                                            ),
                                            @ExampleObject(
                                                    name = "핸드폰 번호 형식 오류",
                                                    value = "핸드폰 번호 형식이 올바르지 않습니다. 예: 010-1234-5678"
                                            )
                                    }
                            )
                    ),
                    @ApiResponse(
                            responseCode = "409",
                            description = "이미 존재하는 이메일",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(type = "string"),
                                    examples = @ExampleObject(
                                            name = "중복 이메일",
                                            value = "이미 등록된 이메일입니다."
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
                                            value = "회원가입 처리 중 서버 오류가 발생했습니다."
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<?> signupWithGoogle(
            @Valid @RequestBody SignupRequest request,
            BindingResult bindingResult
    ) {
        try {
            // 유효성 검사
            if (bindingResult.hasErrors()) {
                String errorMsg = bindingResult.getAllErrors()
                        .stream()
                        .map(error -> error.getDefaultMessage())
                        .findFirst()
                        .orElse("입력값이 올바르지 않습니다.");
                return ResponseEntity.badRequest().body(errorMsg);
            }

            authService.signup(request);

            return ResponseEntity.ok().build();
        } catch (DuplicateEmailException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            log.error("회원가입 중 오류 발생", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("회원가입 처리 중 서버 오류가 발생했습니다.");
        }
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<String> handleRuntimeException(RuntimeException e) {
        log.error("AuthController에서 예외 발생", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("인증 처리 중 오류가 발생했습니다");
    }
}