# Payment Service

Serwis platnosci zintegrowany ze Stripe.

## Port: 8086

## Technologie

- Stripe Java SDK (PaymentIntent API + Webhook)
- Spring Data JPA + PostgreSQL (schemat: `payments`)
- Flyway (migracje)
- Kafka Producer/Consumer

## Schemat bazy: `payments`

```
payment
├── uuid (PK)
├── order_id
├── user_id
├── amount
├── currency
├── payment_method
├── client_secret (Stripe)
├── pg_payment_id (Stripe PaymentIntent ID)
├── pg_status (PENDING / SUCCEEDED / FAILED)
├── pg_response_message
├── pg_name ("STRIPE")
├── created_at
└── updated_at
```

## Flow platnosci

```
1. Kafka ← order-created-topic (OrderCreatedEvent)
   → Stripe API: PaymentIntent.create(amount, currency, metadata)
   → Zapisz Payment w DB (status: PENDING, clientSecret)

2. Frontend pobiera clientSecret:
   GET /api/payments/order/{orderId}
   → { clientSecret, stripePublicKey, amount, currency, status }

3. Frontend potwierdza platnosc w Stripe (stripe.js)

4. Stripe Webhook → POST /api/payments/webhook
   → Weryfikacja podpisu (Stripe-Signature)
   → Aktualizacja statusu na SUCCEEDED
   → Kafka → payment-succeeded-topic
```

## Endpointy REST

```
GET  /api/payments/order/{orderId}    - Dane do platnosci (clientSecret, publicKey)
POST /api/payments/webhook            - Stripe webhook (payment_intent.succeeded)
```

## Kafka Events

| Topic | Rola | Event |
|-------|------|-------|
| `order-created-topic` | Consumer | OrderCreatedEvent → tworzenie PaymentIntent |
| `payment-succeeded-topic` | Producer | PaymentSucceededEvent → Order Service |
| `payment-failed-topic` | Producer | PaymentFailedEvent → Order Service |

## Zmienne srodowiskowe

```
STRIPE_API_KEY=sk_test_...          # Klucz API Stripe (secret)
STRIPE_PUBLISH_KEY=pk_test_...      # Klucz publiczny Stripe
SECRET_HOOK=whsec_...               # Webhook signing secret
```
