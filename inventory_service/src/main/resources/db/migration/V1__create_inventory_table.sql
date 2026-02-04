--Create inventory table
CREATE TABLE IF NOT EXISTS inventory (
    uuid UUID PRIMARY KEY,
    product_number VARCHAR(100) NOT NULL UNIQUE,
    available_quantity INT NOT NULL DEFAULT 0,
    reserved_quantity INT NOT NULL DEFAULT 0,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

