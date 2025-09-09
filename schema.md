# 데이터베이스 스키마 문서

> Market Follower 서비스의 MySQL 데이터베이스 구조

## 📊 전체 ERD

```
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   member    │ 1──N │    auth     │      │   wallet    │
│             │      │             │      │             │
│ - id (PK)   │      │ - id (PK)   │      │ - id (PK)   │
│ - name      │      │ - member_id │ N──1 │ - member_id │
│ - email     │      │ - role      │      │ - balance   │
│ - phone     │      │             │      │ - locked    │
│ - birthday  │      └─────────────┘      │             │
│ - created_at│                           └─────────────┘
│ - last_login│
└─────────────┘
       │
       │ 1
       │
       │ N
┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│   holding   │      │trade_history│      │tradable_coin│
│             │      │             │      │             │
│ - id (PK)   │      │ - id (PK)   │      │ - market(PK)│
│ - member_id │ N──1 │ - member_id │      │ - names     │
│ - market    │      │ - market    │      │ - warnings  │
│ - size      │      │ - side      │      │             │
│ - locked    │      │ - price     │      │             │
│ - avg_price │      │ - size      │      │             │
│             │      │ - status    │      │             │
│             │      │ - request_at│      │             │
│             │      │ - matched_at│      │             │
└─────────────┘      └─────────────┘      └─────────────┘

┌─────────────┐      ┌─────────────┐      ┌─────────────┐
│upbit_ticker │      │upbit_candle │      │upbit_candle │
│             │      │    _7d      │      │    _30d     │
│ - id (PK)   │      │             │      │             │
│ - market    │      │ - id (PK)   │      │ - id (PK)   │
│ - prices    │      │ - market    │      │ - market    │
│ - volumes   │      │ - datetime  │      │ - datetime  │
│ - changes   │      │ - ohlc      │      │ - ohlc      │
│ - 52w data  │      │ - volumes   │      │ - volumes   │
└─────────────┘      │ - unit:60m  │      │ - unit:4h   │
                     └─────────────┘      └─────────────┘

┌─────────────┐      ┌─────────────┐
│upbit_candle │      │upbit_candle │
│    _3m      │      │    _1y      │
│             │      │             │
│ - id (PK)   │      │ - id (PK)   │
│ - market    │      │ - market    │
│ - datetime  │      │ - datetime  │
│ - ohlc      │      │ - ohlc      │
│ - volumes   │      │ - volumes   │
│ - changes   │      │ - changes   │
│ - unit:1d   │      │ - unit:1d   │
└─────────────┘      └─────────────┘

┌─────────────┐
│upbit_candle │
│    _5y      │
│             │
│ - id (PK)   │
│ - market    │
│ - datetime  │
│ - ohlc      │
│ - volumes   │
│ - unit:1w   │
└─────────────┘
```

## 🗄️ 테이블 구조

### 👥 사용자 관리 테이블

#### `member` - 사용자 기본 정보
사용자의 기본 정보와 가입/로그인 기록을 저장합니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 사용자 고유 ID |
| `name` | VARCHAR(255) | | 사용자 이름 |
| `email` | VARCHAR(255) | UNIQUE | 이메일 (로그인용) |
| `phone_number` | VARCHAR(255) | | 휴대폰 번호 |
| `birthday` | DATE | | 생년월일 |
| `created_at` | TIMESTAMP | | 가입 일시 |
| `last_login_at` | TIMESTAMP | | 최종 로그인 일시 |

```sql
CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(255),
    birthday DATE,
    created_at TIMESTAMP,
    last_login_at TIMESTAMP
);
```

#### `auth` - 사용자 권한
역할 기반 접근 제어를 위한 사용자 권한 정보를 저장합니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 권한 고유 ID |
| `member_id` | BIGINT | NOT NULL, FOREIGN KEY | 사용자 ID |
| `role` | VARCHAR(50) | NOT NULL | 사용자 역할 (USER, ADMIN) |

```sql
CREATE TABLE IF NOT EXISTS auth (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_auth_member FOREIGN KEY (member_id) REFERENCES member(id)
);
```

### 💰 지갑 및 거래 테이블

#### `wallet` - 사용자 지갑
사용자의 KRW 잔액과 주문에 사용된 잠금 자금을 관리합니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 지갑 고유 ID |
| `member_id` | BIGINT | NOT NULL, UNIQUE, FOREIGN KEY | 사용자 ID (1:1 관계) |
| `balance` | DECIMAL(30,8) | NOT NULL, DEFAULT 0 | 사용 가능한 KRW 잔액 |
| `locked` | DECIMAL(30,8) | NOT NULL, DEFAULT 0 | 주문 대기 중인 잠금 자금 |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 일시 |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정 일시 |

```sql
CREATE TABLE IF NOT EXISTS wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(30,8) NOT NULL DEFAULT 0,
    locked DECIMAL(30,8) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_member FOREIGN KEY (member_id) REFERENCES member(id)
);
```

**주요 비즈니스 로직:**
- `balance + locked = 총 보유 KRW`
- 매수 주문 시: `balance` → `locked` 이동
- 주문 체결 시: `locked` 차감, 암호화폐 보유량 증가
- 주문 취소 시: `locked` → `balance` 복구

#### `holding` - 암호화폐 보유량
사용자의 암호화폐 보유 현황과 평균 매수가를 관리합니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 보유량 고유 ID |
| `member_id` | BIGINT | NOT NULL, FOREIGN KEY | 사용자 ID |
| `market` | VARCHAR(20) | NOT NULL | 마켓 코드 (예: KRW-BTC) |
| `size` | DECIMAL(30,8) | NOT NULL, DEFAULT 0 | 매도 가능한 수량 |
| `locked` | DECIMAL(30,8) | NOT NULL, DEFAULT 0 | 매도 주문 중인 잠금 수량 |
| `avg_price` | DECIMAL(30,8) | NOT NULL, DEFAULT 0 | 평균 매수가 |

```sql
CREATE TABLE IF NOT EXISTS holding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    market VARCHAR(20) NOT NULL,
    size DECIMAL(30,8) NOT NULL DEFAULT 0,
    locked DECIMAL(30,8) NOT NULL DEFAULT 0,
    avg_price DECIMAL(30,8) NOT NULL DEFAULT 0,
    CONSTRAINT uq_member_market UNIQUE (member_id, market),
    CONSTRAINT fk_holding_member FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**주요 비즈니스 로직:**
- `size + locked = 총 보유 수량`
- 매수 체결 시: `size` 증가, `avg_price` 재계산
- 매도 주문 시: `size` → `locked` 이동
- 매도 체결 시: `locked` 차감

**평균 매수가 계산 공식:**
```
새로운 평균가 = (기존 총 매수금액 + 신규 매수금액) / (기존 수량 + 신규 수량)
```

#### `trade_history` - 거래 내역
모든 매수/매도 주문의 실행 내역을 기록합니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 거래 내역 고유 ID |
| `member_id` | BIGINT | NOT NULL, FOREIGN KEY | 사용자 ID |
| `market` | VARCHAR(20) | NOT NULL | 마켓 코드 (예: KRW-BTC) |
| `side` | ENUM('BUY', 'SELL') | NOT NULL | 거래 종류 |
| `price` | DECIMAL(30,8) | NOT NULL | 거래 단가 |
| `size` | DECIMAL(30,8) | NOT NULL | 거래 수량 |
| `status` | ENUM('WAITING', 'SUCCESS', 'FAILED', 'CANCELLED') | NOT NULL, DEFAULT 'WAITING' | 거래 상태 |
| `request_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 주문 요청 시각 |
| `matched_at` | TIMESTAMP | NULL | 체결 완료 시각 |

```sql
CREATE TABLE IF NOT EXISTS trade_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    market VARCHAR(20) NOT NULL,
    side ENUM('BUY', 'SELL') NOT NULL,
    price DECIMAL(30,8) NOT NULL,
    size DECIMAL(30,8) NOT NULL,
    status ENUM('WAITING', 'SUCCESS', 'FAILED', 'CANCELLED') NOT NULL DEFAULT 'WAITING',
    request_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    matched_at TIMESTAMP NULL,
    CONSTRAINT fk_trade_member FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

**거래 상태 설명:**
- `WAITING`: 주문 대기 중
- `SUCCESS`: 체결 완료
- `FAILED`: 주문 실패
- `CANCELLED`: 주문 취소

### 🪙 코인 정보 테이블

#### `tradable_coin` - 거래 가능 코인
업비트에서 거래 가능한 코인의 기본 정보와 주의사항을 저장합니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `market` | VARCHAR(20) | PRIMARY KEY | 마켓 코드 (예: KRW-BTC) |
| `korean_name` | VARCHAR(50) | | 한국어 이름 |
| `english_name` | VARCHAR(50) | | 영어 이름 |
| `is_warning` | BOOLEAN | | 투자유의 지정 여부 |
| `is_caution_price_fluctuations` | BOOLEAN | | 가격급등주의 여부 |
| `is_caution_trading_volume_soaring` | BOOLEAN | | 거래량급증주의 여부 |
| `is_caution_deposit_amount_soaring` | BOOLEAN | | 입금량급증주의 여부 |
| `is_caution_global_price_differences` | BOOLEAN | | 글로벌가격차이주의 여부 |
| `is_caution_concentration_of_small_accounts` | BOOLEAN | | 소액계좌집중주의 여부 |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 수정 일시 |

```sql
CREATE TABLE IF NOT EXISTS tradable_coin (
    market VARCHAR(20) PRIMARY KEY,
    korean_name VARCHAR(50),
    english_name VARCHAR(50),
    is_warning BOOLEAN,
    is_caution_price_fluctuations BOOLEAN,
    is_caution_trading_volume_soaring BOOLEAN,
    is_caution_deposit_amount_soaring BOOLEAN,
    is_caution_global_price_differences BOOLEAN,
    is_caution_concentration_of_small_accounts BOOLEAN,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

### 📊 업비트 시장 데이터 테이블

#### `upbit_ticker` - 실시간 시세 정보
업비트의 실시간 코인 시세 정보를 저장합니다.

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 고유 ID |
| `market` | VARCHAR(255) | | 마켓 코드 |
| `trade_date` | VARCHAR(255) | | 최근 거래 일자 (UTC) |
| `trade_time` | VARCHAR(255) | | 최근 거래 시각 (UTC) |
| `trade_date_kst` | VARCHAR(255) | | 최근 거래 일자 (KST) |
| `trade_time_kst` | VARCHAR(255) | | 최근 거래 시각 (KST) |
| `trade_timestamp` | BIGINT | | 최근 거래 일시 (timestamp) |
| `opening_price` | DOUBLE | | 시가 |
| `high_price` | DOUBLE | | 고가 |
| `low_price` | DOUBLE | | 저가 |
| `trade_price` | DOUBLE | | 종가 (현재가) |
| `prev_closing_price` | DOUBLE | | 전일 종가 |
| `change_direction` | VARCHAR(255) | | 변화 방향 (RISE/FALL/EVEN) |
| `change_price` | DOUBLE | | 변화 금액 |
| `change_rate` | DOUBLE | | 변화율 |
| `signed_change_price` | DOUBLE | | 부호 있는 변화 금액 |
| `signed_change_rate` | DOUBLE | | 부호 있는 변화율 |
| `trade_volume` | DOUBLE | | 가장 최근 거래량 |
| `acc_trade_price` | DOUBLE | | 누적 거래 대금 (UTC 0시 기준) |
| `acc_trade_price_24h` | DOUBLE | | 24시간 누적 거래 대금 |
| `acc_trade_volume` | DOUBLE | | 누적 거래량 (UTC 0시 기준) |
| `acc_trade_volume_24h` | DOUBLE | | 24시간 누적 거래량 |
| `highest_52_week_price` | DOUBLE | | 52주 신고가 |
| `highest_52_week_date` | VARCHAR(255) | | 52주 신고가 달성일 |
| `lowest_52_week_price` | DOUBLE | | 52주 신저가 |
| `lowest_52_week_date` | VARCHAR(255) | | 52주 신저가 달성일 |
| `upbit_timestamp` | BIGINT | | 타임스탬프 |

#### `upbit_candle_7d` - 7일간 1시간 단위 캔들
최근 7일간의 1시간 단위 캔들 데이터를 저장합니다. (168개 × 600코인 = 약 100,800개 레코드)

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | 고유 ID |
| `market` | VARCHAR(20) | NOT NULL | 마켓명 (KRW-BTC) |
| `candle_date_time_utc` | DATETIME | NOT NULL | 캔들 기준 시각 (UTC) |
| `candle_date_time_kst` | DATETIME | NOT NULL | 캔들 기준 시각 (KST) |
| `opening_price` | DECIMAL(20,8) | NOT NULL | 시가 |
| `high_price` | DECIMAL(20,8) | NOT NULL | 고가 |
| `low_price` | DECIMAL(20,8) | NOT NULL | 저가 |
| `trade_price` | DECIMAL(20,8) | NOT NULL | 종가 |
| `timestamp` | BIGINT | NOT NULL | 마지막 틱 저장 시각 |
| `candle_acc_trade_price` | DECIMAL(30,8) | NOT NULL | 누적 거래 금액 |
| `candle_acc_trade_volume` | DECIMAL(30,8) | NOT NULL | 누적 거래량 |
| `unit` | INT | NOT NULL, DEFAULT 60 | 분 단위 (60분) |
| `created_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP | 생성 시각 |
| `updated_at` | TIMESTAMP | DEFAULT CURRENT_TIMESTAMP ON UPDATE | 수정 시각 |

```sql
CREATE TABLE IF NOT EXISTS upbit_candle_7d (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market VARCHAR(20) NOT NULL COMMENT '마켓명 (KRW-BTC)',
    candle_date_time_utc DATETIME NOT NULL COMMENT '캔들 기준 시각 (UTC)',
    candle_date_time_kst DATETIME NOT NULL COMMENT '캔들 기준 시각 (KST)',
    opening_price DECIMAL(20,8) NOT NULL COMMENT '시가',
    high_price DECIMAL(20,8) NOT NULL COMMENT '고가',
    low_price DECIMAL(20,8) NOT NULL COMMENT '저가',
    trade_price DECIMAL(20,8) NOT NULL COMMENT '종가',
    timestamp BIGINT NOT NULL COMMENT '마지막 틱 저장 시각',
    candle_acc_trade_price DECIMAL(30,8) NOT NULL COMMENT '누적 거래 금액',
    candle_acc_trade_volume DECIMAL(30,8) NOT NULL COMMENT '누적 거래량',
    unit INT NOT NULL DEFAULT 60 COMMENT '분 단위 (60분)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uk_market_datetime (market, candle_date_time_utc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

#### `upbit_candle_30d` - 30일간 4시간 단위 캔들
최근 30일간의 4시간 단위 캔들 데이터를 저장합니다. (180개 × 600코인 = 약 108,000개 레코드)

구조는 `upbit_candle_7d`와 동일하며, `unit` 필드가 240(분)으로 설정됩니다.

#### `upbit_candle_3m` - 3개월간 일 단위 캔들
최근 3개월간의 일 단위 캔들 데이터를 저장합니다. (90개 × 600코인 = 약 54,000개 레코드)

기본 캔들 데이터에 추가로 다음 컬럼들을 포함합니다:

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `prev_closing_price` | DECIMAL(20,8) | NULL | 전일 종가 |
| `change_price` | DECIMAL(20,8) | NULL | 전일 대비 변화 금액 |
| `change_rate` | DECIMAL(10,8) | NULL | 전일 대비 변화율 |

#### `upbit_candle_1y` - 1년간 일 단위 캔들
최근 1년간의 일 단위 캔들 데이터를 저장합니다. (365개 × 600코인 = 약 219,000개 레코드)

구조는 `upbit_candle_3m`과 동일합니다.

#### `upbit_candle_5y` - 5년간 주 단위 캔들
최근 5년간의 주 단위 캔들 데이터를 저장합니다. (260주 × 600코인 = 약 156,000개 레코드)

기본 캔들 데이터에 추가로 다음 컬럼을 포함합니다:

| 컬럼명 | 타입 | 제약조건 | 설명 |
|--------|------|----------|------|
| `first_day_of_period` | DATE | NULL | 캔들 기간의 가장 첫 날 |

```sql
CREATE TABLE IF NOT EXISTS upbit_candle_5y (
    id BIGINT NOT NULL AUTO_INCREMENT,
    market VARCHAR(20) NOT NULL COMMENT '마켓명 (KRW-BTC)',
    candle_date_time_utc DATETIME NOT NULL COMMENT '캔들 기준 시각 (UTC)',
    candle_date_time_kst DATETIME NOT NULL COMMENT '캔들 기준 시각 (KST)',
    opening_price DECIMAL(20,8) NOT NULL COMMENT '시가',
    high_price DECIMAL(20,8) NOT NULL COMMENT '고가',
    low_price DECIMAL(20,8) NOT NULL COMMENT '저가',
    trade_price DECIMAL(20,8) NOT NULL COMMENT '종가',
    timestamp BIGINT NOT NULL COMMENT '마지막 틱 저장 시각',
    candle_acc_trade_price DECIMAL(30,8) NOT NULL COMMENT '누적 거래 금액',
    candle_acc_trade_volume DECIMAL(30,8) NOT NULL COMMENT '누적 거래량',
    first_day_of_period DATE NULL COMMENT '캔들 기간의 가장 첫 날',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uk_market_datetime (market, candle_date_time_utc)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

## 📈 캔들 데이터 저장 전략

### 데이터 수집 주기
- **실시간 데이터**: `upbit_ticker` - 10초마다 업데이트
- **7일 캔들**: 1시간마다 업데이트
- **30일 캔들**: 4시간마다 업데이트
- **3개월/1년 캔들**: 1일마다 업데이트
- **5년 캔들**: 1주마다 업데이트

### 데이터 용량 관리
- 각 테이블별로 적절한 데이터 보존 주기 설정
- 오래된 데이터는 별도 아카이브 테이블로 이관
- 인덱스 최적화를 통한 조회 성능 향상

### 주요 인덱스
```sql
-- 캔들 데이터 조회 최적화
CREATE INDEX idx_market_datetime ON upbit_candle_7d (market, candle_date_time_utc);
CREATE INDEX idx_market_datetime ON upbit_candle_30d (market, candle_date_time_utc);
CREATE INDEX idx_market_datetime ON upbit_candle_3m (market, candle_date_time_utc);
CREATE INDEX idx_market_datetime ON upbit_candle_1y (market, candle_date_time_utc);
CREATE INDEX idx_market_datetime ON upbit_candle_5y (market, candle_date_time_utc);

-- 사용자 데이터 조회 최적화
CREATE INDEX idx_member_market ON holding (member_id, market);
CREATE INDEX idx_member_status ON trade_history (member_id, status);
```

## 🔐 보안 고려사항

### 민감 데이터 보호
- 사용자 개인정보는 암호화하여 저장
- 거래 내역은 무결성 검증을 위한 해시값 포함 검토
- 데이터베이스 접근 권한 최소화

### 데이터 백업
- 일일 전체 백업
- 실시간 트랜잭션 로그 백업
- 주요 테이블별 별도 백업 전략 수립

---

> **주의사항**: 이 스키마는 Market Follower 서비스의 초기 버전이며, 서비스 확장에 따라 추가 테이블 및 컬럼이 필요할 수 있습니다.
