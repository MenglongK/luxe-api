# LUXE API — E-Commerce Backend Service

Multi-module **Spring Boot** REST API for the **LUXE** e-commerce platform, built with **Java 25**, **Gradle**, **PostgreSQL**, **Redis**, **MinIO**, and **Keycloak**.

---

## Tech Stack

- **Language & Build**: Java `25`, Gradle (multi-module)
- **Framework**: Spring Boot `4.1.1` (Web MVC, Data JPA, Validation, Security, OAuth2 Client & Resource Server, Actuator)
- **Database**: PostgreSQL `17` (`org.postgresql:postgresql`)
- **Object Storage**: MinIO (`io.minio:minio:8.5.17`)
- **Caching**: Redis `7`
- **Identity & Auth**: Keycloak `26.1` (OIDC / JWT)
- **API Documentation**: Springdoc OpenAPI `2.5.0` (Swagger UI)
- **Mapping & Utilities**: MapStruct `1.6.3`, Lombok, `dotenv-java 3.0.0`
- **Containerization**: Multi-stage `Dockerfile` (`eclipse-temurin:25`) & Docker Compose (`docker-config/`)

---

## Module Architecture

```text
luxe-api/
├── application/        # Main Spring Boot app (Application.java), application.yaml, TestDataSeeder
├── core-feature/       # Domain entities, repositories, MapStruct mappers, services, and /api/v1 controllers
├── security/           # Spring Security filter chain & OAuth2 configuration
├── payment/            # Payment module
└── docker-config/      # Docker Compose definitions for PostgreSQL, Keycloak, MinIO, and Redis
```

### Core Feature Domains (`core-feature`)
- `brand`, `category`, `product`, `product_variant`, `review`
- `cart`, `wishlist`, `order`, `coupon`, `wallet`, `customerpoint`
- `facility`, `inventory` (`facility_inventory`), `supplier`, `user` (`customer`)
- `file` (MinIO file upload & retrieval)

---

## Running Locally

### 1. Start Docker Dependencies
Configure `docker-config/.env` and start PostgreSQL, Keycloak, MinIO, and Redis:

```bash
cd docker-config
docker compose up -d
```

### 2. Run the Application
```bash
./gradlew :application:bootRun
```

### 3. Run Tests & Build Jar
```bash
./gradlew test
./gradlew :application:bootJar
```

### API Documentation
Once running on port `8080`:
- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
