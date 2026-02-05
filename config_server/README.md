# Config Server

Centralny serwer konfiguracji (Spring Cloud Config).

## Port: 8888

## Tryb: Native (plikowy)

Konfiguracje sa przechowywane lokalnie w `src/main/resources/configurations/`.

## Pliki konfiguracyjne

```
configurations/
├── application.yml           - Wspolna konfiguracja (Eureka, logging, actuator)
├── api-gateway.yml           - Routing, JWT, CORS, gRPC
├── user-service.yml          - DB, Redis, JWT, gRPC, Kafka
├── product-service.yml       - DB, gRPC, Kafka
├── cart-service.yml          - Redis, gRPC clients, Kafka
├── order-service.yml         - DB, Feign, Kafka, Virtual Threads
├── payment-service.yml       - DB, Stripe, Kafka
├── inventory-service.yml     - DB, Redis, gRPC, Kafka
├── notification-service.yml  - Mail, Kafka
└── ai-service.yml            - Ollama, DB (read-only)
```

## Jak serwisy pobieraja konfiguracje

Kazdy serwis ma w `application.yml`:
```yaml
spring:
  config:
    import: optional:configserver:${CONFIG_SERVER_URL:http://localhost:8888}
```

Config Server musi byc uruchomiony **jako pierwszy**.

## Health check

```
GET http://localhost:8888/actuator/health
GET http://localhost:8888/user-service/default    - Konfiguracja user-service
```
