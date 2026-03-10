# 1. Builder Stage
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app

# Gradle 설정 및 래퍼 복사
COPY gradlew .
COPY gradle ./gradle
COPY build.gradle .
COPY settings.gradle .
COPY gradle.properties .
COPY lint.gradle .

# 멀티 모듈 의존성 캐싱 (변경 빈도가 낮은 설정 파일들)
COPY core/core-api/build.gradle ./core/core-api/
COPY core/core-domain/build.gradle ./core/core-domain/
COPY core/core-enum/build.gradle ./core/core-enum/

COPY clients/client-meritz/build.gradle ./clients/client-meritz/
COPY storage/db-core/build.gradle ./storage/db-core/
COPY support/logging/build.gradle ./support/logging/
COPY support/monitoring/build.gradle ./support/monitoring/

COPY tests/api-docs/build.gradle ./tests/api-docs/

RUN chmod +x ./gradlew
RUN ./gradlew dependencies --no-daemon

# 전체 소스 복사 및 빌드
COPY . .

RUN chmod +x ./gradlew
# :core:core-api 모듈만 빌드 (테스트 제외)
RUN ./gradlew :core:core-api:clean :core:core-api:bootJar \
    -x unitTest -x contextTest -x developTest --no-daemon

# 2. Runtime Stage
FROM eclipse-temurin:25-jre
# HealthCheck용 curl 설치
RUN apt-get update && apt-get install -y --no-install-recommends curl \
    && rm -rf /var/lib/apt/lists/*

# 보안: non-root 사용자로 실행
RUN groupadd -r appuser && useradd -r -g appuser -d /app appuser

WORKDIR /app

# 빌드된 core-api jar 파일 복사
COPY --from=builder /app/core/core-api/build/libs/*.jar app.jar

RUN chown -R appuser:appuser /app

USER appuser

ENV TZ=Asia/Seoul
EXPOSE 8080

ENTRYPOINT ["java", "-jar", "-Djava.security.egd=file:/dev/./urandom", "app.jar"]