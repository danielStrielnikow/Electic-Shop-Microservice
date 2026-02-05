# Product Service

Serwis produktow i kategorii.

## Port: 8083 (REST) / 9090 (gRPC)

## Technologie

- Spring Data JPA + PostgreSQL (schemat: `products`)
- gRPC Server (dane produktu dla Cart Service)
- Flyway (migracje)
- Kafka Producer (eventy dodania/aktualizacji produktow)

## Schemat bazy: `products`

```
categories
├── uuid (PK)
├── category_number (UNIQUE)
├── category_name
├── created_at, updated_at

products
├── uuid (PK)
├── product_number (UNIQUE)
├── product_name
├── image
├── description
├── price, discount, special_price
├── quantity
├── created_at, updated_at

product_categories (junction table)
├── product_uuid (FK)
└── category_uuid (FK)
```

## Endpointy REST

### Publiczne

```
GET /api/v1/products                              - Lista produktow (paginacja, sortowanie, filtrowanie)
    ?pageNumber=0&pageSize=10&sortBy=productName&keyword=laptop&category=Electronics
GET /api/v1/categories                            - Lista kategorii
```

### Admin (wymaga roli ADMIN)

```
POST   /api/v1/admin/products/{categoryNumber}    - Dodaj produkt
PUT    /api/v1/admin/products/{productName}        - Edytuj produkt
PUT    /api/v1/admin/products/{productName}/image  - Zaktualizuj zdjecie
DELETE /api/v1/admin/products/{productName}        - Usun produkt
```

## gRPC (ProductGrpcService)

```protobuf
rpc GetProductForCart(ProductCartRequest) returns (ProductCartResponse)
    → zwraca: productNumber, productName, price, discount, quantity
```

## Kafka Events

| Topic | Rola | Event |
|-------|------|-------|
| `product-add-topic` | Producer | ProductEvent → Inventory Service |
| `product-update-topic` | Producer | ProductEvent → Inventory Service |
