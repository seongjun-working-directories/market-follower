# 1단계: 빌드 스테이지 (Gradle 7.5 + JDK 17 이미지 사용)
FROM gradle:7.5-jdk17 AS build

WORKDIR /app

# gradlew와 gradle/wrapper 포함한 모든 빌드 관련 파일 복사
COPY gradlew .
COPY gradle ./gradle

# 빌드 스크립트 복사
COPY build.gradle settings.gradle ./

# 소스 코드 전체 복사
COPY src ./src

# gradlew에 실행 권한 부여
RUN chmod +x ./gradlew

# Gradle Wrapper로 빌드 실행 (테스트 제외)
RUN ./gradlew clean build -x test

# 2단계: 실행 스테이지 (OpenJDK 17 경량 이미지 사용)
FROM openjdk:17-jdk-slim

COPY --from=build /app/build/libs/*.jar app.jar

ENTRYPOINT ["java", "-jar", "/app.jar"]
