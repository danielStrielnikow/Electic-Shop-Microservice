-- Create categories table
CREATE TABLE IF NOT EXISTS categories (
    uuid UUID PRIMARY KEY,
    category_name VARCHAR(100) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create index on category_name
CREATE INDEX idx_categories_name ON categories(category_name);
