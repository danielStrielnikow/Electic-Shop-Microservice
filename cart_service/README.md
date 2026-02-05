# Cart Service

Serwis koszyka zakupowego oparty na Redis.

## Port: 8084

## Technologie

- Redis (przechowywanie koszyka)
- gRPC Client (Product Service :9090, Inventory Service :9093)
- Kafka Producer/Consumer
- Java 21 Virtual Threads

## Komunikacja

```
Cart Service ──gRPC──► Product Service     (dane produktu do koszyka)
Cart Service ──gRPC──► Inventory Service   (rezerwacja, anulowanie, aktualizacja)
Cart Service ──Kafka──► Order Service      (cart-checkout-topic)
Cart Service ◄──Kafka── Order Service      (order-placed-topic → wyczysc koszyk)
```

## Endpointy REST

Wszystkie wymagaja headera `X-User-ID` (ustawiany przez API Gateway z JWT).

```
GET    /api/v1/cart                         - Pobierz koszyk
POST   /api/v1/cart/items                   - Dodaj produkt (productNumber, quantity)
PUT    /api/v1/cart/items                   - Zmien ilosc (productNumber, quantity)
DELETE /api/v1/cart/items/{productNumber}    - Usun produkt z koszyka
DELETE /api/v1/cart                          - Wyczysc koszyk
POST   /api/v1/cart/checkout                - Zloz zamowienie (wymaga X-User-Email, body: addressId)
```

## Flow dodania do koszyka

```
1. POST /api/v1/cart/items { productNumber, quantity }
2. gRPC → Product Service: pobierz dane produktu (nazwa, cena)
3. gRPC → Inventory Service: zarezerwuj produkt (reserveProduct)
4. Zapisz CartItem w Redis (z reservationId)
5. Ustaw TTL rezerwacji (15 min)
```

## Flow checkout

```
1. POST /api/v1/cart/checkout { addressId }
2. Walidacja: koszyk niepusty, rezerwacja nie wygasla
3. Kafka → cart-checkout-topic (CartCheckoutEvent z userId, email, items, totalPrice)
4. Oczekiwanie na OrderPlacedEvent → usun koszyk z Redis
```

## Kafka Events

| Topic | Rola | Event |
|-------|------|-------|
| `cart-checkout-topic` | Producer | CartCheckoutEvent |
| `order-placed-topic` | Consumer | OrderPlacedEvent → usun koszyk |
