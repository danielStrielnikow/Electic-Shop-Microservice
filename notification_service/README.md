# Notification Service

Serwis powiadomien email.

## Port: 8082

## Technologie

- Spring Mail (SMTP)
- Spring Cloud Stream + Kafka (konsument eventow)
- Thymeleaf (szablony HTML email)

## Kafka Events (konsument)

| Topic | Event | Akcja |
|-------|-------|-------|
| `user-registration` | UserRegistrationEvent | Wysyla mail weryfikacyjny |
| `password-reset` | PasswordResetEvent | Wysyla mail z linkiem resetu |

## Szablony email

Lokalizacja: `src/main/resources/templates/`

## Zmienne srodowiskowe

```
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=email@gmail.com
MAIL_PASSWORD=app-password
MAIL_FROM=noreply@electric-shop.com.pl
```

## Brak endpointow REST

Cala komunikacja odbywa sie przez Kafka.
