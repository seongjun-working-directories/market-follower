package com.example.market_follower.controller;

import com.example.market_follower.service.CandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping(value = "/candle")
@RequiredArgsConstructor
@Tag(name = "Candle API", description = "암호화폐 캔들 정보 조회 API")
public class CandleController {
    private final CandleService candleService;

    @GetMapping("/initialization")
    @Operation(
            summary = "캔들 데이터 초기화",
            description = """
                지정된 기간의 캔들 데이터를 초기화합니다.
                업비트 API에서 캔들 데이터를 가져와 데이터베이스에 저장합니다.
                중복 데이터는 자동으로 필터링되며, 모든 거래 가능한 코인에 대해 순차적으로 처리됩니다.
                데이터 초기화는 시간이 오래 걸릴 수 있으므로 주의해서 사용해야 합니다.
                """,
            parameters = {
                    @Parameter(
                            name = "type",
                            description = """
                                초기화할 캔들 기간 타입:
                                - A: 7일 (1시간 간격, 168개 데이터)
                                - B: 30일 (4시간 간격, 180개 데이터) 
                                - C: 3개월 (일봉, 90개 데이터)
                                - D: 1년 (일봉, 365개 데이터)
                                - E: 5년 (주봉, 260개 데이터)
                                """,
                            required = true,
                            example = "A"
                    )
            },
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "캔들 데이터 초기화 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "초기화 성공",
                                            summary = "캔들 데이터 초기화 완료",
                                            description = "지정된 타입의 캔들 데이터가 성공적으로 초기화됨"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "400",
                            description = "잘못된 요청",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "잘못된 타입",
                                            summary = "지원하지 않는 캔들 타입",
                                            description = "type 파라미터가 없거나 A, B, C, D, E 이외의 값인 경우"
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
                                            summary = "캔들 데이터 초기화 중 오류",
                                            description = "데이터 초기화 중 서버에서 오류가 발생한 경우"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> initialize(@RequestParam String type) {
        try {
            candleService.initializeCandles(type);
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            log.error("Error initializing candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/invalid")
    @Operation(
            summary = "유효하지 않은 캔들 데이터 삭제",
            description = """
                현재 거래 불가능한 코인들의 캔들 데이터를 삭제합니다.
                업비트에서 상장 폐지되거나 거래가 중단된 코인들의 캔들 데이터를 정리하여
                데이터베이스의 일관성을 유지합니다. 모든 캔들 기간(7일, 30일, 3개월, 1년, 5년)의
                데이터를 대상으로 정리 작업을 수행합니다.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "거래 불가능한 코인의 캔들 데이터 삭제 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "삭제 성공",
                                            summary = "거래 불가능한 코인 데이터 삭제 완료",
                                            description = "현재 거래 불가능한 코인들의 캔들 데이터가 성공적으로 삭제됨"
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
                                            summary = "데이터 삭제 중 오류",
                                            description = "거래 불가능한 코인 데이터 삭제 중 서버에서 오류가 발생한 경우"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<Void> deleteInvalid() {
        try {
            candleService.deleteInvalidCandles();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            log.error("Error deleting invalid candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}