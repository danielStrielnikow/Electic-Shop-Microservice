-- ZAWSZE ustawiaj schemat na początku, żeby mieć pewność
SET search_path TO orders;

CREATE TABLE orders (
    -- Pola z BaseEntity
    uuid UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,

    -- Pola własne Order
    user_id UUID,
    order_number VARCHAR(255),
    email VARCHAR(255) NOT NULL,
    order_date TIMESTAMP,
    total_amount DECIMAL(19, 2),
    order_status VARCHAR(50),
    payment_id VARCHAR(255),

    -- Pola z @Embedded AddressSnapshot
    city VARCHAR(255),
    street VARCHAR(255),
    building_name VARCHAR(50),
    zip_code VARCHAR(20),
    state VARCHAR(100),
    country VARCHAR(100),
    original_address_id UUID,

    PRIMARY KEY (uuid)
);

CREATE INDEX idx_orders_user_id ON orders(user_id);
CREATE INDEX idx_orders_email ON orders(email);