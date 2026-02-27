# E-Commerce Microservices Backend

E-commerce backend and admin system built as a **microservices** suite: Spring Boot 3 + Spring Cloud, with a Vue.js admin frontend. Services are discovered via Consul and exposed through an API gateway.

**Repository:** [https://github.com/Yang-Zhang1994/ecommerce-microservices-backend](https://github.com/Yang-Zhang1994/ecommerce-microservices-backend)

---

## Overview

- **Microservices:** Product, Member, Order, Coupon, Ware (inventory), Third-party (e.g. S3), Search
- **API Gateway:** Single entry point (port 88), path-based routing to backend services
- **Service discovery:** Consul (Docker Compose); health checks use `prefer-ip-address` for reliable registration
- **Admin:** Renren-fast (Spring Boot) for auth, captcha, and admin APIs; Renren-fast-vue (Vue 2 + Element UI) for product, order, member, coupon, and warehouse management
- **Inter-service calls:** HTTP Interface (RestClient) + Consul; e.g. Product → Coupon for SPU bounds / SKU reduction
- **AWS:** Database (RDS PostgreSQL), config center (Secrets Manager + Parameter Store), object storage (S3 presigned URL upload)

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| Backend | Java 17, Spring Boot 3.2, Spring Cloud 2023 |
| Service discovery | Consul |
| Gateway | Spring Cloud Gateway |
| Inter-service calls | **HTTP Interface** (Spring 6 `@HttpExchange`), **RestClient** (load-balanced via Consul) |
| Data | PostgreSQL (**AWS RDS** per-service) |
| Config | **AWS Secrets Manager**, **AWS Systems Manager Parameter Store** |
| Object storage | **AWS S3** (presigned URL upload via gulimall-third-party) |
| Frontend | Vue 2, Element UI, Vuex, Vue Router, Axios |

---

## Project Structure

```
gulimall/
├── gulimall-common       # Shared utils, DTOs, CouponApi (HTTP client)
├── gulimall-gateway      # API Gateway (port 88)
├── gulimall-product      # Product service (port 10000)
├── gulimall-member       # Member service (port 8000)
├── gulimall-coupon       # Coupon service (port 12000)
├── gulimall-ware         # Warehouse service
├── gulimall-order        # Order service
├── gulimall-third-party  # Third-party e.g. S3 presign (port 30000)
├── gulimall-search       # Search service
├── renren-fast           # Admin backend (port 8080, context-path /api)
├── renren-fast-vue       # Admin frontend (port 8001)
└── docker-compose.yml    # Consul for local dev
```




---

## AWS / Cloud

The project supports **AWS** for production-style deployment:

- **Database (AWS RDS):** With profile `dev`, services can connect to **Amazon RDS for PostgreSQL** (one RDS instance, multiple databases per service: e.g. `ecommerce_pms`, `ecommerce_sms`, `ecommerce_ums`, `ecommerce_oms`, `ecommerce_wms`, `ecommerce_admin`). Each module has `application-dev.yml` with the RDS endpoint; credentials via config or IAM.

- **Config center:** **AWS Secrets Manager** and **AWS Systems Manager Parameter Store** are used as optional config sources. For example:
  - **Gateway:** `optional:aws-secretsmanager:gulimall/gateway/config`, `optional:aws-parameterstore:/gulimall/gateway/`
  - **Member:** `optional:aws-secretsmanager:gulimall/member/datasource`, `optional:aws-parameterstore:/gulimall/member/`  
  Spring Cloud AWS loads these when available; services still start without AWS (e.g. local).

- **Object storage (AWS S3):** **gulimall-third-party** exposes presigned URL upload so the frontend can upload files directly to **Amazon S3** (e.g. bucket in us-east-2). Credentials via `AWS_ACCESS_KEY_ID` / `AWS_SECRET_ACCESS_KEY` or `~/.aws/credentials`. S3 client and presigner are created when `aws.s3.bucket` is set.

---

## Prerequisites

- JDK 17, Maven 3.6+, PostgreSQL, Node.js 12+, Docker (for Consul)

---

## Quick Start

**1. Consul (start first)**  
`docker compose up -d` — UI: http://localhost:8500

**2. Build**  
`mvn clean install -DskipTests`

**3. Backend**  
Start after Consul: Gateway (88) → renren-fast (8080) → gulimall-product (10000) → gulimall-member (8000) → gulimall-coupon (12000). Optionally gulimall-third-party (30000), gulimall-ware, gulimall-order, gulimall-search.

**Gateway routes:** `/api/product/**`, `/api/member/**`, `/api/coupon/**`, `/api/ware/**`, `/api/third-party/**`, `/api/**` (admin → renren-fast). Captcha: `/api/captcha.jpg`.

**4. Frontend**  
`cd renren-fast-vue && npm install && npm run dev` — open the URL (e.g. http://localhost:8001); set API base to `http://localhost:88/api`.

**Database:** Create PostgreSQL DBs per service (e.g. `gulimall_pms`, `ecommerce_sms`); run SQL under `renren-fast/db/` as needed.

---

## Configuration

Each module has `application.yml` (and often `application-dev.yml`). **Database credentials are not in the repo**; set environment variables for your environment:
- `SPRING_DATASOURCE_URL` (e.g. `jdbc:postgresql://localhost:5432/your_db`)
- `SPRING_DATASOURCE_USERNAME`
- `SPRING_DATASOURCE_PASSWORD`

Defaults in config are localhost URL and empty password. **Local dev:** set `SPRING_DATASOURCE_PASSWORD` in your environment or in an IDE run configuration so the app can connect (e.g. `SPRING_DATASOURCE_PASSWORD=your_local_password`). Optionally use a local-only file `application-local.yml` with your password and run with profile `local`; add `**/application-local.yml` to `.gitignore` (already ignored). For **AWS**: use `AWS_ACCESS_KEY_ID`, `AWS_SECRET_ACCESS_KEY`, `AWS_REGION`, or IAM roles; do not commit secrets.

---

## License

Apache-2.0 (see [LICENSE](LICENSE)).
