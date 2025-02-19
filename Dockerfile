# 빌드 스테이지
FROM gradle:7.4-jdk17-alpine AS build
ARG GRADLE_USER_HOME="/gradle-cache/.gradle"
ENV GRADLE_USER_HOME="${GRADLE_USER_HOME}"
WORKDIR /app
RUN mkdir -p "${GRADLE_USER_HOME}"

# Gradle 의존성 캐시
COPY build.gradle settings.gradle ./
COPY gradle ./gradle
RUN gradle --no-daemon dependencies || return 0

# 프로젝트 전체 복사
COPY . /app
RUN chmod +x ./gradlew && gradle --no-daemon clean build

# 테스트 실행 여부에 따라 빌드 방식 결정
RUN if [ "$SKIP_TESTS" = "true" ]; then \
        ./gradlew clean build -x test; \
    else \
        ./gradlew clean build; \
    fi

# 런타임 스테이지
FROM openjdk:17-jdk-alpine
COPY --from=build /app/build/libs/*.jar /app.jar
EXPOSE 8080
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod","/app.jar"]