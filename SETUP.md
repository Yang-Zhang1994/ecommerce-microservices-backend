# Gulimall Project Deployment Guide

## Prerequisites

### Required Environment
- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Node.js 12+ (Frontend)
- Nacos 2.0+ (Service Registry and Configuration Center)
- Docker (Optional, for containerized deployment)

## Quick Start

### 1. Clone Project

```bash
git clone https://github.com/your-username/gulimall.git
cd gulimall
```

### 2. Database Configuration

#### 2.1 Create Databases

Execute SQL scripts to create databases and table structures:

```sql
-- Create databases
CREATE DATABASE gulimall_pms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE gulimall_oms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE gulimall_ums DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE gulimall_wms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE DATABASE gulimall_sms DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

#### 2.2 Create Tables and Import Data

**Step 1: Create Databases**

Execute the database creation script:

```bash
mysql -u root -p < sql/01-create-databases.sql
```

**Step 2: Extract SQL Files (if not already extracted)**

The project includes `db.rar` in the root directory. All SQL files are already in the `sql/` directory. If you need to extract from `db.rar`:

**On Mac/Linux:**
```bash
cd sql
unar ../db.rar .
mv db/*.sql . && rm -rf db  # Move SQL files to sql/ directory
```

**On Windows:**
- Use WinRAR or 7-Zip to extract `db.rar` to the `sql/` directory

**Note**: The `sql/` directory already contains all necessary SQL files:
- `01-create-databases.sql` - Database creation script
- `gulimall_admin.sql` - System admin database
- `gulimall_pms.sql` - Product Management System schema
- `pms_catelog.sql` - Three-level category data for products
- `gulimall_oms.sql` - Order Management System
- `gulimall_ums.sql` - User Management System
- `gulimall_wms.sql` - Warehouse Management System
- `gulimall_sms.sql` - System Management Service

**Step 3: Import SQL Files**

Import SQL files in the following order:

```bash
# System admin database
mysql -u root -p gulimall_admin < sql/gulimall_admin.sql

# Product Management System (import schema first, then category data)
mysql -u root -p gulimall_pms < sql/gulimall_pms.sql
mysql -u root -p gulimall_pms < sql/pms_catelog.sql  # Three-level category data

# Order Management System
mysql -u root -p gulimall_oms < sql/gulimall_oms.sql

# User Management System
mysql -u root -p gulimall_ums < sql/gulimall_ums.sql

# Warehouse Management System
mysql -u root -p gulimall_wms < sql/gulimall_wms.sql

# System Management Service
mysql -u root -p gulimall_sms < sql/gulimall_sms.sql
```

**Important**: Import `pms_catelog.sql` after `gulimall_pms.sql` to populate the category table with three-level category data.

**Alternative: Manual Table Creation**

If you prefer to create tables manually, refer to entity classes in each service module:
- `gulimall-product/src/main/java/com/xunqi/gulimall/product/entity/`
- `gulimall-order/src/main/java/com/xunqi/gulimall/order/entity/`
- `gulimall-member/src/main/java/com/xunqi/gulimall/member/entity/`
- `gulimall-ware/src/main/java/com/xunqi/gulimall/ware/entity/`
- `gulimall-coupon/src/main/java/com/xunqi/gulimall/coupon/entity/`

For more details, see [sql/README.md](sql/README.md).

### 3. Configuration File Setup

#### 3.1 Copy Configuration File Templates

Copy configuration file templates for each service:

```bash
# Example: Product service
cd gulimall-product/src/main/resources
cp application.yml.example application.yml
cp bootstrap.yml.example bootstrap.yml
```

#### 3.2 Modify Configuration

Edit `application.yml` and modify the following configurations:

```yaml
spring:
  datasource:
    username: your_db_username
    password: your_db_password
    url: jdbc:mysql://your_db_host:3306/gulimall_pms?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
  
  cloud:
    nacos:
      discovery:
        server-addr: your_nacos_host:8848
```

### 4. Start Nacos

```bash
# Using Docker
docker run -d -p 8848:8848 --name nacos nacos/nacos-server:v2.2.0

# Or using local installation
cd nacos/bin
sh startup.sh -m standalone  # Linux/Mac
startup.cmd -m standalone     # Windows
```

Access Nacos Console: http://localhost:8848/nacos
- Default username/password: nacos/nacos

### 5. Start Backend Services

#### 5.1 Build Project

```bash
mvn clean install -DskipTests
```

#### 5.2 Startup Order

It is recommended to start services in the following order:

1. **gulimall-gateway** - API Gateway
2. **gulimall-product** - Product Service
3. **gulimall-member** - Member Service
4. **gulimall-coupon** - Coupon Service
5. **gulimall-ware** - Warehouse Service
6. **gulimall-order** - Order Service
7. **gulimall-third-party** - Third-party Service

**Note**: The following services are not yet implemented:
- gulimall-cart (Cart Service)
- gulimall-search (Search Service)
- gulimall-seckill (Seckill Service)
- gulimall-auth-server (Authentication Service)

#### 5.3 Startup Methods

**Method 1: Using IDE**
- Import project in IDEA
- Run the `Application` main class of each service

**Method 2: Using Command Line**
```bash
cd gulimall-gateway
mvn spring-boot:run
```

### 6. Start Frontend

```bash
cd renren-fast-vue
npm install
npm run dev
```

Frontend service will run at: http://localhost:8001

### 7. Configure Host (Optional)

If you need to access using domain names, modify the local hosts file:

**Windows**: `C:\Windows\System32\drivers\etc\hosts`
**Linux/Mac**: `/etc/hosts`

```
127.0.0.1    gulimall.com
127.0.0.1    search.gulimall.com
127.0.0.1    item.gulimall.com
127.0.0.1    auth.gulimall.com
127.0.0.1    cart.gulimall.com
127.0.0.1    order.gulimall.com
127.0.0.1    member.gulimall.com
127.0.0.1    seckill.gulimall.com
```

## Environment Variable Configuration (Recommended)

Use environment variables to manage sensitive configurations:

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export DB_HOST=localhost
export NACOS_SERVER_ADDR=localhost:8848
```

## Docker Deployment

### Build Images

```bash
# Build Docker images for each service
docker build -t gulimall-gateway:latest ./gulimall-gateway
docker build -t gulimall-product:latest ./gulimall-product
# ... other services
```

### Using Docker Compose

Create a `docker-compose.yml` file to start all services with one command.

## Common Issues

### 1. Service Registration Failed

- Check if Nacos is running
- Check if Nacos address configuration is correct
- Check network connection

### 2. Database Connection Failed

- Check if database is running
- Check if username and password are correct
- Check if database exists

### 3. Frontend Cannot Access Backend

- Check if backend services are running
- Check gateway configuration
- Check CORS configuration

## Verify Deployment

1. Access Nacos console and confirm all services are registered
2. Access frontend admin interface: http://localhost:8001
3. Test API endpoint: http://localhost:88/api/product/category/list/tree

## Production Environment Recommendations

1. Use configuration center (Nacos) to manage configurations, avoid hardcoding
2. Use environment variables to manage sensitive information
3. Configure log collection and monitoring
4. Use HTTPS
5. Configure database connection pool
6. Use message queue to handle asynchronous tasks
7. Configure service rate limiting and circuit breaking

## Technical Support

If you have any questions, please submit an Issue or contact the project maintainer.
