package com.example.market_follower.controller;

import com.example.market_follower.dto.MemberLoginResponseDto;
import com.example.market_follower.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/auth")
@RequiredArgsConstructor
@Tag(name = "Auth API", description = "사용자 인증 API")
public class AuthController {
    private final AuthService authService;

    @Getter
    @Setter
    public static class GoogleTokenRequest {
        private String accessToken;
    }

    @PostMapping("/google")
    @Operation(
            summary = "구글 Access Token 을 기반으로 사용자 인증",
            description = "구글 Access Token 을 받아 사용자 정보 확인 후 회원가입 또는 로그인 처리",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공", content = @Content(schema = @Schema(implementation = MemberLoginResponseDto.class))),
                    @ApiResponse(responseCode = "400", description = "잘못된 요청"),
                    @ApiResponse(responseCode = "401", description = "인증 실패")
            }
    )
    public ResponseEntity<MemberLoginResponseDto> authenticateWithGoogle(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "구글 Access Token JSON",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = GoogleTokenRequest.class)
                    )
            )
            @RequestBody GoogleTokenRequest request) {

        MemberLoginResponseDto memberLoginResponseDto = authService.loginWithGoogle(request.getAccessToken());
        return ResponseEntity.status(HttpStatus.OK).body(memberLoginResponseDto);
    }
}
