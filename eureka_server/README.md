# Eureka Server

Service Discovery - rejestr serwisow.

## Port: 8761

## Dashboard

```
http://localhost:8761
```

Pokazuje wszystkie zarejestrowane serwisy, ich instancje i status.

## Zarejestrowane serwisy

- config-server
- api-gateway
- user-service
- product-service
- cart-service
- order-service
- payment-service
- inventory-service
- notification-service

## Konfiguracja klienta

Kazdy serwis rejestruje sie automatycznie:
```yaml
eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka/
  instance:
    prefer-ip-address: true
```
