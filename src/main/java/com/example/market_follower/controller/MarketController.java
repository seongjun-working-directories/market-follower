package com.example.market_follower.controller;

import com.example.market_follower.dto.upbit.TradableCoinDto;
import com.example.market_follower.dto.upbit.UpbitTickerDto;
import com.example.market_follower.service.MarketService;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping(value = "/market")
@RequiredArgsConstructor
@Tag(name = "Market API", description = "암호화폐 시장 정보 조회 API")
public class MarketController {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final MarketService marketService;

    @GetMapping("/list")
    @Operation(
            summary = "거래 가능한 코인 목록 조회",
            description = """
                업비트에서 거래 가능한 모든 암호화폐 목록을 반환합니다.
                각 코인의 마켓 코드, 한글명, 영문명 등의 기본 정보를 제공합니다.
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "거래 가능한 코인 목록 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = TradableCoinDto.class)),
                                    examples = @ExampleObject(
                                            name = "코인 목록",
                                            summary = "거래 가능한 코인 목록 예시",
                                            value = """
                                            [
                                              {
                                                "market": "KRW-BTC",
                                                "korean_name": "비트코인",
                                                "english_name": "Bitcoin"
                                              },
                                              {
                                                "market": "KRW-ETH",
                                                "korean_name": "이더리움",
                                                "english_name": "Ethereum"
                                              }
                                            ]
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "인증 실패",
                                            summary = "JWT 토큰이 없거나 유효하지 않음",
                                            description = "Authorization 헤더가 없거나 유효하지 않은 JWT 토큰인 경우"
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
                                            summary = "예상치 못한 서버 오류",
                                            description = "데이터 조회 중 서버에서 오류가 발생한 경우"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<List<TradableCoinDto>> getTradableCoinList() {
        try {
            List<TradableCoinDto> coins = marketService.getAllTradableCoins();
            return ResponseEntity.status(HttpStatus.OK).body(coins);
        } catch (Exception e) {
            log.error("Error fetching tradable coins", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @GetMapping("/ticker/{market}")
    @Operation(
            summary = "특정 마켓의 현재가 정보 조회",
            description = """
                지정된 마켓의 실시간 현재가 정보를 조회합니다.
                Redis 캐시에서 데이터를 가져오므로 빠른 응답을 제공합니다.
                
                **마켓 코드 형식:**
                - 원화 마켓: KRW-BTC, KRW-ETH 등
                - 비트코인 마켓: BTC-ETH, BTC-ADA 등
                - 테더 마켓: USDT-BTC, USDT-ETH 등
                """,
            parameters = @Parameter(
                    name = "market",
                    description = "마켓 코드 (예: KRW-BTC, KRW-ETH)",
                    required = true,
                    schema = @Schema(type = "string", pattern = "^[A-Z]{3,4}-[A-Z0-9]{2,10}$"),
                    examples = {
                            @ExampleObject(name = "비트코인", value = "KRW-BTC", summary = "원화 비트코인 마켓"),
                            @ExampleObject(name = "이더리움", value = "KRW-ETH", summary = "원화 이더리움 마켓"),
                            @ExampleObject(name = "리플", value = "KRW-XRP", summary = "원화 리플 마켓")
                    }
            ),
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "현재가 정보 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    schema = @Schema(implementation = UpbitTickerDto.class),
                                    examples = @ExampleObject(
                                            name = "비트코인 현재가",
                                            summary = "KRW-BTC 현재가 정보",
                                            value = """
                                            {
                                              "market": "KRW-BTC",
                                              "trade_date": "20240814",
                                              "trade_time": "143052",
                                              "trade_date_kst": "20240814",
                                              "trade_time_kst": "233052",
                                              "trade_timestamp": 1723642252000,
                                              "opening_price": 88500000.0,
                                              "high_price": 89200000.0,
                                              "low_price": 87800000.0,
                                              "trade_price": 88900000.0,
                                              "prev_closing_price": 88500000.0,
                                              "change": "RISE",
                                              "change_price": 400000.0,
                                              "change_rate": 0.0045,
                                              "signed_change_price": 400000.0,
                                              "signed_change_rate": 0.0045,
                                              "trade_volume": 0.02451875,
                                              "acc_trade_price": 123456789012.34,
                                              "acc_trade_price_24h": 987654321098.76,
                                              "acc_trade_volume": 1389.12345678,
                                              "acc_trade_volume_24h": 11123.87654321,
                                              "highest_52_week_price": 95000000.0,
                                              "highest_52_week_date": "2024-03-14",
                                              "lowest_52_week_price": 42000000.0,
                                              "lowest_52_week_date": "2024-01-15",
                                              "timestamp": 1723642252123
                                            }
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "인증 실패",
                                            summary = "JWT 토큰이 없거나 유효하지 않음",
                                            description = "Authorization 헤더가 없거나 유효하지 않은 JWT 토큰인 경우"
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "마켓을 찾을 수 없음",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "마켓 없음",
                                            summary = "존재하지 않는 마켓 코드",
                                            description = "해당 마켓의 데이터가 Redis에 존재하지 않는 경우"
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
                                            summary = "예상치 못한 서버 오류",
                                            description = "Redis 조회 또는 JSON 파싱 중 오류가 발생한 경우"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<UpbitTickerDto> getTicker(@PathVariable String market) throws Exception {
        try {
            String key = "upbit:ticker:" + market;
            String json = redisTemplate.opsForValue().get(key);
            UpbitTickerDto dto = json != null ? objectMapper.readValue(json, UpbitTickerDto.class) : null;
            if (dto != null) {
                return ResponseEntity.status(HttpStatus.OK).body(dto);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }
        } catch (Exception e) {
            log.error("Error fetching ticker for market {}", market, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/ticker/all")
    @Operation(
            summary = "모든 마켓의 현재가 정보 조회",
            description = """
                Redis에 캐시된 모든 마켓의 실시간 현재가 정보를 일괄 조회합니다.
                업비트에서 거래되는 모든 암호화폐의 현재가를 한 번에 가져올 수 있습니다.
                
                **주의사항:**
                - 데이터 크기가 클 수 있으므로 필요시에만 사용하세요
                - Redis keys 패턴 검색을 사용하므로 성능에 주의하세요
                """,
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "전체 현재가 정보 조회 성공",
                            content = @Content(
                                    mediaType = "application/json",
                                    array = @ArraySchema(schema = @Schema(implementation = UpbitTickerDto.class)),
                                    examples = @ExampleObject(
                                            name = "전체 현재가",
                                            summary = "모든 마켓의 현재가 정보",
                                            value = """
                                            [
                                              {
                                                "market": "KRW-BTC",
                                                "trade_price": 88900000.0,
                                                "change": "RISE",
                                                "change_rate": 0.0045,
                                                "acc_trade_price_24h": 987654321098.76
                                              },
                                              {
                                                "market": "KRW-ETH",
                                                "trade_price": 3520000.0,
                                                "change": "FALL",
                                                "change_rate": -0.0023,
                                                "acc_trade_price_24h": 456789012345.67
                                              }
                                            ]
                                            """
                                    )
                            )
                    ),
                    @ApiResponse(
                            responseCode = "401",
                            description = "인증 실패",
                            content = @Content(
                                    mediaType = "application/json",
                                    examples = @ExampleObject(
                                            name = "인증 실패",
                                            summary = "JWT 토큰이 없거나 유효하지 않음",
                                            description = "Authorization 헤더가 없거나 유효하지 않은 JWT 토큰인 경우"
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
                                            summary = "예상치 못한 서버 오류",
                                            description = "Redis 키 조회 또는 JSON 파싱 중 오류가 발생한 경우"
                                    )
                            )
                    )
            }
    )
    public ResponseEntity<List<UpbitTickerDto>> getAllTickers() throws Exception {
        try {
            // Redis에서 전체 마켓 키 패턴 가져오기
            Set<String> keys = redisTemplate.keys("upbit:ticker:*");
            List<UpbitTickerDto> tickers = new ArrayList<>();
            if (keys != null) {
                for (String key : keys) {
                    String json = redisTemplate.opsForValue().get(key);
                    if (json != null) {
                        tickers.add(objectMapper.readValue(json, UpbitTickerDto.class));
                    }
                }
            }
            return ResponseEntity.status(HttpStatus.OK).body(tickers);
        } catch (Exception e) {
            log.error("Error fetching all tickers", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
