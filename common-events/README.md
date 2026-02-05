# Common Events

Biblioteka wspoldzielona - definicje proto (gRPC), eventy Kafka, BaseEntity.

## Zawartosc

### Protobuf / gRPC

```
src/main/proto/
├── auth.proto          - AuthService (login, register, refresh, validate, logout)
├── product.proto       - ProductGrpcService (GetProductForCart)
└── inventory.proto     - InventoryGrpcService (Reserve, Check, Cancel, Update)
```

Wygenerowane klasy Java: `pl.electricshop.grpc.*`

### Eventy Kafka

```
events/
├── cart/
│   ├── CartCheckoutEvent       - Checkout koszyka
│   └── CartItemPayload         - Pozycja koszyka
├── payment/
│   ├── OrderCreatedEvent       - Nowe zamowienie → Payment
│   ├── OrderPlacedEvent        - Zamowienie potwierdzone
│   ├── OrderFailedEvent        - Zamowienie anulowane
│   ├── OrderItemPayload        - Pozycja zamowienia
│   ├── PaymentSucceededEvent   - Platnosc udana
│   └── PaymentFailedEvent      - Platnosc nieudana
└── product/
    └── ProductEvent            - Dodanie/aktualizacja produktu
```

### BaseEntity

```java
@MappedSuperclass
public abstract class BaseEntity {
    UUID uuid;           // @Id @GeneratedValue(UUID)
    LocalDateTime createdAt;   // @CreatedDate
    LocalDateTime updatedAt;   // @LastModifiedDate
}
```

## Build

```bash
./gradlew :common-events:build
```

Generuje klasy gRPC z plikow .proto automatycznie.
