#!/bin/bash
set -euo pipefail

# 사용법: ./deploy.sh [dev|prod]
TARGET_ENV=${1:-""}
APP_NAME="tpa-travel-api"

# 공통 경로 (dev/prod 모두 동일 디렉토리, .env.dev/.env.prod로 구분)
BASE_PATH="/home/nex3/app/${APP_NAME}"

if [ "$TARGET_ENV" == "prod" ]; then
  ENV_FILE=".env.prod"
  NGINX_CONF="/etc/nginx/conf.d/tpa-travel-api.conf"
  DEFAULT_PORT="8061"
  ALT_PORT="8062"
elif [ "$TARGET_ENV" == "dev" ]; then
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
  docker compose -f docker-compose.yml -p "${NEW_PROJECT}" down 2>/dev/null || true
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

# compose 프로젝트명 = 컨테이너 이름 (docker-compose.yml의 container_name에서 사용)
OLD_PROJECT="${APP_NAME}-${TARGET_ENV}-${CURRENT_PORT}"
NEW_PROJECT="${APP_NAME}-${TARGET_ENV}-${TARGET_PORT}"

echo "🔄 포트 스위칭: ${CURRENT_PORT}(Blue) → ${TARGET_PORT}(Green)"

# 3. 신규 컨테이너 실행
export HOST_PORT="${TARGET_PORT}"
export TARGET_ENV="${TARGET_ENV}"
export DOCKER_IMAGE="${APP_NAME}:${TARGET_ENV}"
export COMPOSE_PROJECT_NAME="${NEW_PROJECT}"

echo "📦 신규 컨테이너 기동: ${NEW_PROJECT} (Port: ${TARGET_PORT})"
cd "${BASE_PATH}"

# 같은 이름의 잔여 컨테이너가 있으면 제거 (이전 배포 실패 등으로 남아있을 수 있음)
docker rm -f "${NEW_PROJECT}" 2>/dev/null || true

if ! docker compose -f docker-compose.yml -p "${NEW_PROJECT}" up -d; then
  echo "❌ 컨테이너 기동 실패!"
  cleanup_on_failure
  exit 1
fi

# 4. Health Check (최대 120초 대기)
echo "🏥 헬스체크 시작... (http://127.0.0.1:${TARGET_PORT}/health)"
HEALTH_OK=false
for i in $(seq 1 24); do
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
  echo "❌ 헬스체크 실패! 컨테이너 로그:"
  echo "--------------------------------------------"
  docker compose -p "${NEW_PROJECT}" logs --tail 100
  echo "--------------------------------------------"
  cleanup_on_failure
  exit 1
fi

# 5. Nginx 트래픽 전환
echo "🔄 Nginx 트래픽 전환 중... (Config: ${NGINX_CONF})"

if [ ! -f "$NGINX_CONF" ]; then
    echo "⚠️  Nginx 설정 파일(${NGINX_CONF})이 없어 트래픽 전환을 건너뜁니다."
else
    sudo sed -i "s/127.0.0.1:[0-9]\{4\}/127.0.0.1:${TARGET_PORT}/g" "$NGINX_CONF"
    sudo nginx -t && sudo nginx -s reload
    echo "✅ Nginx 트래픽 전환 완료 → 포트 ${TARGET_PORT}"
fi

# 6. 이전 컨테이너 제거
echo "🛑 이전 컨테이너 제거"

# 6-1. compose down (프로젝트명 일치하면 정리됨)
docker compose -f docker-compose.yml -p "${OLD_PROJECT}" down 2>/dev/null || true

# 6-2. 위에서 못 잡은 잔여 컨테이너 강제 정리 (신규만 남기고 전부 제거)
OLD_CIDS=$(docker ps -aq --filter "name=${APP_NAME}-${TARGET_ENV}" 2>/dev/null || true)
if [ -n "$OLD_CIDS" ]; then
  for cid in $OLD_CIDS; do
    CNAME=$(docker inspect --format='{{.Name}}' "$cid" 2>/dev/null | sed 's/^\///')
    # 신규 컨테이너는 건드리지 않음
    if [ "$CNAME" = "${NEW_PROJECT}" ]; then
      continue
    fi
    echo "  🗑️ 제거: ${CNAME} ($cid)"
    docker rm -f "$cid" 2>/dev/null || true
  done
fi

# 7. dangling 이미지 정리
echo "🧹 미사용 이미지 정리"
docker image prune -f

# 8. 현재 포트 정보 업데이트
echo "$TARGET_PORT" > "$CURRENT_PORT_FILE"

echo "============================================"
echo "🎉 ${TARGET_ENV} 배포 성공!"
echo "📍 서비스 포트: ${TARGET_PORT}"
echo "📦 컨테이너: ${NEW_PROJECT}"
echo "🕐 완료 시간: $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"
