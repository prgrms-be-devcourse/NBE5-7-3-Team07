# 백엔드 Dockerfile (backend/Dockerfile)
FROM openjdk:21-slim

# 작업 디렉토리 설정
WORKDIR /app

ENV TZ=Asia/Seoul

# JAR 파일을 컨테이너로 복사
COPY build/libs/*.jar app.jar

# 포트 노출
EXPOSE 8080

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]