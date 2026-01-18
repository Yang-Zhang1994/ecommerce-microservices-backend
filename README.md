# Gulimall - E-commerce Backend Management System

[![Java](https://img.shields.io/badge/Java-1.8-orange.svg)](https://www.oracle.com/java/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-2.7.x-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![Spring Cloud](https://img.shields.io/badge/Spring%20Cloud-2021.x-blue.svg)](https://spring.io/projects/spring-cloud)
[![Vue](https://img.shields.io/badge/Vue-2.x-green.svg)](https://vuejs.org/)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

## 📖 Project Introduction

Gulimall is an e-commerce backend management system based on microservices architecture, using frontend-backend separation development mode. This project focuses on the backend management system, providing CRUD operations for core business modules including product management, order management, member management, inventory management, and promotional marketing.

### ✨ Project Highlights

- 🏗️ **Microservices Architecture**: Built on Spring Cloud Alibaba, with clear service separation
- 🌐 **Frontend-Backend Separation**: Vue.js + Element UI frontend, Spring Boot backend, RESTful API
- 🌍 **English Interface**: Complete English interface for better international project presentation
- 📊 **Service Governance**: Nacos service registration and discovery
- 🔧 **Code Generator**: Integrated code generator for rapid development

### 🎯 Implemented Features

#### Backend Management System

This project is currently in development. The following modules have basic CRUD functionality implemented:

**Product System**
- Category management (CRUD)
- Brand management (CRUD)
- Attribute group management (CRUD)
- Base attributes management (CRUD)
- Sale attributes management (CRUD)
- SPU management (CRUD)

**Order System**
- Order inquiry
- Return order processing (CRUD)
- Payment flow inquiry (CRUD)
- Refund flow inquiry (CRUD)
- Order settings (CRUD)

**Member System**
- Member management (CRUD)
- Member level management (CRUD)

**Promotional Marketing**
- Coupon management (CRUD)
- Coupon issuance record
- Flash sale activities (CRUD)
- Full reduction discount (CRUD)
- Member price (CRUD)
- Points maintenance (CRUD)
- Daily flash sale (CRUD)
- Homepage recommendation (CRUD)
- Special activities (CRUD)

**Inventory System**
- Warehouse maintenance (CRUD)
- Product inventory (CRUD)
- Inventory work order (CRUD)
- Purchase requirements (CRUD)
- Purchase order (CRUD)

**System Management**
- Admin management
- Role management
- Menu management
- Permission management

> **Note**: Some modules may have frontend pages but backend implementation is still in progress. The project structure is set up for microservices architecture, but not all features are fully implemented yet.

## 🏗️ Technical Architecture

### System Architecture

```
┌─────────────┐
│  API Gateway│ Spring Cloud Gateway
└──────┬──────┘
       │
   ┌───┴───┬────────┬────────┬────────┐
   │       │        │        │        │
┌──▼──┐ ┌─▼──┐ ┌──▼──┐ ┌──▼──┐ ┌──▼──┐
│Product│ │Order│ │Member│ │Coupon│ │Ware│
│Service│ │Service│ │Service│ │Service│ │Service│
└──┬──┘ └─┬──┘ └──┬──┘ └──┬──┘ └──┬──┘
   │      │       │       │       │
┌──▼──────▼───────▼───────▼───────▼──┐
│            MySQL + Nacos            │
└────────────────────────────────────┘
```

### Tech Stack

#### Backend Technologies

| Technology | Description | Version |
|:---:|:---:|:---:|
| Spring Boot | Container + MVC Framework | 2.7.x |
| Spring Cloud | Microservices Architecture | 2021.x |
| Spring Cloud Alibaba | Microservices Components | 2021.x |
| MyBatis-Plus | ORM Framework | 3.5.x |
| Nacos | Service Registry and Configuration Center | 2.0+ |

#### Frontend Technologies

| Technology | Description | Version |
|:---:|:---:|:---:|
| Vue.js | Frontend Framework | 2.x |
| Element UI | UI Component Library | 2.x |
| Axios | HTTP Client | - |
| Vue Router | Route Management | - |
| Vuex | State Management | - |

## 📁 Project Structure

```
gulimall/
├── gulimall-common          # Common utility classes
├── gulimall-gateway         # API Gateway
├── gulimall-product         # Product Service
├── gulimall-member          # Member Service
├── gulimall-coupon          # Coupon Service
├── gulimall-ware            # Warehouse Service
├── gulimall-order           # Order Service
├── gulimall-third-party     # Third-party Service
└── renren-fast-vue          # Frontend Admin Interface
```

## 🚀 Quick Start

### Environment Requirements

- JDK 1.8+
- Maven 3.6+
- MySQL 5.7+
- Node.js 12+ (Frontend)
- Nacos 2.0+

### Deployment Steps

For detailed deployment instructions, please refer to [SETUP.md](SETUP.md)

### Project Documentation

- [SETUP.md](SETUP.md) - Deployment and setup guide
- [CONFIG.md](CONFIG.md) - Configuration file guide
- [docs/API.md](docs/API.md) - API documentation
- [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) - Architecture design document

#### 1. Clone Project

```bash
git clone https://github.com/Yang-Zhang1994/gulimall.git
cd gulimall
```

#### 2. Database Initialization

1. Create databases using `sql/01-create-databases.sql`
2. Import SQL files from `sql/` directory into corresponding databases
3. **Note**: Import `pms_catelog.sql` after `gulimall_pms.sql` to populate three-level category data
4. See [sql/README.md](sql/README.md) for detailed instructions and complete SQL file list

#### 3. Configuration Files

Copy configuration file templates and modify:

```bash
cd gulimall-product/src/main/resources
cp application.yml.example application.yml
# Edit application.yml, modify database, etc.
```

#### 4. Start Services

1. Start Nacos
2. Start each microservice (recommended to use IDE)
3. Start frontend: `cd renren-fast-vue && npm install && npm run dev`

#### 5. Access System

- Frontend admin interface: http://localhost:8001
- Nacos console: http://localhost:8848/nacos (nacos/nacos)

## 📚 Project Documentation

- [Deployment Guide](SETUP.md) - Detailed deployment and configuration instructions
- [Configuration Guide](CONFIG.md) - Configuration file instructions
- [API Documentation](docs/API.md) - RESTful API interface documentation
- [Architecture Design](docs/ARCHITECTURE.md) - System architecture and design patterns

## 🎨 Project Screenshots

### Backend Management System

![Backend Login](https://images.gitee.com/uploads/images/2020/0714/193422_cd40fc31_4914148.png)

![Product Management](https://images.gitee.com/uploads/images/2020/0714/193422_d1a789d5_4914148.png)


## 📝 Development Standards

1. Run `mvn clean install` before committing code to ensure compilation passes
2. Follow Alibaba Java Development Manual standards
3. Update API documentation in a timely manner
4. Use `.example` templates for configuration files, use environment variables for sensitive information

## 🤝 Contributing

Welcome to submit Issues and Pull Requests!

1. Fork this repository
2. Create a feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details

## 🙏 Acknowledgments

- [Spring Cloud Alibaba](https://github.com/alibaba/spring-cloud-alibaba)
- [Renren Open Source](https://gitee.com/renrenio)
- [Element UI](https://element.eleme.io/)

## 📮 Contact

If you have any questions or suggestions, please contact us through:

- Submit an Issue
- Send an email

