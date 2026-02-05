# User Service

Serwis uzytkownikow - autoryzacja, uwierzytelnianie, adresy, administracja.

## Port: 8081 (REST) / 9091 (gRPC)

## Technologie

- Spring Security + JWT (RS256, klucze RSA)
- Spring Data JPA + PostgreSQL (schemat: `users`)
- Spring Data Redis (rate limiting, sesje)
- gRPC Server (AuthGrpcService dla API Gateway)
- Kafka Producer (eventy rejestracji, resetu hasla)
- Flyway (migracje)
- MapStruct (mapowanie DTO)
- Thymeleaf (szablony email)

## Schemat bazy: `users`

```
users
├── uuid (PK)
├── email (UNIQUE)
├── password (BCrypt)
├── user_role (ADMIN / SELLER / USER)
├── email_verified
├── created_at, updated_at

addresses
├── uuid (PK)
├── user_id (FK → users)
├── street, building_name, city, state, country, pin_code
├── created_at, updated_at
```

## Endpointy REST

### Autoryzacja

```
POST /auth/login                              - Logowanie
POST /auth/register                           - Rejestracja
GET  /auth/refresh                            - Odswiezenie tokenu (cookie)
POST /auth/logout                             - Wylogowanie
GET  /auth/me                                 - Dane zalogowanego uzytkownika
```

### Hasla

```
POST /auth/forgot-password                    - Wyslij mail reset hasla
POST /auth/reset-password                     - Resetuj haslo
POST /auth/change-password                    - Zmien haslo (wymaga JWT)
```

### Weryfikacja email

```
POST /auth/verify-email/{token}               - Zweryfikuj email
POST /auth/resend-verification-email          - Ponownie wyslij mail
GET  /auth/email-verification-status          - Status weryfikacji
GET  /auth/validate-password-token/{token}    - Waliduj token resetu
GET  /auth/validate-verification-email-token/{token} - Waliduj token weryfikacji
```

### Adresy (wymaga JWT)

```
GET    /api/addresses                         - Lista adresow uzytkownika
GET    /api/addresses/user/{addressId}        - Konkretny adres
POST   /api/addresses                         - Dodaj adres
PUT    /api/addresses/{addressId}             - Edytuj adres
DELETE /api/addresses/{addressId}             - Usun adres
GET    /api/addresses/internal/{addressId}    - Wewnetrzny (Feign, bez auth)
```

### Admin (wymaga roli ADMIN)

```
PUT /admin/users/role                         - Zmien role uzytkownika
```

## gRPC (AuthGrpcService)

W pelni zaimplementowany - uzywany przez API Gateway:

```protobuf
rpc Login(LoginRequest) returns (AuthResponse)
rpc Register(RegisterRequest) returns (AuthResponse)
rpc RefreshToken(RefreshTokenRequest) returns (AuthResponse)
rpc ValidateToken(ValidateTokenRequest) returns (ValidateTokenResponse)
rpc Logout(LogoutRequest) returns (LogoutResponse)
```

## Komunikacja

```
API Gateway ──gRPC──► User Service           (login, register, refresh, validate, logout)
Order Service ──Feign──► User Service        (pobieranie adresu: /api/addresses/internal/{id})
User Service ──Kafka──► Notification Service (user-registration, password-reset)
```

## Kafka Events

| Topic | Rola | Event |
|-------|------|-------|
| `user-registration` | Producer | UserRegistrationEvent → mail weryfikacyjny |
| `password-reset` | Producer | PasswordResetEvent → mail reset hasla |

## Bezpieczenstwo

- **JWT RS256** - asymetryczne klucze RSA (`certs/private.pem`, `certs/public.pem`)
- **Access Token** - 1 godzina
- **Refresh Token** - 7 dni, HTTP-only cookie
- **BCrypt** - hashowanie hasel
- **HMAC-SHA256** - tokeny resetu hasla (15 min) i weryfikacji email (24h)
- **Rate limiting** - Redis, 5 req/min na reset hasla
