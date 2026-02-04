SET search_path TO orders;

CREATE TABLE order_item (
    uuid UUID NOT NULL,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP,
    product_number VARCHAR(255),
    product_name VARCHAR(255),
    quantity INTEGER,
    discount DECIMAL(19, 2),
    ordered_product_price DECIMAL(19, 2),
    order_id UUID,

    PRIMARY KEY (uuid)
);

ALTER TABLE order_item
    ADD CONSTRAINT fk_order_item_orders
    FOREIGN KEY (order_id)
    REFERENCES orders (uuid);

CREATE INDEX idx_order_item_order_id ON order_item(order_id);