-- Create products table
CREATE TABLE IF NOT EXISTS products (
    uuid UUID PRIMARY KEY,
    product_number VARCHAR(20) NOT NULL UNIQUE,
    product_name VARCHAR(255) NOT NULL,
    image VARCHAR(500),
    description TEXT NOT NULL,
    price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    discount DECIMAL(5, 2) NOT NULL DEFAULT 0,
    special_price DECIMAL(10, 2) NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_products_product_number ON products(product_number);
CREATE INDEX idx_products_product_name ON products(product_name);
CREATE INDEX idx_products_price ON products(price);
