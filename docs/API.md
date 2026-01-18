# API Documentation

## Overview

This document describes the RESTful API endpoints provided by the Gulimall e-commerce backend management system. All APIs are accessed through the API Gateway at port 88, with the base path `/api`.

## Base URL

```
http://localhost:88/api
```

## Response Format

All API responses follow a standard format:

```json
{
  "code": 0,
  "msg": "success",
  "data": {}
}
```

- `code`: Response code (0 = success, non-zero = error)
- `msg`: Response message
- `data`: Response data (varies by endpoint)

## Authentication

### Management System (Admin Frontend)

The admin frontend uses **token-based authentication** with the following flow:

1. **Login**: POST `/sys/login` with username, password, and captcha
2. **Token Storage**: Token is stored in a cookie after successful login
3. **Request Authentication**: Token is automatically included in request headers by the HTTP interceptor:

```
token: {token}
```

**Location in Code:**
- Frontend Login: `renren-fast-vue/src/views/common/login.vue`
- HTTP Interceptor: `renren-fast-vue/src/utils/httpRequest.js` (line 20)
- Token stored in Cookie: `Vue.cookie.set('token', data.token)`

### Business Services (Microservices)

Business services (Order, Member, Seckill) use **Session-based authentication**:

1. User information is stored in HTTP Session after login
2. Services use `LoginUserInterceptor` to check session for user information
3. If not logged in, requests are redirected to login page

**Location in Code:**
- `gulimall-order/src/main/java/com/xunqi/gulimall/order/interceptor/LoginUserInterceptor.java`
- `gulimall-member/src/main/java/com/xunqi/gulimall/member/interceptor/LoginUserInterceptor.java`
- `gulimall-seckill/src/main/java/com/xunqi/gulimall/seckill/interceptor/LoginUserInterceptor.java`

**Note**: The `gulimall-auth-server` (OAuth2/JWT authentication service) exists in the codebase but is not yet fully implemented. The current implementation uses basic session-based authentication.

## API Endpoints

### Product Service (`/api/product`)

#### Category Management

**Get Category Tree**
```
GET /api/product/category/list/tree
```
Returns all categories in a tree structure.

**Response:**
```json
{
  "code": 0,
  "msg": "success",
  "data": [
    {
      "catId": 1,
      "name": "Electronics",
      "parentCid": 0,
      "catLevel": 1,
      "children": [...]
    }
  ]
}
```

**Get Category Info**
```
GET /api/product/category/info/{catId}
```
Get category details by ID.

**Create Category**
```
POST /api/product/category/save
Content-Type: application/json

{
  "name": "Category Name",
  "parentCid": 0,
  "catLevel": 1,
  "showStatus": 1,
  "sort": 0
}
```

**Update Category**
```
POST /api/product/category/update
Content-Type: application/json

{
  "catId": 1,
  "name": "Updated Name",
  ...
}
```

**Delete Category**
```
POST /api/product/category/delete
Content-Type: application/json

[1, 2, 3]
```

**Update Category Sort**
```
POST /api/product/category/update/sort
Content-Type: application/json

[
  {"catId": 1, "sort": 0},
  {"catId": 2, "sort": 1}
]
```

#### Brand Management

**List Brands**
```
GET /api/product/brand/list?page=1&limit=10&key=
```
Query parameters:
- `page`: Page number (default: 1)
- `limit`: Items per page (default: 10)
- `key`: Search keyword (optional)

**Get Brand Info**
```
GET /api/product/brand/info/{brandId}
```

**Create Brand**
```
POST /api/product/brand/save
Content-Type: application/json

{
  "name": "Brand Name",
  "logo": "logo_url",
  "descript": "Description",
  "showStatus": 1,
  "firstLetter": "B",
  "sort": 0
}
```

**Update Brand**
```
POST /api/product/brand/update
Content-Type: application/json

{
  "brandId": 1,
  "name": "Updated Brand",
  ...
}
```

**Delete Brand**
```
POST /api/product/brand/delete
Content-Type: application/json

[1, 2, 3]
```

#### SPU Management

**List SPUs**
```
GET /api/product/spuinfo/list?page=1&limit=10&key=&catId=&brandId=&status=
```

**Get SPU Info**
```
GET /api/product/spuinfo/info/{id}
```

**Create SPU**
```
POST /api/product/spuinfo/save
Content-Type: application/json

{
  "spuName": "Product Name",
  "spuDescription": "Description",
  "catalogId": 1,
  "brandId": 1,
  "weight": 1.5,
  ...
}
```

**Update SPU**
```
POST /api/product/spuinfo/update
```

**Delete SPU**
```
POST /api/product/spuinfo/delete
Content-Type: application/json

[1, 2, 3]
```

**Publish SPU**
```
POST /api/product/spuinfo/{spuId}/up
```

#### SKU Management

**List SKUs**
```
GET /api/product/skuinfo/list?page=1&limit=10&key=&catId=&brandId=&min=&max=
```

**Get SKU Info**
```
GET /api/product/skuinfo/info/{skuId}
```

**Get SKU Info by SpuId**
```
GET /api/product/skuinfo/info/{spuId}/sku
```

### Order Service (`/api/order`)

#### Order Management

**List Orders**
```
GET /api/order/order/list?page=1&limit=10&key=
```

**Get Order Info**
```
GET /api/order/order/info/{id}
```

**Get Order Status**
```
GET /api/order/order/status/{orderSn}
```

**List Orders with Items**
```
POST /api/order/order/listWithItem
Content-Type: application/json

{
  "page": 1,
  "limit": 10
}
```

**Create Order**
```
POST /api/order/order/submitOrder
Content-Type: application/json

{
  "addressId": 1,
  "orderToken": "token",
  "payType": 1
}
```

#### Return Order Management

**List Return Orders**
```
GET /api/order/returnreason/list?page=1&limit=10
```

**Get Return Order Info**
```
GET /api/order/returnreason/info/{id}
```

**Create Return Order**
```
POST /api/order/returnreason/save
```

**Update Return Order**
```
POST /api/order/returnreason/update
```

**Delete Return Order**
```
POST /api/order/returnreason/delete
```

### Member Service (`/api/member`)

#### Member Management

**List Members**
```
GET /api/member/member/list?page=1&limit=10&key=
```

**Get Member Info**
```
GET /api/member/member/info/{id}
```

**Create Member**
```
POST /api/member/member/save
```

**Update Member**
```
POST /api/member/member/update
```

**Delete Member**
```
POST /api/member/member/delete
```

#### Member Level Management

**List Member Levels**
```
GET /api/member/memberlevel/list
```

**Get Member Level Info**
```
GET /api/member/memberlevel/info/{id}
```

**Create Member Level**
```
POST /api/member/memberlevel/save
```

**Update Member Level**
```
POST /api/member/memberlevel/update
```

**Delete Member Level**
```
POST /api/member/memberlevel/delete
```

### Coupon Service (`/api/coupon`)

#### Coupon Management

**List Coupons**
```
GET /api/coupon/coupon/list?page=1&limit=10&key=
```

**Get Coupon Info**
```
GET /api/coupon/coupon/info/{id}
```

**Create Coupon**
```
POST /api/coupon/coupon/save
Content-Type: application/json

{
  "couponType": 0,
  "couponImg": "image_url",
  "couponName": "Coupon Name",
  "num": 100,
  "amount": 10.00,
  "perLimit": 1,
  "minPoint": 100.00,
  "startTime": "2024-01-01 00:00:00",
  "endTime": "2024-12-31 23:59:59",
  "useType": 0,
  "note": "Notes",
  "publishCount": 100,
  "useCount": 0,
  "receiveCount": 0,
  "enableStartTime": "2024-01-01 00:00:00",
  "enableEndTime": "2024-12-31 23:59:59",
  "code": "COUPON_CODE",
  "memberLevel": 0,
  "publish": 0
}
```

**Update Coupon**
```
POST /api/coupon/coupon/update
```

**Delete Coupon**
```
POST /api/coupon/coupon/delete
```

#### Flash Sale Management

**List Flash Sale Activities**
```
GET /api/coupon/seckillpromotion/list
```

**Get Flash Sale Info**
```
GET /api/coupon/seckillpromotion/info/{id}
```

**Create Flash Sale**
```
POST /api/coupon/seckillpromotion/save
```

**Update Flash Sale**
```
POST /api/coupon/seckillpromotion/update
```

**Delete Flash Sale**
```
POST /api/coupon/seckillpromotion/delete
```

### Warehouse Service (`/api/ware`)

#### Warehouse Management

**List Warehouses**
```
GET /api/ware/wareinfo/list?page=1&limit=10&key=
```

**Get Warehouse Info**
```
GET /api/ware/wareinfo/info/{id}
```

**Create Warehouse**
```
POST /api/ware/wareinfo/save
```

**Update Warehouse**
```
POST /api/ware/wareinfo/update
```

**Delete Warehouse**
```
POST /api/ware/wareinfo/delete
```

#### Inventory Management

**List Inventory**
```
GET /api/ware/waresku/list?page=1&limit=10&wareId=&skuId=
```

**Get Inventory Info**
```
GET /api/ware/waresku/info/{id}
```

**Create Inventory**
```
POST /api/ware/waresku/save
```

**Update Inventory**
```
POST /api/ware/waresku/update
```

**Delete Inventory**
```
POST /api/ware/waresku/delete
```

**Check Stock**
```
POST /api/ware/waresku/hasStock
Content-Type: application/json

[1, 2, 3]
```

### Third-party Service (`/api/thirdparty`)

#### File Upload

**Upload File (OSS)**
```
POST /api/thirdparty/oss/upload
Content-Type: multipart/form-data

file: [binary]
```

**Response:**
```json
{
  "code": 0,
  "msg": "success",
  "data": {
    "url": "https://oss.example.com/file.jpg"
  }
}
```

#### SMS Service

**Send SMS Code**

This is an internal service endpoint used by other services. It requires both phone number and verification code.

```
GET /api/thirdparty/sms/sendCode?phone=13800138000&code=123456
```

**Parameters:**
- `phone` (required): Phone number to send SMS to
- `code` (required): Verification code to send

**Response:**
```json
{
  "code": 0,
  "msg": "success"
}
```

**Note**: This endpoint is typically called internally by the auth service, not directly by clients. The auth service generates the verification code and then calls this endpoint to send it via SMS.

## Error Codes

| Code | Description |
|------|-------------|
| 0 | Success |
| 10001 | Parameter error |
| 10002 | Data not found |
| 10003 | Operation failed |
| 10004 | Unauthorized |
| 10005 | Forbidden |

## Rate Limiting

API requests are rate-limited to prevent abuse. Current limits:
- 100 requests per minute per IP for general endpoints
- 10 requests per minute per IP for sensitive operations

## Notes

1. All timestamps are in format: `yyyy-MM-dd HH:mm:ss`
2. All monetary values are in decimal format (e.g., 99.99)
3. Pagination parameters: `page` (starts from 1), `limit` (default: 10)
4. All DELETE operations accept an array of IDs in the request body
5. File uploads use multipart/form-data encoding

## Testing

You can test the APIs using:
- Postman
- curl
- Frontend admin interface (http://localhost:8001)

Example curl command:
```bash
curl -X GET "http://localhost:88/api/product/category/list/tree" \
  -H "Authorization: Bearer {token}"
```
