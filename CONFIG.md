# Configuration File Guide

## Overview

This project uses configuration file templates (`.example` files) to manage sensitive information. Before deploying the project, you need to copy the template files to actual configuration files and fill in the correct configuration information.

## Configuration Steps

### 1. Copy Configuration File Templates

Copy configuration files for each service:

```bash
# Example: Product service
cd gulimall-product/src/main/resources
cp application.yml.example application.yml
cp bootstrap.yml.example bootstrap.yml
```

### 2. Modify Configuration Items

Edit `application.yml` and modify the following key configurations:

#### Database Configuration

```yaml
spring:
  datasource:
    username: your_db_username      # Database username
    password: your_db_password      # Database password
    url: jdbc:mysql://your_db_host:3306/gulimall_pms?useUnicode=true&characterEncoding=UTF-8&serverTimezone=Asia/Shanghai
```

#### Nacos Configuration

```yaml
spring:
  cloud:
    nacos:
      discovery:
        server-addr: your_nacos_host:8848  # Nacos service address
```

#### Third-party Service Configuration (gulimall-third-party)

```yaml
spring:
  cloud:
    alicloud:
      sms:
        host: https://fesms.market.alicloudapi.com
        path: /sms/
        skin: your_skin_id
        sign: your_sign
        appcode: your_appcode      # Alibaba Cloud SMS service appcode
```

## Environment Variable Configuration (Recommended)

For more secure management of sensitive information, it is recommended to use environment variables:

### Linux/Mac

```bash
export DB_USERNAME=root
export DB_PASSWORD=your_password
export DB_HOST=localhost
export NACOS_SERVER_ADDR=localhost:8848
export ALI_SMS_APPCODE=your_appcode
```

### Windows

```cmd
set DB_USERNAME=root
set DB_PASSWORD=your_password
set DB_HOST=localhost
set NACOS_SERVER_ADDR=localhost:8848
set ALI_SMS_APPCODE=your_appcode
```

Then use environment variables in configuration files:

```yaml
spring:
  datasource:
    username: ${DB_USERNAME:root}
    password: ${DB_PASSWORD:your_password}
    url: jdbc:mysql://${DB_HOST:localhost}:3306/gulimall_pms?...
```

## Configuration File List

Services that need configuration:

- `gulimall-gateway` - API Gateway
- `gulimall-product` - Product Service
- `gulimall-member` - Member Service
- `gulimall-coupon` - Coupon Service
- `gulimall-ware` - Warehouse Service
- `gulimall-order` - Order Service
- `gulimall-third-party` - Third-party Service

**Note**: The following services are not yet implemented and do not need configuration:
- `gulimall-cart` - Cart Service
- `gulimall-search` - Search Service
- `gulimall-seckill` - Seckill Service
- `gulimall-auth-server` - Authentication Service

## Important Notes

1. **Do not commit real configuration files to Git**: `.gitignore` has been configured to ignore all `application.yml` and `bootstrap.yml` files
2. **Use template files**: Only commit `.example` template files
3. **Protect sensitive information**: Do not hardcode passwords, keys, and other sensitive information in configuration files
4. **Use configuration center**: Production environment is recommended to use Nacos configuration center to manage configurations

## Configuration Verification

After configuration is complete, please verify before starting services:

1. Database connection is normal
2. Nacos service is accessible
3. Service ports do not conflict

## Troubleshooting

### Configuration Not Taking Effect

- Check if configuration file path is correct
- Check configuration file format (YAML indentation)
- Check if environment variables are set correctly

### Connection Failed

- Check if services are running (database, Nacos)
- Check network connection
- Check firewall settings
- Check if username and password are correct
