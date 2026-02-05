# API Gateway

Centralna brama API - routing, walidacja JWT, CORS.

## Port: 8080

## Technologie

- Spring Cloud Gateway (WebFlux)
- JWT walidacja (RS256, klucz publiczny)
- gRPC Client (User Service - logowanie/rejestracja)
- Eureka Client (dynamiczne odkrywanie serwisow)

## Routing

```
/api/v1/auth/**         → gRPC → User Service (logowanie, rejestracja, refresh, logout)
/api/v1/cart/**         → lb://CART-SERVICE
/api/v1/orders/**       → lb://ORDER-SERVICE
/api/v1/products/**     → lb://PRODUCT-SERVICE
/api/v1/categories/**   → lb://PRODUCT-SERVICE
/api/auth/**            → lb://USER-SERVICE (StripPrefix=1)
/api/addresses/**       → lb://USER-SERVICE
/api/notifications/**   → lb://NOTIFICATION-SERVICE
/api/payments/**        → lb://PAYMENT-SERVICE
```

## JWT Filter

Dla kazdego zabezpieczonego endpointu:
1. Odczytuje token z headera `Authorization: Bearer <token>`
2. Weryfikuje podpis RS256 kluczem publicznym
3. Sprawdza wygasniecie
4. Dodaje headery do request:
   - `X-User-ID` (subject z JWT)
   - `X-User-Email` (claim email)
   - `X-User-Role` (claim role)
5. Przekazuje request do docelowego serwisu

## Publiczne sciezki (bez JWT)

```
/api/v1/auth/**
/api/auth/login, /register, /logout, /refresh, /forgot-password, /reset-password, ...
/api/v1/products/**, /api/v1/categories/**
/actuator/**
```

## Sciezki admin

```
/api/admin/** → wymaga roli ADMIN w JWT
```
