package com.example.market_follower.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "Basic API", description = "기본 API")
public class BasicController {
    @GetMapping("/fun")
    @Operation(
            summary = "그냥 만듦",
            description = "그냥 재미로 만듦",
            responses = {
                    @ApiResponse(responseCode = "200", description = "성공")
            }
    )
    public ResponseEntity<Void> getFun() {
        return ResponseEntity.status(HttpStatus.OK).build();
    }
}
