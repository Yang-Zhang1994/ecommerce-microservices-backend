# Architecture Design Document

## Overview

Gulimall is an e-commerce backend management system built with microservices architecture. The system is designed to be scalable, maintainable, and follows modern software engineering best practices.

## System Architecture

### High-Level Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    Frontend Layer                        │
│              (Vue.js + Element UI)                      │
│                  http://localhost:8001                   │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                  API Gateway Layer                        │
│            Spring Cloud Gateway                           │
│                  http://localhost:88                     │
│              Route: /api/* → Services                    │
└──────────────────────┬──────────────────────────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
┌───────▼───┐  ┌───────▼───┐  ┌───────▼───┐
│  Product  │  │   Order   │  │  Member   │
│  Service  │  │  Service  │  │  Service  │
│  :10000   │  │  :9000    │  │  :8000    │
└───────────┘  └───────────┘  └───────────┘
        │              │              │
        └──────────────┼──────────────┘
                       │
        ┌──────────────┼──────────────┐
        │              │              │
┌───────▼───┐  ┌───────▼───┐  ┌───────▼───┐
│  Coupon   │  │    Ware    │  │  Third-   │
│  Service  │  │   Service  │  │  party    │
│  :7000    │  │  :11000    │  │  :30000   │
└───────────┘  └───────────┘  └───────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│              Service Registry & Config                  │
│                    Nacos                                │
│              http://localhost:8848                      │
└──────────────────────┬──────────────────────────────────┘
                       │
┌──────────────────────▼──────────────────────────────────┐
│                    Data Layer                           │
│                      MySQL                              │
│              (Multiple Databases)                       │
│  - gulimall_pms (Product)                               │
│  - gulimall_oms (Order)                                 │
│  - gulimall_ums (Member)                                │
│  - gulimall_wms (Warehouse)                             │
│  - gulimall_sms (System)                                 │
└─────────────────────────────────────────────────────────┘
```

## Microservices Architecture

### Service Decomposition

The system is decomposed into the following microservices:

#### 1. API Gateway Service (`gulimall-gateway`)
- **Port**: 88
- **Technology**: Spring Cloud Gateway
- **Responsibilities**:
  - Request routing
  - Load balancing
  - Cross-cutting concerns (logging, monitoring)
  - API aggregation

#### 2. Product Service (`gulimall-product`)
- **Port**: 10000
- **Database**: `gulimall_pms`
- **Responsibilities**:
  - Product catalog management
  - Category management
  - Brand management
  - SPU/SKU management
  - Attribute management

#### 3. Order Service (`gulimall-order`)
- **Port**: 9000
- **Database**: `gulimall_oms`
- **Responsibilities**:
  - Order creation and management
  - Order status tracking
  - Return order processing
  - Payment flow management
  - Refund flow management

#### 4. Member Service (`gulimall-member`)
- **Port**: 8000
- **Database**: `gulimall_ums`
- **Responsibilities**:
  - Member registration and management
  - Member level management
  - Member address management
  - Member statistics

#### 5. Coupon Service (`gulimall-coupon`)
- **Port**: 7000
- **Database**: `gulimall_sms`
- **Responsibilities**:
  - Coupon management
  - Flash sale activities
  - Full reduction discounts
  - Member pricing
  - Points maintenance
  - Homepage recommendations

#### 6. Warehouse Service (`gulimall-ware`)
- **Port**: 11000
- **Database**: `gulimall_wms`
- **Responsibilities**:
  - Warehouse management
  - Inventory management
  - Purchase order management
  - Inventory work orders

#### 7. Third-party Service (`gulimall-third-party`)
- **Port**: 30000
- **Responsibilities**:
  - File upload (OSS)
  - SMS service integration
  - Other third-party service integrations

## Technology Stack

### Backend Framework
- **Spring Boot 2.7.x**: Application framework
- **Spring Cloud 2021.x**: Microservices framework
- **Spring Cloud Alibaba**: Alibaba cloud components

### Service Discovery & Configuration
- **Nacos 2.0+**: Service registry and configuration center

### Data Access
- **MyBatis-Plus 3.5.x**: ORM framework
- **MySQL 5.7+**: Relational database

### API Gateway
- **Spring Cloud Gateway**: API gateway and routing

### Frontend
- **Vue.js 2.x**: Frontend framework
- **Element UI 2.x**: UI component library
- **Axios**: HTTP client
- **Vue Router**: Route management
- **Vuex**: State management

## Design Patterns

### 1. Microservices Pattern
- Each service is independently deployable
- Services communicate via RESTful APIs
- Database per service pattern

### 2. API Gateway Pattern
- Single entry point for all client requests
- Handles cross-cutting concerns
- Route requests to appropriate services

### 3. Service Registry Pattern
- Nacos acts as service registry
- Services register themselves on startup
- Client discovers services through registry

### 4. Database per Service
- Each service has its own database
- Prevents tight coupling
- Allows independent scaling

### 5. Feign Client Pattern
- Declarative REST client
- Simplifies inter-service communication
- Built-in load balancing

## Data Flow

### Request Flow

1. **Client Request** → Frontend (Vue.js)
2. **API Call** → API Gateway (Port 88)
3. **Route** → Gateway routes to appropriate service
4. **Service Processing** → Microservice handles business logic
5. **Data Access** → Service queries its database
6. **Response** → Service returns data
7. **Gateway Aggregation** → Gateway aggregates responses (if needed)
8. **Client Response** → Frontend receives and displays data

### Inter-Service Communication

Services communicate via:
- **REST APIs**: Synchronous communication
- **Feign Client**: Declarative HTTP client
- **Service Discovery**: Via Nacos

Example: Order Service calls Product Service to get product details:
```
Order Service → Feign Client → Nacos (Service Discovery) → Product Service
```

## Security Architecture

### Current Implementation
- Basic authentication mechanism
- Token-based authentication (planned)
- CORS configuration for frontend

### Security Considerations
- Sensitive information stored in configuration files (not in code)
- Environment variables for production
- Configuration center for secure config management

## Scalability

### Horizontal Scaling
- Each service can be scaled independently
- Stateless services allow easy scaling
- Load balancing via Gateway and Nacos

### Database Scaling
- Database per service allows independent scaling
- Read replicas can be added per service
- Connection pooling for efficient resource usage

## Deployment Architecture

### Development Environment
```
Local Machine:
├── MySQL (localhost:3306)
├── Nacos (localhost:8848)
├── Gateway (localhost:88)
├── Microservices (various ports)
└── Frontend (localhost:8001)
```

### Production Recommendations
- Containerized deployment (Docker)
- Orchestration (Kubernetes)
- Load balancer for Gateway
- Database clusters
- Monitoring and logging solutions

## Monitoring & Observability

### Current State
- Basic logging via Spring Boot
- Actuator endpoints for health checks

### Recommended Additions
- Distributed tracing (Zipkin/Jaeger)
- Metrics collection (Prometheus)
- Log aggregation (ELK Stack)
- APM tools (New Relic, Datadog)

## Error Handling

### Global Exception Handling
- `@ControllerAdvice` for centralized exception handling
- Standardized error response format
- Error code system

### Service Resilience
- Circuit breaker pattern (planned with Sentinel)
- Retry mechanisms
- Fallback strategies

## API Design Principles

1. **RESTful Design**: Follow REST principles
2. **Standardized Responses**: Consistent response format
3. **Versioning**: API versioning strategy (planned)
4. **Documentation**: API documentation (this document)
5. **Error Handling**: Clear error messages

## Future Enhancements

### Planned Features
1. **Message Queue**: RabbitMQ for asynchronous processing
2. **Cache Layer**: Redis for performance optimization
3. **Search Service**: Elasticsearch for product search
4. **Authentication Service**: OAuth2/JWT implementation
5. **Cart Service**: Shopping cart functionality
6. **Seckill Service**: Flash sale functionality

### Architecture Improvements
1. **Event-Driven Architecture**: Event sourcing and CQRS
2. **Service Mesh**: Istio for advanced traffic management
3. **API Versioning**: Version management strategy
4. **GraphQL**: Alternative API approach
5. **gRPC**: High-performance inter-service communication

## Development Guidelines

### Code Organization
```
Service Structure:
├── controller/     # REST controllers
├── service/         # Business logic
├── dao/            # Data access objects
├── entity/         # Domain entities
├── vo/             # Value objects
├── config/         # Configuration classes
└── exception/      # Exception handling
```

### Naming Conventions
- Controllers: `*Controller`
- Services: `*Service`, `*ServiceImpl`
- DAOs: `*Dao`
- Entities: `*Entity`
- VOs: `*Vo`

### Best Practices
1. **Separation of Concerns**: Clear layer separation
2. **Dependency Injection**: Use Spring DI
3. **Exception Handling**: Centralized exception handling
4. **Validation**: Input validation at controller level
5. **Logging**: Appropriate logging levels
6. **Documentation**: Code comments and API docs

## Conclusion

This architecture provides a solid foundation for an e-commerce backend system. The microservices approach allows for independent development, deployment, and scaling of each service. The use of modern frameworks and patterns ensures maintainability and extensibility.

For questions or suggestions, please refer to the project documentation or contact the development team.
