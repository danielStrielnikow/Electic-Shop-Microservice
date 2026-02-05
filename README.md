# Electric Shop - Microservices E-Commerce Platform

Platforma e-commerce oparta na architekturze mikroserwisowej, zbudowana w **Java 21** ze **Spring Boot** i **Spring Cloud**.

---

## Architektura

```
                                    [Frontend]
                                          │
                                          ▼
                                  ┌───────────────┐
                                  │  API Gateway   │ :8080
                                  │  (JWT Filter)  │
                                  └───────┬────────┘
                                          │
                    ┌─────────────────────┴-──────────────────────┐
                    │                     │                       │
              ┌─────▼──────┐    ┌────────▼────────┐    ┌────────▼────────┐
              │ User Service│    │ Product Service  │    │  Cart Service   │
              │    :8081    │    │     :8083        │    │     :8084       │
              │  gRPC:9091  │    │   gRPC:9090      │    │                 │
              └──────┬──────┘    └────────┬─────────┘    └───┬──────┬─────┘
                     │                    │                  │      │
                     │              ┌─────▼──────────┐       │      │
                     │              │   Inventory     │◄──gRPC┘     │
                     │              │   Service :8087 │             │
                     │              │   gRPC:9093     │             │
                     │              └─────┬──────────┘              │
                     │                    │                         │
                     │              ┌─────▼──────┐         Kafka    │
                     │              │   Kafka    │◄─────────────────┘
                     │              │  :9092     │    cart-checkout-topic
                     │              └──┬───┬──┬──┘
                     │                 │   │  │
              ┌──────▼──────┐   ┌─────▼┐   │  │ ┌─────────────────┐
              │ Notification │   │Order │  │  │ │ Payment Service │
              │ Service:8082 │   │Serv. │  │  └──►│     :8086     │
              └──────────────┘   │:8085 │◄───── │   (Stripe)      │
                                 └──────┘       └─────────────────┘
```

---

## Serwisy

| Serwis | Port | gRPC | Opis |
|--------|------|------|------|
| **Config Server** | 8888 | - | Centralna konfiguracja (Spring Cloud Config) |
| **Eureka Server** | 8761 | - | Service Discovery |
| **API Gateway** | 8080 | - | Routing, walidacja JWT, CORS |
| **User Service** | 8081 | 9091 | Autoryzacja, uzytkownicy, adresy |
| **Product Service** | 8083 | 9090 | Produkty, kategorie |
| **Cart Service** | 8084 | - | Koszyk (Redis) |
| **Order Service** | 8085 | - | Zamowienia, orkiestracja flow |
| **Payment Service** | 8086 | - | Platnosci (Stripe) |
| **Inventory Service** | 8087 | 9093 | Stany magazynowe, rezerwacje |
| **Notification Service** | 8082 | - | Maile (weryfikacja, reset hasla) |
| **AI Service** | 8088 | - | Rekomendacje produktow (Ollama) |

---

## Komunikacja miedzy serwisami

### gRPC (synchroniczna, szybka)

```
API Gateway ──gRPC──► User Service       (logowanie, rejestracja, walidacja tokenu)
Cart Service ──gRPC──► Product Service    (pobieranie danych produktu do koszyka)
Cart Service ──gRPC──► Inventory Service  (rezerwacje, dostepnosc, anulowanie)
```

### Kafka (asynchroniczna, event-driven)

```
Topiki i przeplywy:

user-registration         User Service ───► Notification Service (mail weryfikacyjny)
password-reset            User Service ───► Notification Service (mail reset hasla)

product-add-topic         Product Service ──► Inventory Service  (nowy produkt)
product-update-topic      Product Service ──► Inventory Service  (aktualizacja)

cart-checkout-topic       Cart Service ─────► Order Service      (checkout koszyka)
order-created-topic       Order Service ────► Payment Service    (nowe zamowienie → plac)
payment-succeeded-topic   Payment Service ──► Order Service      (platnosc OK)
payment-failed-topic      Payment Service ──► Order Service      (platnosc FAIL)
order-placed-topic        Order Service ────► Inventory Service  (potwierdz zamowienie)
                          Order Service ────► Cart Service       (wyczysc koszyk)
order-failed-topic        Order Service ────► Inventory Service  (zwolnij rezerwacje)
```

### Feign (REST, synchroniczna)

```
Order Service ──Feign──► User Service     (pobieranie adresu dostawy)
```

### Headery HTTP (API Gateway → serwisy)

```
X-User-ID    : UUID uzytkownika (z JWT)
X-User-Email : Email uzytkownika (z JWT)
X-User-Role  : Rola uzytkownika (z JWT)
```

---

## Flow zamowienia

```
1. Uzytkownik dodaje produkt do koszyka
   Cart Service ──gRPC──► Inventory Service (rezerwacja produktu)

2. Uzytkownik klika "Zamow"
   Cart Service ──Kafka──► Order Service (CartCheckoutEvent)

3. Order Service tworzy zamowienie
   Order Service ──Feign──► User Service (pobranie adresu)
   Order Service ──Kafka──► Payment Service (OrderCreatedEvent)

4a. Platnosc udana:
   Stripe Webhook ──► Payment Service ──Kafka──► Order Service (PaymentSucceededEvent)
   Order Service ──Kafka──► Inventory Service (OrderPlacedEvent - potwierdz)
   Order Service ──Kafka──► Cart Service (OrderPlacedEvent - wyczysc koszyk)

4b. Platnosc nieudana:
   Order Service ──Kafka──► Inventory Service (OrderFailedEvent - zwolnij rezerwacje)
```

---

## Technologie

| Kategoria | Technologia |
|-----------|-------------|
| **Jezyk** | Java 21 (Virtual Threads) |
| **Framework** | Spring Boot 3.4/3.5, Spring Cloud 2024/2025 |
| **Baza danych** | PostgreSQL 16 (wspolna baza, oddzielne schematy) |
| **Cache** | Redis 7 |
| **Message broker** | Apache Kafka 7.6 |
| **Service Discovery** | Netflix Eureka |
| **Konfiguracja** | Spring Cloud Config (native) |
| **Komunikacja** | gRPC (Protobuf), REST, Kafka |
| **Platnosci** | Stripe (PaymentIntent + Webhook) |
| **Bezpieczenstwo** | JWT (RS256), Spring Security |
| **Migracje** | Flyway |
| **Mapowanie** | MapStruct |
| **AI** | Spring AI + Ollama (deepseek-r1:8b) |
| **Szablony email** | Thymeleaf |
| **Build** | Gradle 8.5 |

---

## Baza danych - schematy

Wspolna baza PostgreSQL (`electric_shop_db`) z izolacja schematow:

| Schemat | Serwis | Tabele |
|---------|--------|--------|
| `users` | User Service | users, addresses |
| `products` | Product Service | products, categories, product_categories |
| `orders` | Order Service | orders, order_items |
| `payments` | Payment Service | payment |
| `inventory` | Inventory Service | inventory |

---

## Uruchamianie

### Wymagania

- **Java 21** (JDK)
- **Docker** i **Docker Compose**
- **Gradle 8.5** (wrapper dolaczony)

### 1. Infrastruktura (Docker)

```bash
# Uruchom PostgreSQL, Redis, Kafka, Zookeeper
docker-compose up -d
```

### 2. Konfiguracja srodowiska

Plik `.env` w glownym katalogu zawiera zmienne srodowiskowe.

Dla uruchamiania **lokalnego** (bez Dockera dla serwisow):
```env
POSTGRES_HOST=localhost
POSTGRES_PORT=5433
REDIS_HOST=localhost
REDIS_PORT=6379
KAFKA_BOOTSTRAP_SERVERS=localhost:9092
```

Dla uruchamiania **w Dockerze** (pelny docker-compose):
```env
POSTGRES_HOST=postgres
POSTGRES_PORT=5432
REDIS_HOST=redis
KAFKA_BOOTSTRAP_SERVERS=kafka:29092
```

### 3. Kolejnosc uruchamiania serwisow

```bash
# 1. Config Server (musi byc pierwszy - reszta pobiera z niego konfiguracje)
./gradlew :config_server:bootRun

# 2. Eureka Server (service discovery)
./gradlew :eureka_server:bootRun

# 3. Serwisy biznesowe (dowolna kolejnosc)
./gradlew :user_service:bootRun
./gradlew :product_service:bootRun
./gradlew :inventory_service:bootRun
./gradlew :cart_service:bootRun
./gradlew :order_service:bootRun
./gradlew :payment_service:bootRun
./gradlew :notification_service:bootRun

# 4. API Gateway (na koncu - potrzebuje zarejestrowanych serwisow)
./gradlew :api_gateway:bootRun

# 5. Opcjonalnie - AI Service (wymaga Ollama)
./gradlew :ai_service:bootRun
```

### 4. Weryfikacja

- **Eureka Dashboard:** http://localhost:8761
- **Config Server Health:** http://localhost:8888/actuator/health
- **API Gateway:** http://localhost:8080

---

## API Endpoints

### Autoryzacja (przez API Gateway :8080)

```
POST /api/v1/auth/login       - Logowanie
POST /api/v1/auth/register    - Rejestracja
POST /api/v1/auth/refresh     - Odswiezenie tokenu
POST /api/v1/auth/logout      - Wylogowanie
```

### Produkty (publiczne)

```
GET  /api/v1/products                          - Lista produktow
GET  /api/v1/products?keyword=laptop           - Wyszukiwanie
GET  /api/v1/categories                        - Lista kategorii
```

### Koszyk (wymaga JWT)

```
GET    /api/v1/cart                            - Pobierz koszyk
POST   /api/v1/cart/items                      - Dodaj produkt
PUT    /api/v1/cart/items                      - Zmien ilosc
DELETE /api/v1/cart/items/{productNumber}       - Usun produkt
POST   /api/v1/cart/checkout                   - Zloz zamowienie
```

### Adresy (wymaga JWT)

```
GET    /api/addresses                          - Lista adresow
POST   /api/addresses                          - Dodaj adres
PUT    /api/addresses/{id}                     - Edytuj adres
DELETE /api/addresses/{id}                     - Usun adres
```

### Platnosci

```
GET    /api/payments/order/{orderId}           - Dane do platnosci (clientSecret)
POST   /api/payments/webhook                   - Stripe webhook
```

### Admin (wymaga roli ADMIN)

```
POST   /api/v1/admin/products/{categoryNumber} - Dodaj produkt
PUT    /api/v1/admin/products/{productName}    - Edytuj produkt
DELETE /api/v1/admin/products/{productName}    - Usun produkt
PUT    /admin/users/role                       - Zmien role uzytkownika
```

---

## Struktura projektu

```
electric_shop/
├── common-events/          # Biblioteka wspoldzielona (proto, eventy, BaseEntity)
├── config_server/          # Centralna konfiguracja
├── eureka_server/          # Service Discovery
├── api_gateway/            # Brama API (JWT, routing)
├── user_service/           # Uzytkownicy, autoryzacja, adresy
├── product_service/        # Produkty, kategorie
├── cart_service/           # Koszyk (Redis)
├── order_service/          # Zamowienia
├── payment_service/        # Platnosci (Stripe)
├── inventory_service/      # Magazyn, rezerwacje
├── notification_service/   # Maile
├── ai_service/             # Rekomendacje AI
├── docker-compose.yml      # Infrastruktura Docker
├── .env                    # Zmienne srodowiskowe
└── settings.gradle         # Moduly Gradle
```
