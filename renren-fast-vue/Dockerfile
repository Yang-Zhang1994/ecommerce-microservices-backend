FROM node:14-alpine AS builder
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm install --ignore-scripts --legacy-peer-deps
COPY . .
# Skip gulp (primordials issues); webpack build + prod config for nginx /api proxy
RUN NODE_ENV=production node build/build.js && \
    mkdir -p dist/config && \
    cp static/config/index-prod.js dist/config/index.js && \
    cat static/config/init.js >> dist/config/index.js

FROM nginx:1.27-alpine
COPY --from=builder /app/dist/ /usr/share/nginx/html/
COPY nginx-k8s.conf /etc/nginx/conf.d/default.conf
EXPOSE 80
