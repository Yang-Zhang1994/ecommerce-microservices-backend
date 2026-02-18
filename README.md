# E-Commerce Microservices Backend (Gulimall)

E-commerce backend and admin system built as a **microservices** suite: Spring Boot 3 + Spring Cloud, with a Vue.js admin frontend. Services are discovered via Consul and exposed through an API gateway.

**Repository:** [https://github.com/Yang-Zhang1994/ecommerce-microservices-backend](https://github.com/Yang-Zhang1994/ecommerce-microservices-backend)

---

## Features

- **Microservices**: Product, Member, Order, Coupon, Ware (inventory), Third-party (e.g. S3/OSS), Search
- **API Gateway**: Single entry point (port 88), path-based routing to backend services
- **Service discovery**: Consul (Docker Compose for local dev); health checks use `prefer-ip-address` for reliable registration
- **Admin backend**: Renren-fast (Spring Boot) for auth, captcha, and admin APIs (context-path `/api`)
- **Admin frontend**: Renren-fast-vue (Vue 2 + Element UI) for product, order, member, coupon, and warehouse management (English UI where updated)
- **Inter-service calls**: HTTP Interface (RestClient) + Consul; e.g. Product → Coupon for SPU bounds / SKU reduction

---

## Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 17, Spring Boot 3.2, Spring Cloud 2023 |
| **Service discovery** | Consul |
| **Gateway** | Spring Cloud Gateway |
| **Data** | PostgreSQL (per-service DBs) |
| **Frontend** | Vue 2, Element UI, Vuex, Vue Router, Axios |

---

## Project Structure

```
gulimall/
├── gulimall-common       # Shared utils, DTOs, constants, CouponApi (HTTP client)
├── gulimall-gateway      # API Gateway (port 88)
├── gulimall-product      # Product / catalog service (port 10000)
├── gulimall-member       # Member service (port 8000)
├── gulimall-order        # Order service
├── gulimall-coupon       # Coupon / promotion service (port 12000)
├── gulimall-ware         # Warehouse / inventory service
├── gulimall-third-party  # Third-party e.g. S3 presign (port 30000)
├── gulimall-search       # Search service
├── renren-fast           # Admin backend (port 8080, context-path /api)
├── renren-generator      # Code generator
├── renren-fast-vue       # Admin frontend (port 8001)
├── docker-compose.yml    # Consul for local dev
├── docs/                 # SQL and docs
│   ├── sms_missing_tables_postgresql.sql   # Coupon (ecommerce_sms) missing tables
│   ├── sms_spu_bounds_postgresql.sql       # SPU bounds table
│   ├── pms_missing_tables_postgresql.sql   # Product (pms) missing tables
│   └── ...
└── scripts/              # Run scripts, API gateway setup
```

---

## Prerequisites

- **JDK 17**
- **Maven 3.6+**
- **PostgreSQL** (per-service databases)
- **Node.js 12+** (for admin frontend)
- **Docker** (for Consul via `docker-compose`)

---

## Database Setup

Create PostgreSQL databases and run schema scripts as needed:

| Service | Database (example) | Notes |
|---------|--------------------|--------|
| Product | `gulimall_pms` | Use `docs/pms_*.sql`, `docs/pms_missing_tables_postgresql.sql` |
| Coupon | `ecommerce_sms` | Use `docs/sms_missing_tables_postgresql.sql`, `docs/sms_spu_bounds_postgresql.sql` |
| Member / Order / Ware / Renren-fast | See each module's `application*.yml` | Run SQL under `renren-fast/db/` (e.g. `postgresql.sql`, `sys_menu_postgresql.sql`) |

---

## Quick Start

### 1. Consul (start first)

```bash
docker compose up -d
```

- UI: http://localhost:8500  
- Stop: `docker compose down`

### 2. Build

```bash
mvn clean install -DskipTests
```

### 3. Backend services

Start in order (after Consul is up): **Gateway** (88) → **renren-fast** (8080) → **gulimall-product** (10000) → **gulimall-member** (8000) → **gulimall-coupon** (12000). Optionally **gulimall-third-party** (30000), **gulimall-ware**, **gulimall-order**, **gulimall-search**.

- **Gateway**: http://localhost:88  
- **Routes**: `/api/product/**`, `/api/member/**`, `/api/coupon/**`, `/api/ware/**`, `/api/third-party/**`, `/api/**` (admin) → renren-fast; captcha at `/api/captcha.jpg`

Use each module's `application.yml` (and `application-dev.yml`) for DB URL, user, password.

### 4. Admin frontend

```bash
cd renren-fast-vue
npm install
npm run dev
```

Open the URL shown (e.g. http://localhost:8001). Set base API URL to `http://localhost:88/api` (e.g. in `static/config/index.js`).

---

## Implemented / Current Status

- Consul registration with `prefer-ip-address: true` (gateway, product, coupon, member, renren-fast, third-party) for passing health checks
- Gateway routing: admin and captcha via `/api/**` to renren-fast
- Product SPU save: `saveSpuInfo` with CouponApi `saveSpuBounds` / `saveSkuReduction`; CouponApi uses `@RequestBody` on body params; coupon `/coupon/spubounds/save` accepts `SpuBoundTo` and maps to `SpuBoundsEntity`
- Coupon SMS tables: DDL in `docs/sms_missing_tables_postgresql.sql` for missing `sms_*` tables
- Product PMS tables: DDL in `docs/pms_missing_tables_postgresql.sql` for missing `pms_*` tables
- Admin UI: Specification Maintenance and SPU Management placeholders/labels in English where updated; brand select "Select", attrupdate "Quick Display" / "Confirm Changes"

---

## Configuration

- **Per-service**: Each module has `application.yml` (and often `application-dev.yml`). Set DB URLs, usernames, and passwords for your environment.
- **Secrets**: Do not commit real passwords or API keys; use environment variables or local config.

---

## License

Apache-2.0 (see [LICENSE](LICENSE)).
