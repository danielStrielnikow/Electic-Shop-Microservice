-- Ustawienie schematu (jeśli używasz osobnego schematu dla płatności)
CREATE SCHEMA IF NOT EXISTS payments;
SET search_path TO payments;

CREATE TABLE payment (
    -- Pola z BaseEntity
    uuid UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    -- Pola własne Payment
    -- Uwaga: Hibernate mapuje camelCase na snake_case
    order_id UUID NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(10) NOT NULL,

    -- Kluczowe dane dla Stripe
    client_secret VARCHAR(255),

    -- Pole PaymentMethod (zmapowane jako tekst - patrz uwaga niżej)
    payment_method VARCHAR(255),

    -- Pola odpowiedzi z bramki (Payment Gateway - pg)
    pg_payment_id VARCHAR(255),      -- ID płatności u dostawcy (np. pi_...)
    pg_status VARCHAR(50),           -- status (succeeded, pending)
    pg_response_message TEXT,        -- komunikaty błędów mogą być długie
    pg_name VARCHAR(100),            -- nazwa bramki (np. Stripe)

    PRIMARY KEY (uuid)
);

-- Indeks do szybkiego wyszukiwania płatności po ID zamówienia
CREATE INDEX idx_payment_order_id ON payment(order_id);

-- Indeks do szukania po ID zewnętrznym (np. przy webhookach ze Stripe)
CREATE INDEX idx_payment_pg_payment_id ON payment(pg_payment_id);