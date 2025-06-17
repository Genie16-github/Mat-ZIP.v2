FROM gradle:7.4-jdk17-alpine AS builder
WORKDIR /app

# 필요한 파일 복사 및 캐싱
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle dependencies --no-daemon || return 0

# 전체 복사 및 빌드
COPY . .
RUN chmod +x ./gradlew
RUN ./gradlew clean build -x test --no-daemon

# 런타임 스테이지: 빌드된 JAR만 복사
FROM openjdk:17-jdk-alpine
COPY --from=builder /app/build/libs/*.jar /app/app.jar

# 컨테이너 포트 노출
EXPOSE 8080

# 실행 명령
ENTRYPOINT ["java", "-jar", "-Dspring.profiles.active=prod", "/app/app.jar"]
