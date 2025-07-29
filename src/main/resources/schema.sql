-- auth 테이블 생성
CREATE TABLE auth (
    id BIGINT AUTO_INCREMENT PRIMARY KEY
);

-- user 테이블 생성
CREATE TABLE member (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255),
    email VARCHAR(255) UNIQUE,
    phone_number VARCHAR(255),
    birthday DATE,
    created_at DATETIME,
    last_login_at DATETIME
);
