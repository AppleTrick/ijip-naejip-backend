# 1. 빌드 단계 (Builder)
FROM eclipse-temurin:17-jdk-jammy AS builder
WORKDIR /app

# 소스코드 복사
COPY . .

# gradlew 실행 권한 부여 및 빌드 (jar 파일 생성)
RUN chmod +x ./gradlew
RUN ./gradlew clean bootJar

# 2. 실행 단계 (Runtime)
FROM eclipse-temurin:17-jre-jammy
WORKDIR /app

# 빌드된 jar 파일만 가져오기
COPY --from=builder /app/build/libs/*SNAPSHOT.jar app.jar

# 8080 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-Dspring.profiles.active=prod", "-jar", "app.jar"]