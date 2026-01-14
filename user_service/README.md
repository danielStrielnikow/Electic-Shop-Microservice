# User Service

User Service is a microservice responsible for user authentication, authorization, and account management in the Electric Shop e-commerce platform. It provides secure user registration, login, password management, and email verification functionality.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Technology Stack](#technology-stack)
- [Architecture](#architecture)
- [Getting Started](#getting-started)
- [API Documentation](#api-documentation)
- [Environment Variables](#environment-variables)
- [Database Schema](#database-schema)
- [Security](#security)
- [Future Enhancements](#future-enhancements)

## Overview

This service is part of a microservices-based e-commerce platform. It handles all user-related operations including authentication using JWT tokens, role-based access control (RBAC), and secure password management with email verification.

## Features

### Authentication & Authorization
- User registration with email validation
- User login with JWT access tokens
- Refresh token mechanism (HTTP-only cookies, 7-day expiration)
- Role-based access control (ADMIN, SELLER, USER)
- Secure logout functionality

### Password Management
- Password strength validation (min 8 chars, uppercase, lowercase, digit, special character)
- Forgot password with email token
- Password reset via secure HMAC-signed tokens
- Change password for authenticated users

### Email Verification
- Email verification with token-based system
- Resend verification email functionality
- Email verification status check

### Security Features
- BCrypt password hashing
- JWT-based authentication
- HMAC-SHA256 token signing
- HTTP-only secure cookies for refresh tokens
- Rate limiting via Redis (5 requests/minute for password reset)
- CSRF protection for stateless API

### Address Management
- Multiple addresses per user
- Support for 56 countries (primarily European)
- Address CRUD operations

## Technology Stack

### Core Framework
- **Java 17** - Programming language
- **Spring Boot 3.5.7** - Application framework
- **Spring Security** - Authentication & authorization
- **Spring Data JPA** - Database persistence
- **Spring Data Redis** - Caching and rate limiting
- **Spring Web** - REST API

### Database & Migrations
- **PostgreSQL 16** - Primary database
- **Flyway 9.22.3** - Database migration management
- **Redis 7** - Caching and session storage

### Security & Authentication
- **JWT (io.jsonwebtoken)** - Token generation and validation
- **BCrypt** - Password hashing
- **HMAC-SHA256** - Secure token signing

### Additional Libraries
- **Lombok** - Reduce boilerplate code
- **MapStruct 1.5.5** - DTO-Entity mapping
- **Thymeleaf** - Email template rendering
- **Spring Mail** - Email notifications

### Development Tools
- **Gradle 8.x** - Build tool
- **Docker & Docker Compose** - Containerization
- **gRPC** - Inter-service communication (infrastructure ready)

## Architecture

### Layered Architecture
```
API Layer (Controllers)
    ↓
Service Layer (Business Logic)
    ↓
Repository Layer (Data Access)
    ↓
Database (PostgreSQL)
```

### Package Structure
```
pl.electricshop.user_service/
├── api/                        # REST Controllers & DTOs
│   ├── AuthController          # Authentication endpoints
│   ├── PasswordController      # Password management
│   ├── ValidationController    # Token validation
│   ├── request/               # Request DTOs (9 types)
│   └── response/              # Response DTOs (7 types)
├── base/                      # Base entities
│   └── BaseEntity             # Auditing base class
├── config/                    # Configuration classes
│   ├── SecurityConfig         # Spring Security setup
│   ├── JwtConfig              # JWT configuration
│   └── RedisConfig            # Redis connection
├── exception/                 # Custom exceptions & handlers
│   └── GlobalExceptionHandler # Centralized error handling
├── filter/                    # Request filters
│   ├── JwtAuthFilter          # JWT authentication
│   └── LoggingFilter          # Request logging
├── mapper/                    # DTO-Entity mapping
│   ├── UserMapper             # User mappings
│   └── AddressMapper          # Address mappings
├── model/                     # Domain entities
│   ├── User                   # User entity
│   ├── Address                # Address entity
│   └── enums/                # Enumerations
├── repository/                # Data access
│   └── UserRepository         # JPA repository
├── service/                   # Business logic
│   ├── AuthService           # Authentication
│   ├── PasswordService       # Password management
│   ├── JwtService            # JWT generation
│   ├── TokenService          # HMAC token generation
│   └── EmailService          # Email notifications
└── validator/                 # Validation logic
    └── PasswordValidator      # Password strength
```

### Design Patterns & Principles
- **Single Responsibility Principle (SRP)** - Each class has one responsibility
- **Dependency Inversion Principle (DIP)** - Depends on abstractions
- **DRY (Don't Repeat Yourself)** - Code reuse through service delegation
- **Repository Pattern** - Data access abstraction
- **DTO Pattern** - Data transfer between layers

## Getting Started

### Prerequisites
- Java 17 or higher
- Docker and Docker Compose
- Gradle 8.x (or use included Gradle Wrapper)

### Running with Docker

1. Clone the repository:
```bash
git clone <repository-url>
cd electric_shop
```

2. Create `.env` file in the root directory (use `.env.example` as template):
```bash
cp .env.example .env
```

3. Configure environment variables in `.env` file (see [Environment Variables](#environment-variables))

4. Start the infrastructure (PostgreSQL, Redis):
```bash
docker-compose up -d
```

5. Run the application:
```bash
cd user_service
./gradlew bootRun
```

The service will be available at `http://localhost:8080`

### Running Locally (without Docker)

1. Ensure PostgreSQL and Redis are running locally
2. Configure `application.yml` or set environment variables
3. Run database migrations:
```bash
./gradlew flywayMigrate
```

4. Start the application:
```bash
./gradlew bootRun
```

## API Documentation

### Base URL
```
http://localhost:8080
```

### Authentication Endpoints

#### Register User
```http
POST /auth/register
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!",
  "userRole": "USER"
}
```

#### Login
```http
POST /auth/login
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "SecurePass123!"
}

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### Refresh Token
```http
GET /auth/refresh
Cookie: refreshToken=<refresh_token>

Response:
{
  "accessToken": "eyJhbGciOiJIUzI1NiIs..."
}
```

#### Logout
```http
POST /auth/logout

Response:
{
  "success": true,
  "message": "Logout successful"
}
```

### Password Management Endpoints

#### Forgot Password
```http
POST /auth/forgot-password
Content-Type: application/json

{
  "email": "user@example.com"
}

Response:
{
  "success": true,
  "message": "Password reset email sent"
}
```

#### Reset Password
```http
POST /auth/reset-password
Content-Type: application/json

{
  "token": "base64-encoded-token",
  "newPassword": "NewSecurePass123!"
}

Response:
{
  "success": true,
  "message": "Password reset successfully"
}
```

#### Change Password (Authenticated)
```http
POST /auth/change-password
Authorization: Bearer <access_token>
Content-Type: application/json

{
  "oldPassword": "OldPassword123!",
  "newPassword": "NewPassword123!"
}

Response:
{
  "success": true,
  "message": "Password changed successfully"
}
```

### Email Verification Endpoints

#### Verify Email
```http
POST /auth/verify-email/{token}

Response: true/false
```

#### Resend Verification Email
```http
POST /auth/resend-verification-email
Content-Type: application/json

{
  "email": "user@example.com"
}

Response:
{
  "success": true,
  "message": "Verification email sent"
}
```

#### Check Email Verification Status
```http
GET /auth/email-verification-status?email=user@example.com

Response:
{
  "email": "user@example.com",
  "verified": true
}
```

### Validation Endpoints

#### Validate Password Reset Token
```http
GET /auth/validate-password-token/{token}

Response:
{
  "valid": true,
  "message": "Token is valid",
  "email": "user@example.com"
}
```

#### Validate Email Verification Token
```http
GET /auth/validate-verification-email-token/{token}

Response:
{
  "valid": true,
  "message": "Token is valid",
  "email": "user@example.com"
}
```

### Error Responses

All endpoints return consistent error responses:

```json
{
  "code": "ERROR_CODE",
  "message": "Human-readable error message"
}
```

Common error codes:
- `USER_ALREADY_EXISTS` (409 Conflict)
- `USER_NOT_FOUND` (404 Not Found)
- `EMAIL_NOT_VERIFIED` (403 Forbidden)
- `INVALID_CREDENTIALS` (401 Unauthorized)
- `INVALID_PASSWORD` (400 Bad Request)
- `REFRESH_TOKEN_EXPIRED` (401 Unauthorized)

## Environment Variables

### Database Configuration
```
POSTGRES_HOST=localhost
POSTGRES_PORT=5433
POSTGRES_DB=electric_shop_db
POSTGRES_USER=electric_user
POSTGRES_PASSWORD=electric_password
```

### Redis Configuration
```
REDIS_HOST=localhost
REDIS_PORT=6379
```

### JWT Configuration
```
JWT_SECRET=your-secret-key-min-256-bits
JWT_EXPIRATION=3600000              # 1 hour in milliseconds
JWT_REFRESH_EXPIRATION=604800000    # 7 days in milliseconds
```

### Security Configuration
```
HMAC_SECRET=your-hmac-secret-key
```

### Email Configuration
```
MAIL_FROM=noreply@electricshop.com
PASSWORD_RESET_SUBJECT=Password Reset Request
PASSWORD_RESET_EXPIRY_MINUTES=15
EMAIL_VERIFICATION_SUBJECT=Verify Your Email
EMAIL_VERIFICATION_EXPIRY_MINUTES=1440  # 24 hours
```

### Application Configuration
```
APP_BASE_URL=http://localhost:3000
```

## Database Schema

### Users Table
```sql
CREATE TABLE users (
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(320) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    user_role VARCHAR(50) NOT NULL,
    email_verified BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_users_email ON users(email);
```

### Addresses Table
```sql
CREATE TABLE addresses (
    uuid UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    street VARCHAR(255) NOT NULL,
    building_name VARCHAR(255) NOT NULL,
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100) NOT NULL,
    country VARCHAR(50) NOT NULL,
    pin_code VARCHAR(20) NOT NULL,
    user_id UUID NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(uuid) ON DELETE CASCADE
);

CREATE INDEX idx_addresses_user_id ON addresses(user_id);
```

### Migrations

Database migrations are managed by Flyway. Migration files are located in:
```
src/main/resources/db/migration/
├── V1__Create_users_table.sql
├── V2__Create_addresses_table.sql
└── V3__Create_indexes.sql
```

## Security

### Password Security
- Passwords are hashed using BCrypt with automatic salt generation
- Password strength validation enforces:
  - Minimum 8 characters
  - At least one uppercase letter
  - At least one lowercase letter
  - At least one digit
  - At least one special character

### Token Security
- **Access Tokens (JWT)**: Short-lived (1 hour), used for API authentication
- **Refresh Tokens (JWT)**: Long-lived (7 days), HTTP-only cookies, used to obtain new access tokens
- **Password Reset Tokens**: HMAC-SHA256 signed, time-limited (15 minutes)
- **Email Verification Tokens**: HMAC-SHA256 signed, time-limited (24 hours)

### API Security
- All endpoints except authentication are protected by JWT
- CSRF protection for stateless API
- Rate limiting on sensitive endpoints (password reset)
- HTTP-only cookies for refresh tokens prevent XSS attacks

### Role-Based Access Control
Three user roles are supported:
- **USER**: Standard user with basic permissions
- **SELLER**: Can manage products and orders
- **ADMIN**: Full system access


## License

This project is part of a portfolio and is available for educational purposes.

## Contact

For questions or feedback, please open an issue in the repository.
