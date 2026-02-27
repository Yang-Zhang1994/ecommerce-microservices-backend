# 仅拷贝已构建的 dist，不在镜像内执行 gulp（避免 Build complete 后卡住）。
# 构建前必须在本地执行：./scripts/build-frontend-for-docker.sh
FROM nginx:1.25-alpine
COPY dist/ /usr/share/nginx/html/
# index.html 请求 ./config/index.js，但构建产物在 static/config/，必须复制到 config/ 否则浏览器会拿到 index.html 当 JS 解析 → 白屏 + Unexpected token '<'
RUN test -d /usr/share/nginx/html/static/config && cp -r /usr/share/nginx/html/static/config /usr/share/nginx/html/config || (echo "ERROR: dist/static/config not found. Run build script first." && exit 1)
COPY nginx.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
