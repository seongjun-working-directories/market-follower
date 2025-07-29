-- auth 테이블 초기 데이터
INSERT INTO auth (id) VALUES (1);
INSERT INTO auth (id) VALUES (2);

-- member 테이블 초기 데이터
INSERT INTO member (name, email, phone_number, birthday, created_at, last_login_at)
VALUES ('홍길동', 'hong@example.com', '01012345678', '1990-01-01', NOW(), NOW());

INSERT INTO member (name, email, phone_number, birthday, created_at, last_login_at)
VALUES ('김영희', 'kim@example.com', '01087654321', '1995-05-15', NOW(), NOW());
