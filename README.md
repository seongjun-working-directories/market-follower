# Market Follower

> 업비트 암호화폐 실시간 시세 및 차트 데이터 제공 서비스

## 🚀 주요 기능

### 📊 실시간 데이터
- **실시간 암호화폐 시세** - 업비트 600여개 코인 현재가 정보
- **WebSocket 스트리밍** - 10초마다 최신 시세 자동 업데이트
- **빠른 응답** - Redis 캐시로 밀리초 단위 응답

### 📈 차트 데이터
- **다양한 시간대** - 7일, 30일, 3개월, 1년, 5년 캔들 데이터
- **실시간 캔들** - 5분마다 최신 캔들 업데이트
- **데이터 최적화** - 기간별 최적화된 캔들 간격 (1시간~1주)

### 🔐 사용자 관리
- **구글 소셜 로그인** - 간편한 OAuth2 인증
- **JWT 토큰 인증** - 안전한 API 접근 관리

## 🛠 기술 스택

- **Backend**: Spring Boot 3.x, Java 17
- **Database**: MySQL 8.0
- **Cache**: Redis
- **Message Queue**: Apache Kafka
- **Real-time**: WebSocket (STOMP)
- **Infrastructure**: Docker, AWS EC2
- **CI/CD**: GitHub Actions

## 📋 API 문서

🔗 [Swagger UI 문서](http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html)

### 주요 엔드포인트

```
GET  /market/list               # 거래 가능한 코인 목록
GET  /market/ticker/{market}    # 특정 코인 현재가
GET  /market/ticker/all         # 전체 코인 현재가
GET  /candle/all                # 전체 캔들 데이터
GET  /candle/daily?market=      # 특정 코인 일별 캔들
POST /auth/google               # 구글 로그인
POST /auth/signup               # 회원가입
```

### WebSocket 연결

```javascript
// 전체 시세 실시간 구독
/topic/ticker/all

// 특정 코인 시세 구독
/topic/ticker/KRW-BTC
```

## 🏃‍♂️ 빠른 시작

### 1. 환경 변수 설정
```bash
# .env 파일 생성
API_ACCESS=your_upbit_api_key
API_SECRET=your_upbit_secret_key
```

### 2. Docker로 실행
```bash
git clone https://github.com/seongjun-working-directories/market-follower.git
cd market-follower
docker-compose up -d
```

### 3. 애플리케이션 접속
- 백엔드 서버: http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080
- API 문서: http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html

## 🏗 아키텍처

```
[Upbit API] → [Kafka Producer] → [Kafka] → [Consumer] → [Redis Cache]
                                                            ↓
[Client] ←← [WebSocket] ←← [Spring Boot API Server] ←← [MySQL DB]
```

## 📊 데이터 수집 스케줄

- **실시간 시세**: 10초마다 업비트 API 호출
- **캔들 데이터**: 매일 오전 9시 5분 전체 동기화
- **일별 캔들**: 5분마다 최신 캔들 추가
- **거래 코인 목록**: 매일 오전 8시 40분 업데이트

## 🔧 주요 설정

### 데이터베이스 테이블
- `member` - 사용자 정보
- `tradable_coin` - 거래 가능한 코인 목록
- `upbit_candle_*` - 기간별 캔들 데이터 (7d, 30d, 3m, 1y, 5y)

### Redis 키 구조
```
upbit:ticker:{MARKET}           # 현재가 데이터
upbit:candle:1d:{MARKET}        # 일별 캔들 데이터
```

## 🚀 배포

자동 배포는 GitHub Actions를 통해 main 브랜치 푸시 시 실행됩니다.

1. GitHub Container Registry에 이미지 빌드/푸시
2. AWS EC2에 자동 배포
3. Docker 컨테이너 재시작

## 📝 라이선스

MIT License

---

<div align="center">

| [프로젝트 API 문서](http://ec2-43-201-3-45.ap-northeast-2.compute.amazonaws.com:8080/swagger-ui/index.html) | [이슈 제보](https://github.com/seongjun-working-directories/market-follower/issues) |

</div>