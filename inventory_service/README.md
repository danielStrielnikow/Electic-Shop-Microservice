# Inventory Service

Serwis stanow magazynowych i rezerwacji produktow.

## Port: 8087 (REST) / 9093 (gRPC)

## Technologie

- gRPC Server (rezerwacje, dostepnosc)
- Spring Data JPA + PostgreSQL (schemat: `inventory`)
- Redis (tymczasowe rezerwacje z TTL 15 min)
- Kafka Consumer (eventy produktowe i zamowieniowe)
- Flyway (migracje)
- Scheduled Jobs (cleanup wygaslych rezerwacji)
- Java 21 Virtual Threads

## Schemat bazy: `inventory`

```
inventory
├── uuid (PK)
├── product_number (UNIQUE)
├── available_quantity (dostepne do sprzedazy)
├── reserved_quantity (zarezerwowane w koszykach)
├── created_at
└── updated_at
```

## Rezerwacje (Redis)

```
Klucz: reservation:{userId}:{productNumber}
Wartosc: quantity (int)
TTL: 15 minut
```

## gRPC (InventoryGrpcService)

```protobuf
rpc ReserveProduct(ReservationRequest) returns (ReservationResponse)
    → available↓, reserved↑, Redis SET z TTL

rpc CheckAvailability(AvailabilityRequest) returns (AvailabilityResponse)
    → zwraca availableQuantity

rpc CancelReservation(CancelReservationRequest) returns (CancelReservationResponse)
    → available↑, reserved↓, Redis DELETE

rpc UpdateReservation(UpdateReservationRequest) returns (UpdateReservationResponse)
    → zmienia ilosc rezerwacji (wieksza/mniejsza), Redis UPDATE z nowym TTL
```

## Kafka Events

| Topic | Rola | Akcja |
|-------|------|-------|
| `product-add-topic` | Consumer | Tworzenie rekordu inventory |
| `product-update-topic` | Consumer | Aktualizacja availableQuantity |
| `order-placed-topic` | Consumer | reserved↓ (zamowienie potwierdzone) |
| `order-failed-topic` | Consumer | releaseStock: reserved↓, available↑ |

## Scheduled Job

`cleanupExpiredReservations()` - co minute sprawdza rozbieznosci miedzy Redis a DB.
Jesli rezerwacja wygasla w Redis, przywraca availableQuantity w bazie.
