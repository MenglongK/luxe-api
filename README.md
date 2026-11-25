# LUXE API

Multi-module Spring Boot backend for the **LUXE** luxury minimalist fashion e-commerce platform.

## Overview

Handles the core e-commerce business logic across catalog, shopping, loyalty, inventory, and media storage:
- **Catalog & Reviews**: Products, variants, categories, brands, and customer reviews
- **Shopping & Loyalty**: Cart, wishlist, orders, coupons, customer points, and digital wallets
- **Operations & Media**: Facilities, inventory, suppliers, customers, and file/image storage

## Tech Stack

- **Java 25** & **Spring Boot 4** (Gradle multi-module)
- **PostgreSQL 17** & **Spring Data JPA**
- **Redis 7**
- **MinIO** (object storage)
- **Keycloak 26** & **Spring Security**
- **MapStruct** & **Lombok**
- **Docker & Docker Compose**

## Project Structure

```text
luxe-api/
├── application/        # Spring Boot entry point & application setup
├── core-feature/       # Core e-commerce domains & business logic
├── security/           # Spring Security & OAuth2 configuration
├── payment/            # Payment module
├── docker-config/      # Docker Compose services (Postgres, Redis, MinIO, Keycloak)
└── Dockerfile          # Multi-stage container build
```

## Getting Started

1. Start Docker services:
   ```bash
   cd docker-config
   docker compose up -d
   ```

2. Run the backend:
   ```bash
   ./gradlew :application:bootRun
   ```
