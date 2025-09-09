# Market Follower

> 업비트 암호화폐 실시간 시세 및 가상 거래 서비스

[![Java](https://img.shields.io/badge/Java-17-orange)](https://openjdk.java.net/projects/jdk/17/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen)](https://spring.io/projects/spring-boot)
[![MySQL](https://img.shields.io/badge/MySQL-8.0-blue)](https://www.mysql.com/)
[![Redis](https://img.shields.io/badge/Redis-Cache-red)](https://redis.io/)
[![Kafka](https://img.shields.io/badge/Apache-Kafka-black)](https://kafka.apache.org/)
[![WebSocket](https://img.shields.io/badge/WebSocket-STOMP-yellow)](https://stomp.github.io/)

## 🚀 주요 기능

### 📊 실시간 데이터
- **실시간 암호화폐 시세** - 업비트 500여개 코인 현재가 정보
- **실시간 호가 데이터** - 매수/매도 호가 10단계 정보
- **WebSocket 스트리밍** - 10초마다 최신 시세/호가 자동 업데이트
- **빠른 응답** - Redis 캐시로 밀리초 단위 응답

### 📈 차트 데이터
- **다양한 시간대** - 7일, 30일, 3개월, 1년, 5년 캔들 데이터
- **실시간 캔들** - 5분마다 최신 캔들 업데이트
- **데이터 최적화** - 기간별 최적화된 캔들 간격 (1시간~1주)

### 💰 가상 거래 시스템
- **매수/매도 주문** - 실제 거래소와 유사한 주문 시스템
- **자동 체결** - 5초마다 호가 기준 자동 체결
- **포트폴리오 관리** - 보유 코인 및 수익률 추적
- **거래 내역** - 전체 주문/체결 내역 조회
- **실시간 알림** - 주문 체결 시 WebSocket 알림

### 🔐 사용자 관리
- **구글 소셜 로그인** - 간편한 OAuth2 인증
- **JWT 토큰 인증** - 안전한 API 접근 관리
- **개인별 지갑** - KRW 잔액 및 암호화폐 보유량 관리

## 🛠 기술 스택

- **Backend**: Spring Boot 3.x, Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis (실시간 데이터 캐싱)
- **Message Queue**: Apache Kafka (데이터 스트리밍)
- **Real-time**: WebSocket (STOMP) (실시간 알림)
- **Security**: Spring Security, JWT, OAuth2
- **Documentation**: Swagger/OpenAPI 3.0
- **Infrastructure**: Docker, AWS EC2
- **CI/CD**: GitHub Actions

## 📋 API 문서

🔗 [Swagger UI 문서](http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html)

### 🎯 시세 및 차트 API

```http
GET  /market/list               # 거래 가능한 코인 목록
GET  /market/ticker/{market}    # 특정 코인 현재가
GET  /market/ticker/all         # 전체 코인 현재가
GET  /candle/all                # 전체 캔들 데이터
GET  /candle/daily?market=      # 특정 코인 일별 캔들
```

### 🔐 인증 API

```http
POST /auth/google               # 구글 로그인
POST /auth/signup               # 회원가입
```

### 💰 지갑 및 거래 API

```http
GET  /wallet/me                 # 내 지갑 정보 조회
POST /orderbook/request         # 매수/매도 주문 요청
POST /orderbook/cancel/{id}     # 주문 취소
GET  /orderbook/holding/all     # 보유 코인 조회
GET  /orderbook/history/all     # 거래 내역 조회
```

### 📡 WebSocket 채널

Market Follower는 3가지 타입의 실시간 WebSocket 채널을 제공합니다:

#### 1. 시세(Ticker) 채널
```javascript
// 전체 코인 시세 구독 (10초마다 600개 코인 일괄 전송)
stompClient.subscribe('/topic/ticker/all', message => {
    const allTickers = JSON.parse(message.body);
    console.log(`${allTickers.length}개 코인 시세 업데이트`);
});

// 특정 코인 시세 구독
stompClient.subscribe('/topic/ticker/KRW-BTC', message => {
    const btcTicker = JSON.parse(message.body);
    console.log(`BTC 현재가: ${btcTicker.trade_price}원`);
});
```

#### 2. 호가(Orderbook) 채널
```javascript
// 특정 코인 호가 구독 (10초마다 업데이트)
stompClient.subscribe('/topic/orderbook/KRW-BTC', message => {
    const btcOrderbook = JSON.parse(message.body);
    console.log('BTC 매수호가:', btcOrderbook.orderbook_units[0].bid_price);
    console.log('BTC 매도호가:', btcOrderbook.orderbook_units[0].ask_price);
});
```

#### 3. 주문 체결 알림 채널 (개인별)
```javascript
// 개인 주문 체결 알림 구독
const memberId = extractMemberIdFromJWT(jwtToken);
stompClient.subscribe(`/topic/orders/${memberId}`, message => {
    const tradeNotification = JSON.parse(message.body);
    console.log('주문 체결:', tradeNotification);
    // 실시간으로 포트폴리오 업데이트
    updatePortfolio(tradeNotification);
});
```

## 🏃‍♂️ 빠른 시작

### 1. 저장소 클론
```bash
git clone https://github.com/seongjun-working-directories/market-follower.git
cd market-follower
```

### 2. 환경 변수 설정
```bash
# .env 파일 생성
API_ACCESS=your_upbit_api_key
API_SECRET=your_upbit_secret_key
GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret
JWT_SECRET=your_jwt_secret_key
```

### 3. Docker로 실행
```bash
docker-compose up -d
```

### 4. 애플리케이션 접속
- **백엔드 서버**: http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080
- **API 문서**: http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html
- **WebSocket 연결**: `ws://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/ws?token=JWT토큰`

## 🏗 시스템 아키텍처

```
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌─────────────┐
│ Upbit API   │ => │ Kafka        │ => │ Consumer    │ => │ Redis Cache │
│ (REST/WS)   │    │ Producer     │    │ Service     │    │ (3분 TTL)   │
└─────────────┘    └──────────────┘    └─────────────┘    └─────────────┘
                                                                    │
                                                                    ▼
┌─────────────┐    ┌──────────────┐    ┌─────────────┐    ┌─────────────┐
│ Client      │ <= │ WebSocket    │ <= │ Spring Boot │ <= │ MySQL DB    │
│ (Frontend)  │    │ (STOMP)      │    │ API Server  │    │ (거래데이터) │
└─────────────┘    └──────────────┘    └─────────────┘    └─────────────┘
```

### 데이터 흐름

1. **데이터 수집**: Kafka Producer가 업비트 API에서 실시간 데이터 수집
2. **데이터 처리**: Kafka Consumer가 메시지를 처리하여 Redis에 저장
3. **실시간 전송**: 10초마다 Redis에서 최신 데이터를 조회하여 WebSocket으로 브로드캐스팅
4. **거래 처리**: 5초마다 대기 중인 주문의 체결 조건을 확인하여 자동 체결
5. **알림 전송**: 주문 체결 시 개인별 WebSocket 채널로 실시간 알림 전송

## 📊 데이터 수집 및 업데이트 스케줄

| 데이터 타입 | 수집 주기 | 브로드캐스팅 주기 | 설명 |
|------------|----------|------------------|------|
| 실시간 시세 | 실시간 | 10초마다 | 600개 코인 현재가 |
| 호가 데이터 | 실시간 | 10초마다 | 매수/매도 호가 10단계 |
| 주문 체결 확인 | - | 5초마다 | 대기 주문 체결 조건 확인 |
| 캔들 데이터 | 매일 09:05 | - | 전체 기간별 캔들 동기화 |
| 거래 코인 목록 | 매일 08:40 | - | 신규/상장폐지 코인 업데이트 |

## 💾 데이터베이스 설계

### 🗄️ [데이터베이스 스키마 상세 보기](schema.md)

### 주요 테이블 구조

#### 사용자 관리
- **`member`** - 사용자 기본 정보 (이름, 이메일, 가입일 등)
- **`auth`** - 사용자 권한 정보 (역할 기반 접근 제어)
- **`wallet`** - 개인별 KRW 지갑 (잔액, 주문 잠금 자금)

#### 거래 시스템
- **`holding`** - 암호화폐 보유량 (보유 수량, 평균 단가, 잠금 수량)
- **`trade_history`** - 거래 내역 (주문, 체결, 취소 기록)
- **`tradable_coin`** - 거래 가능한 코인 목록 및 주의사항

#### 시세 데이터
- **`upbit_ticker`** - 실시간 시세 정보
- **`upbit_candle_*`** - 기간별 캔들 데이터 (7d, 30d, 3m, 1y, 5y)

### 데이터 볼륨
- **시세 데이터**: 약 600개 코인 × 실시간 업데이트
- **캔들 데이터**: 약 500,000+ 레코드 (5개 기간 × 600개 코인 × 365일)
- **거래 데이터**: 사용자별 무제한 (자동 파티셔닝 고려 중)

## 🔧 주요 설정

### Redis 캐시 구조
```
upbit:ticker:{MARKET}           # 시세 데이터 (TTL: 3분)
upbit:orderbook:{MARKET}        # 호가 데이터 (TTL: 3분)
upbit:candle:1d:{MARKET}        # 일별 캔들 (TTL: 24시간)
```

### 환경별 설정
- **개발환경**: H2 인메모리 DB, 로컬 Redis
- **스테이징**: MySQL 8.0, Redis Cluster
- **운영환경**: AWS RDS MySQL, AWS ElastiCache

## 🎮 가상 거래 시스템

### 거래 프로세스

#### 매수 주문
1. 지갑 잔액 확인 (price × size ≤ balance)
2. 주문 금액만큼 balance → locked 이동
3. 주문 상태 WAITING으로 데이터베이스 저장
4. 5초마다 체결 조건 확인 (매도호가 ≤ 주문가격)
5. 체결 시 locked 차감, 보유량 증가, 평균단가 재계산
6. WebSocket으로 체결 알림 전송

#### 매도 주문
1. 보유 수량 확인 (size ≤ holding.size)
2. 매도 수량만큼 holding.size → holding.locked 이동
3. 주문 상태 WAITING으로 저장
4. 5초마다 체결 조건 확인 (매수호가 ≥ 주문가격)
5. 체결 시 holding.locked 차감, 지갑 잔액 증가
6. WebSocket으로 체결 알림 전송

### 주문 상태
- **WAITING**: 체결 대기 중
- **SUCCESS**: 체결 완료
- **FAILED**: 체결 실패 (시스템 오류)
- **CANCELLED**: 사용자 취소

## 📡 WebSocket 연결 가이드

### 연결 방법
```javascript
const socket = new SockJS("http://server:8080/ws?token=" + jwtToken);
const stompClient = Stomp.over(socket);

stompClient.heartbeat.outgoing = 30000;
stompClient.heartbeat.incoming = 30000;

stompClient.connect({}, () => {
    console.log("WebSocket 연결 성공");
    subscribeToChannels();
}, error => {
    console.error("WebSocket 연결 실패:", error);
});
```

### 채널별 데이터 형태

#### 시세 데이터 (UpbitTickerDto)
```json
{
    "market": "KRW-BTC",
    "trade_price": 50000000.0,
    "change_rate": 0.0245,
    "acc_trade_volume_24h": 1234.56789,
    "timestamp": 1631234567890
}
```

#### 호가 데이터 (UpbitOrderbookDto)
```json
{
    "market": "KRW-BTC",
    "orderbook_units": [
        {
            "ask_price": 50001000.0,
            "bid_price": 49999000.0,
            "ask_size": 0.12345678,
            "bid_size": 0.98765432
        }
    ]
}
```

#### 주문 체결 알림 (TradeHistoryDto)
```json
{
    "id": 123,
    "member_id": 456,
    "market": "KRW-BTC",
    "side": "BUY",
    "price": 50000000.0,
    "size": 0.001,
    "status": "SUCCESS",
    "request_at": "2025-09-09T10:00:00",
    "matched_at": "2025-09-09T10:05:30"
}
```

## 🚀 배포

### GitHub Actions CI/CD
자동 배포는 `main` 브랜치 푸시 시 실행됩니다:

1. **빌드**: Gradle로 JAR 파일 생성
2. **도커 이미지**: GitHub Container Registry에 푸시
3. **배포**: AWS EC2에 자동 배포
4. **서비스 재시작**: Docker 컨테이너 무중단 재시작

### 배포 환경
- **서버**: AWS EC2 t3.medium (2 vCPU, 4GB RAM)
- **데이터베이스**: MySQL 8.0
- **캐시**: Redis
- **로드밸런서**: AWS ALB (추후 적용 예정)

## 📈 성능 및 모니터링

### 성능 지표
- **응답 시간**: 평균 50ms 이하 (Redis 캐시)
- **동시 접속자**: 1,000명 이상 지원
- **데이터 처리량**: 초당 10,000 메시지 (Kafka)
- **WebSocket 연결**: 최대 5,000 동시 연결

### 모니터링 도구
- **APM**: Spring Boot Actuator + Micrometer
- **로그**: Logback + ELK Stack (예정)

## 🛡️ 보안

- **인증**: JWT 토큰 기반 Stateless 인증
- **권한**: Spring Security Role-Based Access Control
- **API 보호**: Rate Limiting, CORS 설정
- **데이터 암호화**: 민감 정보 AES-256 암호화

### 개발 규칙
- **커밋 메시지**: Conventional Commits 형식
- **문서**: API 변경 시 Swagger 문서 업데이트

## 📄 라이선스

이 프로젝트는 MIT 라이선스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참조하세요.

## 📞 문의 및 지원

- **이슈 제보**: [GitHub Issues](https://github.com/seongjun-working-directories/market-follower/issues)
- **기능 요청**: [GitHub Discussions](https://github.com/seongjun-working-directories/market-follower/discussions)
- **이메일**: seongjuncode@gmail.com

---

<div align="center">

| [📚 API 문서](http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html) | [🗄️ 데이터베이스 스키마](schema.md) | [🐛 이슈 제보](https://github.com/seongjun-working-directories/market-follower/issues) |

**⭐ 이 프로젝트가 도움이 되었다면 Star를 눌러주세요!**

</div>