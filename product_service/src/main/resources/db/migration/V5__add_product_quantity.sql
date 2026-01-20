-- Add quantity column to products table
ALTER TABLE products ADD COLUMN quantity INTEGER NOT NULL DEFAULT 0;
