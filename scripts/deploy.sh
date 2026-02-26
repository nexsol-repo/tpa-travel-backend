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
  docker rm -f "${NEW_CONTAINER}" 2>/dev/null || true
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

NEW_CONTAINER="${APP_NAME}-${TARGET_ENV}-${TARGET_PORT}"

echo "🔄 포트 스위칭: ${CURRENT_PORT}(Blue) → ${TARGET_PORT}(Green)"

# 3. 신규 컨테이너 실행 (docker compose 대신 docker run 으로 단순화)
export DOCKER_IMAGE="${APP_NAME}:${TARGET_ENV}"

echo "📦 신규 컨테이너 기동: ${NEW_CONTAINER} (Port: ${TARGET_PORT})"
cd "${BASE_PATH}"

# 혹시 같은 이름 컨테이너가 남아있으면 제거
docker rm -f "${NEW_CONTAINER}" 2>/dev/null || true

docker run -d \
  --name "${NEW_CONTAINER}" \
  --network host \
  --restart always \
  --env-file .env \
  -e TZ=Asia/Seoul \
  -e SERVER_PORT=${TARGET_PORT} \
  -e SPRING_PROFILES_ACTIVE=${TARGET_ENV} \
  "${DOCKER_IMAGE}"

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
  docker logs --tail 100 "${NEW_CONTAINER}" 2>&1 || true
  echo "--------------------------------------------"
  cleanup_on_failure
  exit 1
fi

# 5. Nginx 트래픽 전환
echo "🔄 Nginx 트래픽 전환 중... (Config: ${NGINX_CONF})"

if [ ! -f "$NGINX_CONF" ]; then
    echo "⚠️  Nginx 설정 파일(${NGINX_CONF})이 없어 트래픽 전환을 건너뜁니다."
else
    sudo cp "$NGINX_CONF" "${NGINX_CONF}.bak"
    sudo sed -i "s/127.0.0.1:[0-9]\{4\}/127.0.0.1:${TARGET_PORT}/g" "$NGINX_CONF"

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

# 6. 이전 컨테이너 모두 제거 (신규 컨테이너만 남김)
echo "🛑 이전 컨테이너 제거"
for cid in $(docker ps -aq --filter "name=${APP_NAME}-${TARGET_ENV}" 2>/dev/null || true); do
  CNAME=$(docker inspect --format='{{.Name}}' "$cid" 2>/dev/null | sed 's/^\///')
  if [ "$CNAME" != "${NEW_CONTAINER}" ]; then
    echo "  🗑️ 제거: ${CNAME} ($cid)"
    docker rm -f "$cid" 2>/dev/null || true
  fi
done

# 7. 태그 없는(dangling) 이미지 정리
echo "🧹 미사용 이미지 정리"
docker image prune -f

# 8. 현재 포트 정보 업데이트
echo "$TARGET_PORT" > "$CURRENT_PORT_FILE"

echo "============================================"
echo "🎉 ${TARGET_ENV} 배포 성공!"
echo "📍 서비스 포트: ${TARGET_PORT}"
echo "📦 컨테이너: ${NEW_CONTAINER}"
echo "🕐 완료 시간: $(date '+%Y-%m-%d %H:%M:%S')"
echo "============================================"
