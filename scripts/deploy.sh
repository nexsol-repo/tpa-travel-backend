#!/bin/bash
set -euo pipefail

# 사용법: ./deploy.sh [dev|prod]
TARGET_ENV=${1:-""}
APP_NAME="tpa-travel-api"
ROUTE_PATH="/api/travel/"

# [중요] Dev/Prod 경로 및 포트 분리
if [ "$TARGET_ENV" == "prod" ]; then
  BASE_PATH="/home/nex3/app/${APP_NAME}"
  ENV_FILE=".env.prod"
  NGINX_CONF="/etc/nginx/conf.d/tpa-travel-api.conf"
  DEFAULT_PORT="8061"
  ALT_PORT="8062"
elif [ "$TARGET_ENV" == "dev" ]; then
  BASE_PATH="/home/nex3/app/${APP_NAME}-dev"
  ENV_FILE=".env.dev"
  NGINX_CONF="/etc/nginx/conf.d/tpa-travel-api-dev.conf"
  DEFAULT_PORT="8071"
  ALT_PORT="8072"
else
  echo "❌ 잘못된 환경 인자입니다. (dev 또는 prod 사용)"
  exit 1
fi

# 배포 실패 시 신규 컨테이너 자동 롤백
cleanup_on_failure() {
  echo "🛑 배포 실패! 신규 컨테이너를 정리합니다..."
  docker compose -p "${NEW_PROJECT_NAME}" down 2>/dev/null || true
}

echo "============================================"
echo "🚀 ${APP_NAME} (${TARGET_ENV}) Blue/Green 배포 시작"
echo "📂 배포 경로: ${BASE_PATH}"
echo "🕐 시작 시간: $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"

# 1. 환경 파일 준비
if [ -f "${BASE_PATH}/${ENV_FILE}" ]; then
  cp "${BASE_PATH}/${ENV_FILE}" "${BASE_PATH}/.env"
  echo "✅ 환경 파일 준비 완료: ${ENV_FILE}"
else
  echo "❌ ${ENV_FILE} 파일이 없습니다. 서버 ${BASE_PATH} 경로에 파일을 생성해주세요."
  exit 1
fi

# 2. Blue-Green 포트 결정
CURRENT_PORT_FILE="${BASE_PATH}/current_port.txt"
if [ -f "$CURRENT_PORT_FILE" ]; then
    CURRENT_PORT=$(cat "$CURRENT_PORT_FILE")
else
    CURRENT_PORT="$DEFAULT_PORT"
fi

if [ "$CURRENT_PORT" == "$DEFAULT_PORT" ]; then
    TARGET_PORT="$ALT_PORT"
else
    TARGET_PORT="$DEFAULT_PORT"
fi

OLD_PROJECT_NAME="${APP_NAME}-${TARGET_ENV}-${CURRENT_PORT}"
NEW_PROJECT_NAME="${APP_NAME}-${TARGET_ENV}-${TARGET_PORT}"

echo "🔄 포트 스위칭: ${CURRENT_PORT}(Blue) → ${TARGET_PORT}(Green)"

# 3. 신규 컨테이너 실행
export HOST_PORT=$TARGET_PORT
export TARGET_ENV="${TARGET_ENV}"
export DOCKER_IMAGE="${APP_NAME}:${TARGET_ENV}"
export COMPOSE_PROJECT_NAME="${NEW_PROJECT_NAME}"

echo "📦 신규 컨테이너 기동: ${NEW_PROJECT_NAME} (Port: ${TARGET_PORT})"
cd "${BASE_PATH}"

if ! docker compose -f docker-compose.yml -p "${NEW_PROJECT_NAME}" up -d; then
  echo "❌ 컨테이너 기동 실패!"
  cleanup_on_failure
  exit 1
fi

# 4. Health Check (최대 120초 대기)
echo "🏥 헬스체크 시작... (http://127.0.0.1:${TARGET_PORT}/health)"
HEALTH_OK=false
for i in {1..24}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" "http://127.0.0.1:${TARGET_PORT}/health" 2>/dev/null) || STATUS="000"
  if [ "$STATUS" == "200" ]; then
    echo "✅ 헬스체크 성공! (${i}번째 시도)"
    HEALTH_OK=true
    break
  fi
  echo "⏳ 대기 중... ($i/24) - HTTP: $STATUS"
  sleep 5
done

if [ "$HEALTH_OK" != "true" ]; then
  echo "❌ 헬스체크 실패! 신규 컨테이너 로그:"
  echo "--------------------------------------------"
  docker compose -p "${NEW_PROJECT_NAME}" logs --tail 100
  echo "--------------------------------------------"
  cleanup_on_failure
  exit 1
fi

# 5. Nginx 트래픽 전환
echo "🔄 Nginx 트래픽 전환 중... (Config: ${NGINX_CONF})"

if [ ! -f "$NGINX_CONF" ]; then
    echo "⚠️  Nginx 설정 파일(${NGINX_CONF})이 없어 트래픽 전환을 건너뜁니다."
else
    # Nginx 설정 백업
    sudo cp "$NGINX_CONF" "${NGINX_CONF}.bak"

    # ROUTE_PATH에 해당하는 블록의 포트를 변경
    sudo sed -i "/location ${ROUTE_PATH//\//\\/}/,/}/ s/127.0.0.1:[0-9]\{4\}/127.0.0.1:${TARGET_PORT}/" "$NGINX_CONF"

    # Nginx 설정 검증 및 리로드
    if sudo nginx -t 2>/dev/null; then
        sudo nginx -s reload
        echo "✅ Nginx 트래픽 전환 완료 → 포트 ${TARGET_PORT}"
    else
        echo "❌ Nginx 설정 오류! 백업에서 복원합니다."
        sudo cp "${NGINX_CONF}.bak" "$NGINX_CONF"
        sudo nginx -s reload 2>/dev/null || true
        cleanup_on_failure
        exit 1
    fi
fi

# 6. 구 버전 컨테이너 제거
echo "🛑 이전 버전 제거: ${OLD_PROJECT_NAME}"
docker compose -p "${OLD_PROJECT_NAME}" down 2>/dev/null || true

# 7. 미사용 리소스 정리
echo "🧹 미사용 이미지 정리"
docker image prune -f

# 8. 현재 포트 정보 업데이트
echo "$TARGET_PORT" > "$CURRENT_PORT_FILE"

echo "============================================"
echo "🎉 ${TARGET_ENV} 배포 성공!"
echo "📍 서비스 포트: ${TARGET_PORT}"
echo "🕐 완료 시간: $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"