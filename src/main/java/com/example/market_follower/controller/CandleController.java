package com.example.market_follower.controller;

import com.example.market_follower.service.CandleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

@Slf4j
@RestController
@RequestMapping(value = "/candle")
@RequiredArgsConstructor
@Tag(name = "Candle API", description = "암호화폐 캔들 정보 조회 API")
public class CandleController {
    private final CandleService candleService;

    @GetMapping("/all")
    @Operation(
            summary = "전체 캔들 데이터 조회 (비동기)",
            description = "모든 코인의 캔들 데이터를 비동기로 조회합니다. 앱 최초 다운로드 후 로딩 시 또는 앱 LocalStorage가 비어있을 때 사용합니다.",
            tags = {"데이터 조회"}
    )
    @Parameter(
            name = "is_krw_market",
            description = "KRW 마켓 조회 여부. true: KRW-로 시작하는 마켓만 조회, false: KRW-로 시작하지 않는 마켓만 조회",
            required = true,
            example = "true",
            schema = @Schema(type = "boolean")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "전체 캔들 데이터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "KRW 마켓 성공 응답 예시",
                                            value = """
                        {
                          "upbit_candle_7d": [
                            {
                              "market": "KRW-BTC",
                              "candle_date_time_utc": "2025-08-29T00:00:00",
                              "candle_date_time_kst": "2025-08-29T09:00:00",
                              "opening_price": 85000000.0,
                              "high_price": 87000000.0,
                              "low_price": 84000000.0,
                              "trade_price": 86000000.0,
                              "candle_acc_trade_price": 1234567890.0,
                              "candle_acc_trade_volume": 14.35
                            }
                          ],
                          "upbit_candle_30d": [...],
                          "upbit_candle_3m": [...],
                          "upbit_candle_1y": [...],
                          "upbit_candle_5y": [...]
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "비 KRW 마켓 성공 응답 예시",
                                            value = """
                        {
                          "upbit_candle_7d": [
                            {
                              "market": "BTC-ETH",
                              "candle_date_time_utc": "2025-08-29T00:00:00",
                              "candle_date_time_kst": "2025-08-29T09:00:00",
                              "opening_price": 0.042,
                              "high_price": 0.045,
                              "low_price": 0.041,
                              "trade_price": 0.043,
                              "candle_acc_trade_price": 123.456,
                              "candle_acc_trade_volume": 2845.67
                            }
                          ],
                          "upbit_candle_30d": [...],
                          "upbit_candle_3m": [...],
                          "upbit_candle_1y": [...],
                          "upbit_candle_5y": [...]
                        }
                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(
                            schema = @Schema(implementation = Void.class)
                    )
            )
    })
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getAllCandleData(@RequestParam boolean is_krw_market) {
        return candleService.getAllCandleDataAsync(is_krw_market)
                .thenApply(data -> ResponseEntity.status(HttpStatus.OK).body(data))
                .exceptionally(ex -> {
                    log.error("Error fetching all candle data");
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                });
    }

    @GetMapping("/since")
    @Operation(
            summary = "특정 시점 이후 캔들 데이터 조회 (비동기)",
            description = "지정된 날짜 이후의 캔들 데이터를 비동기로 조회합니다. 그 날 최초 로그인 시 사용합니다.",
            tags = {"데이터 조회"}
    )
    @Parameter(
            name = "period",
            description = "조회 시작 날짜 (ISO 날짜 형식: YYYY-MM-DD)",
            required = true,
            example = "2025-08-26",
            schema = @Schema(type = "string", format = "date")
    )
    @Parameter(
            name = "is_krw_market",
            description = "KRW 마켓 조회 여부. true: KRW-로 시작하는 마켓만 조회, false: KRW-로 시작하지 않는 마켓만 조회",
            required = true,
            example = "true",
            schema = @Schema(type = "boolean")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "특정 시점 이후 캔들 데이터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = Map.class),
                            examples = {
                                    @ExampleObject(
                                            name = "KRW 마켓 성공 응답 예시",
                                            value = """
                        {
                          "upbit_candle_7d": [
                            {
                              "market": "KRW-BTC",
                              "candle_date_time_utc": "2025-08-26T00:00:00",
                              "candle_date_time_kst": "2025-08-26T09:00:00",
                              "opening_price": 85000000.0,
                              "high_price": 87000000.0,
                              "low_price": 84000000.0,
                              "trade_price": 86000000.0
                            }
                          ],
                          "upbit_candle_30d": [...],
                          "upbit_candle_3m": [...],
                          "upbit_candle_1y": [...],
                          "upbit_candle_5y": [...]
                        }
                        """
                                    ),
                                    @ExampleObject(
                                            name = "비 KRW 마켓 성공 응답 예시",
                                            value = """
                        {
                          "upbit_candle_7d": [
                            {
                              "market": "BTC-ETH",
                              "candle_date_time_utc": "2025-08-26T00:00:00",
                              "candle_date_time_kst": "2025-08-26T09:00:00",
                              "opening_price": 0.042,
                              "high_price": 0.045,
                              "low_price": 0.041,
                              "trade_price": 0.043
                            }
                          ],
                          "upbit_candle_30d": [...],
                          "upbit_candle_3m": [...],
                          "upbit_candle_1y": [...],
                          "upbit_candle_5y": [...]
                        }
                        """
                                    )
                            }
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 날짜 형식",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    public CompletableFuture<ResponseEntity<Map<String, Object>>> getAllCandleDataSince(@RequestParam String period, @RequestParam boolean is_krw_market) {
        return candleService.getAllCandleDataSinceAsync(period, is_krw_market)
                .thenApply(data -> ResponseEntity.status(HttpStatus.OK).body(data))
                .exceptionally(ex -> {
                    log.error("Error fetching candle data since {}", period);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
                });
    }

    @PutMapping("/upsert/manual")
    @Operation(
            summary = "캔들 데이터 수동 업데이트",
            description = "모든 캔들 데이터를 수동으로 업데이트합니다. 정기 스케줄러 외에 필요시 수동 실행할 수 있습니다.",
            tags = {"데이터 관리"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "캔들 데이터 업데이트 성공"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "캔들 데이터 업데이트 실패",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    public ResponseEntity<Void> upsertCandleData() {
        try {
            candleService.updateAllCandleData();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }  catch (Exception e) {
            log.error("Error manually updating candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/old/manual")
    @Operation(
            summary = "오래된 캔들 데이터 수동 삭제",
            description = "지정된 기간을 초과한 오래된 캔들 데이터를 수동으로 삭제합니다.",
            tags = {"데이터 관리"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "오래된 캔들 데이터 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "오래된 캔들 데이터 삭제 실패",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    public ResponseEntity<Void> deleteOldCandleData() {
        try {
            candleService.removeOldCandles();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }  catch (Exception e) {
            log.error("Error manually deleting old candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @DeleteMapping("/delete/invalid/coins/manual")
    @Operation(
            summary = "유효하지 않은 코인 데이터 수동 삭제",
            description = "상장폐지되거나 더 이상 거래되지 않는 코인의 캔들 데이터를 수동으로 삭제합니다.",
            tags = {"데이터 관리"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "유효하지 않은 코인 데이터 삭제 성공"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "유효하지 않은 코인 데이터 삭제 실패",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    public ResponseEntity<Void> deleteInvalidCoins() {
        try {
            candleService.deleteInvalidCandles();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        } catch (Exception e) {
            log.error("Error deleting invalid candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/daily")
    @Operation(
            summary = "특정 코인의 일별 캔들 데이터 조회",
            description = "지정된 마켓의 일별 캔들 데이터를 조회합니다.",
            tags = {"데이터 조회"}
    )
    @Parameter(
            name = "market",
            description = "조회할 마켓 코드 (예: KRW-BTC, KRW-ETH)",
            required = true,
            example = "KRW-BTC",
            schema = @Schema(type = "string")
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "일별 캔들 데이터 조회 성공",
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = List.class),
                            examples = @ExampleObject(
                                    name = "성공 응답 예시",
                                    value = """
                    [
                      {
                        "market": "KRW-BTC",
                        "candle_date_time_utc": "2025-08-29T00:00:00",
                        "candle_date_time_kst": "2025-08-29T09:00:00",
                        "opening_price": 85000000.0,
                        "high_price": 87000000.0,
                        "low_price": 84000000.0,
                        "trade_price": 86000000.0,
                        "candle_acc_trade_price": 1234567890.0,
                        "candle_acc_trade_volume": 14.35,
                        "prev_closing_price": 84500000.0,
                        "change_price": 1500000.0,
                        "change_rate": 0.0177
                      }
                    ]
                    """
                            )
                    )
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "잘못된 마켓 코드",
                    content = @Content(schema = @Schema(implementation = Void.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "서버 내부 오류",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    public ResponseEntity<List<Map<String, Object>>> getDailyCandleData(@RequestParam String market) {
        try {
            List<Map<String, Object>> data = candleService.getDailyCandleData(market);
            return ResponseEntity.status(HttpStatus.OK).body(data);
        } catch (Exception e) {
            log.error("Error reading daily candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PutMapping("/upsert/daily/manual")
    @Operation(
            summary = "일별 캔들 데이터 수동 업데이트",
            description = "모든 코인의 일별 캔들 데이터를 정리하고 다시 초기화합니다. 기존 일별 데이터를 정리한 후 새로운 데이터로 업데이트합니다.",
            tags = {"데이터 관리"}
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "일별 캔들 데이터 업데이트 성공"
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "일별 캔들 데이터 업데이트 실패",
                    content = @Content(schema = @Schema(implementation = Void.class))
            )
    })
    public ResponseEntity<Void> upsertDailyCandleData() {
        try {
            candleService.cleanupDailyCandleData();
            candleService.initializeAllDailyCandleData();
            return ResponseEntity.status(HttpStatus.OK).body(null);
        }  catch (Exception e) {
            log.error("Error manually updating daily candle data", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}