#!/bin/bash

# 사용법: ./deploy.sh [dev|prod]
TARGET_ENV=$1
APP_NAME="tpa-travel-api"  # 앱 이름 변경 (Admin 제거)
ROUTE_PATH="/api/travel/"  # 사용자 API 경로 (필요시 수정)

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

echo "🚀 ${APP_NAME} (${TARGET_ENV}) 배포 시작..."
echo "📂 배포 경로: ${BASE_PATH}"

# 1. 환경 파일 준비
if [ -f "${BASE_PATH}/${ENV_FILE}" ]; then
  cp "${BASE_PATH}/${ENV_FILE}" "${BASE_PATH}/.env"
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
echo "🔄 포트 스위칭 계획: ${CURRENT_PORT} -> ${TARGET_PORT}"

# 3. 신규 컨테이너 실행
export HOST_PORT=$TARGET_PORT
export TARGET_ENV="${TARGET_ENV}"
export DOCKER_IMAGE="${APP_NAME}:${TARGET_ENV}"
export COMPOSE_PROJECT_NAME="${APP_NAME}-${TARGET_ENV}-${TARGET_PORT}"

echo "📦 컨테이너 기동: ${COMPOSE_PROJECT_NAME} (Port: ${TARGET_PORT})"
cd "${BASE_PATH}"
docker compose -f docker-compose.yml -p $COMPOSE_PROJECT_NAME up -d

# 4. Health Check (최대 100초 대기)
echo "🏥 서비스 헬스체크 중... (http://127.0.0.1:${TARGET_PORT}/health)"
for i in {1..20}; do
  STATUS=$(curl -s -o /dev/null -w "%{http_code}" http://127.0.0.1:${TARGET_PORT}/health)
  if [ "$STATUS" == "200" ]; then
    echo "✅ 헬스체크 성공!"
    break
  fi
  echo "⏳ 대기 중... ($i/20) - HTTP 응답코드: $STATUS"
  sleep 5

  if [ $i -eq 20 ]; then
    echo "❌ 배포 실패! 앱 로그(마지막 100줄)를 출력합니다:"
    docker logs $COMPOSE_PROJECT_NAME --tail 100
    echo "🛑 신규 컨테이너를 중지하고 제거합니다."
    docker stop $COMPOSE_PROJECT_NAME && docker rm $COMPOSE_PROJECT_NAME
    exit 1
  fi
done

# 5. Nginx 트래픽 전환 (Surgical Update)
echo "🔄 Nginx 트래픽 전환 중... (Config: ${NGINX_CONF})"

if [ ! -f "$NGINX_CONF" ]; then
    echo "⚠️  Nginx 설정 파일($NGINX_CONF)이 없어 트래픽 전환을 건너뜁니다."
else
    # ROUTE_PATH에 해당하는 블록의 포트를 변경
    sudo sed -i "/location ${ROUTE_PATH//\//\\/}/,/}/ s/127.0.0.1:[0-9]\{4\}/127.0.0.1:${TARGET_PORT}/" $NGINX_CONF
    sudo nginx -t && sudo nginx -s reload
fi

# 6. 구 버전 컨테이너 제거
OLD_PROJECT_NAME="${APP_NAME}-${TARGET_ENV}-${CURRENT_PORT}"
echo "🛑 이전 버전 제거: ${OLD_PROJECT_NAME}"
docker compose -p $OLD_PROJECT_NAME down || true

# 7. 미사용 리소스 정리
echo "🧹 미사용 이미지 정리"
docker image prune -f

# 8. 현재 포트 정보 업데이트
echo "$TARGET_PORT" > "$CURRENT_PORT_FILE"
echo "🎉 ${TARGET_ENV} 배포 성공! 현재 서비스 포트: ${TARGET_PORT}"