-- member 테이블 생성
CREATE TABLE IF NOT EXISTS member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(255),
    birthday DATE,
    created_at TIMESTAMP,
    last_login_at TIMESTAMP
);

-- auth 테이블 생성
CREATE TABLE IF NOT EXISTS auth (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    role VARCHAR(50) NOT NULL,
    CONSTRAINT fk_auth_member FOREIGN KEY (member_id) REFERENCES member(id)
);

-- wallet 테이블 생성
CREATE TABLE IF NOT EXISTS wallet (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL UNIQUE,
    balance DECIMAL(30,8) NOT NULL DEFAULT 0,
    locked DECIMAL(30,8) NOT NULL DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_wallet_member FOREIGN KEY (member_id) REFERENCES member(id)
);

-- trade_history 테이블 생성
CREATE TABLE IF NOT EXISTS trade_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    market VARCHAR(20) NOT NULL,
    side ENUM('BUY', 'SELL') NOT NULL,
    price DECIMAL(30,8) NOT NULL,
    size DECIMAL(30,8) NOT NULL,
    status ENUM('WAITING', 'SUCCESS', 'FAILED') NOT NULL DEFAULT 'WAITING',
    request_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    matched_at TIMESTAMP NULL,
    CONSTRAINT fk_trade_member FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- holding 테이블
CREATE TABLE IF NOT EXISTS holding (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    member_id BIGINT NOT NULL,
    market VARCHAR(20) NOT NULL,
    size DECIMAL(30,8) NOT NULL DEFAULT 0,       -- 사용 가능 수량
    locked DECIMAL(30,8) NOT NULL DEFAULT 0,     -- 주문 걸린 수량
    avg_price DECIMAL(30,8) NOT NULL DEFAULT 0,  -- 평균 단가
    CONSTRAINT uq_member_market UNIQUE (member_id, market),
    CONSTRAINT fk_holding_member FOREIGN KEY (member_id) REFERENCES member(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 거래 가능 코인 테이블 생성
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

-- Upbit Ticker 테이블 생성
CREATE TABLE IF NOT EXISTS upbit_ticker (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    market VARCHAR(255),

    trade_date VARCHAR(255),
    trade_time VARCHAR(255),
    trade_date_kst VARCHAR(255),
    trade_time_kst VARCHAR(255),
    trade_timestamp BIGINT,

    opening_price DOUBLE,
    high_price DOUBLE,
    low_price DOUBLE,
    trade_price DOUBLE,
    prev_closing_price DOUBLE,

    change_direction VARCHAR(255),
    change_price DOUBLE,
    change_rate DOUBLE,
    signed_change_price DOUBLE,
    signed_change_rate DOUBLE,

    trade_volume DOUBLE,
    acc_trade_price DOUBLE,
    acc_trade_price_24h DOUBLE,
    acc_trade_volume DOUBLE,
    acc_trade_volume_24h DOUBLE,

    highest_52_week_price DOUBLE,
    highest_52_week_date VARCHAR(255),
    lowest_52_week_price DOUBLE,
    lowest_52_week_date VARCHAR(255),

    upbit_timestamp BIGINT
);

-- 7일간 1시간 단위 캔들 (168개 데이터)
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
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='7일간 1시간 단위 캔들 데이터 (168개 × 600코인 = 약 100,800개 레코드)';

-- 30일간 4시간 단위 캔들 (180개 데이터)
CREATE TABLE IF NOT EXISTS upbit_candle_30d (
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
    unit INT NOT NULL DEFAULT 240 COMMENT '분 단위 (240분)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uk_market_datetime (market, candle_date_time_utc)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='30일간 4시간 단위 캔들 데이터 (180개 × 600코인 = 약 108,000개 레코드)';

-- 3개월간 일 단위 캔들 (90개 데이터)
CREATE TABLE IF NOT EXISTS upbit_candle_3m (
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
    prev_closing_price DECIMAL(20,8) NULL COMMENT '전일 종가',
    change_price DECIMAL(20,8) NULL COMMENT '전일 대비 변화 금액',
    change_rate DECIMAL(10,8) NULL COMMENT '전일 대비 변화율',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uk_market_datetime (market, candle_date_time_utc)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='3개월간 일 단위 캔들 데이터 (90개 × 600코인 = 약 54,000개 레코드)';

-- 1년간 일 단위 캔들 (최신 200개 + to 파라미터로 나머지 165개)
CREATE TABLE IF NOT EXISTS upbit_candle_1y (
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
    prev_closing_price DECIMAL(20,8) NULL COMMENT '전일 종가',
    change_price DECIMAL(20,8) NULL COMMENT '전일 대비 변화 금액',
    change_rate DECIMAL(10,8) NULL COMMENT '전일 대비 변화율',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '생성 시각',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '수정 시각',

    PRIMARY KEY (id),
    UNIQUE KEY uk_market_datetime (market, candle_date_time_utc)
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='1년간 일 단위 캔들 데이터 (365개 × 600코인 = 약 219,000개 레코드)';

-- 5년간 주 단위 캔들 (최신 200주 + to 파라미터로 나머지 165주)
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
) ENGINE=InnoDB
  DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci
  COMMENT='5년간 주 단위 캔들 데이터 (365주 × 600코인 = 약 219,000개 레코드)';
