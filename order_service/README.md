# Order Service

Serwis zamowien - orkiestruje caly flow od checkout do finalizacji.

## Port: 8085

## Technologie

- Spring Data JPA + PostgreSQL (schemat: `orders`)
- Flyway (migracje)
- Kafka Producer/Consumer (glowny orkiestrator eventow)
- Feign Client (pobieranie adresu z User Service)
- Java 21 Virtual Threads
- NanoID (generowanie numerow zamowien: ELO-XXXXXX)

## Schemat bazy: `orders`

```
orders
├── uuid (PK)
├── user_id (VARCHAR)
├── order_number (UNIQUE, np. "ELO-A1B2C3")
├── email
├── order_date
├── total_amount
├── order_status (PENDING / PAID / PAYMENT_FAILED)
├── payment_id
├── AddressSnapshot (embedded: street, city, state, country, zip, original_address_id)
├── created_at
└── updated_at

order_items
├── uuid (PK)
├── order_uuid (FK → orders)
├── product_number
├── product_name
├── quantity
├── ordered_product_price
├── created_at
└── updated_at
```

## Flow

```
1. Kafka ← cart-checkout-topic (CartCheckoutEvent)
   → Feign → User Service: pobierz adres (/api/addresses/internal/{id})
   → Utworz Order (status: PENDING)
   → Kafka → order-created-topic (OrderCreatedEvent → Payment Service)

2. Kafka ← payment-succeeded-topic (PaymentSucceededEvent)
   → Zmien status na PAID
   → Kafka → order-placed-topic (OrderPlacedEvent → Inventory + Cart)

3. Kafka ← payment-failed-topic (PaymentFailedEvent)
   → Zmien status na PAYMENT_FAILED
   → Kafka → order-failed-topic (OrderFailedEvent → Inventory zwalnia rezerwacje)
```

## Kafka Events

| Topic | Rola | Event |
|-------|------|-------|
| `cart-checkout-topic` | Consumer | CartCheckoutEvent → tworzy zamowienie |
| `order-created-topic` | Producer | OrderCreatedEvent → Payment Service |
| `payment-succeeded-topic` | Consumer | PaymentSucceededEvent → finalizuj |
| `payment-failed-topic` | Consumer | PaymentFailedEvent → anuluj |
| `order-placed-topic` | Producer | OrderPlacedEvent → Inventory + Cart |
| `order-failed-topic` | Producer | OrderFailedEvent → Inventory |

## Brak endpointow REST

Order Service nie eksponuje endpointow REST. Cala komunikacja odbywa sie przez Kafka i Feign.
