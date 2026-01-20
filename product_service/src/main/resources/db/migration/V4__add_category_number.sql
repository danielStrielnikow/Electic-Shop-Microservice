-- Add category_number column to categories table
ALTER TABLE categories ADD COLUMN category_number VARCHAR(20);

-- Generate category_number for existing rows (if any)
UPDATE categories
SET category_number = 'CAT-' || UPPER(SUBSTRING(MD5(RANDOM()::TEXT), 1, 6))
WHERE category_number IS NULL;

-- Make category_number NOT NULL and UNIQUE after populating
ALTER TABLE categories ALTER COLUMN category_number SET NOT NULL;
ALTER TABLE categories ADD CONSTRAINT uk_categories_number UNIQUE (category_number);

-- Create index on category_number
CREATE INDEX idx_categories_number ON categories(category_number);
