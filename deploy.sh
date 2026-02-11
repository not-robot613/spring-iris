#!/bin/bash
set -e

# -------------------------------------
# 1️⃣ 최신 코드 pull
# -------------------------------------
echo "🔄 최신 코드를 pull하고 있습니다..."
git pull origin feat/1

# -------------------------------------
# 2️⃣ 실행 중인 애플리케이션 종료
# -------------------------------------
echo "🔎 실행 중인 애플리케이션 프로세스를 찾고 있습니다..."
PID=$(ps aux | grep java | grep 'spring.profiles.active=prod' | grep -v grep | awk '{print $2}')

if [ -z "$PID" ]; then
    echo "✅ 현재 실행 중인 애플리케이션이 없습니다. 새 애플리케이션을 시작합니다."
else
    echo "💀 PID $PID인 기존 애플리케이션을 종료합니다."
    kill "$PID"
    sleep 5
fi

# -------------------------------------
# 3️⃣ Gradle로 bootJar 빌드 (테스트 제외)
# -------------------------------------
echo "🔨 테스트를 건너뛰고 Gradle로 bootJar를 빌드하고 있습니다..."
./gradlew clean bootJar -x test

# -------------------------------------
# 4️⃣ 생성된 JAR 파일 찾기
# -------------------------------------
JAR_FILE=$(find build/libs -name "*-SNAPSHOT.jar" | grep -v 'plain' | head -n 1)

if [ -z "$JAR_FILE" ]; then
    echo "❌ bootJar 파일이 build/libs 디렉토리에서 발견되지 않았습니다. 종료합니다."
    exit 1
fi

echo "✅ 빌드 완료: $JAR_FILE"

# -------------------------------------
# 5️⃣ 애플리케이션 백그라운드 실행
# -------------------------------------
LOG_FILE="app.log"

echo "🚀 'prod' 프로필로 새로운 애플리케이션을 백그라운드에서 실행하고 있습니다..."
nohup java -jar -Dspring.profiles.active=prod "$JAR_FILE" > "$LOG_FILE" 2>&1 &

APP_PID=$!

echo "✅ 새로운 애플리케이션이 'prod' 프로필로 백그라운드에서 시작되었습니다."
echo "📝 로그는 '$LOG_FILE' 파일에서 확인할 수 있습니다."
echo "📝 실행된 프로세스의 PID는 $APP_PID 입니다."
