# 프론트엔드 Dockerfile (frontend/Dockerfile)
FROM nginx:alpine

# 빌드된 React 파일을 nginx 서버로 복사
COPY build/ /usr/share/nginx/html/

COPY nginx.conf /etc/nginx/conf.d/default.conf

# 포트 노출
EXPOSE 80

# nginx 실행
CMD ["nginx", "-g", "daemon off;"]