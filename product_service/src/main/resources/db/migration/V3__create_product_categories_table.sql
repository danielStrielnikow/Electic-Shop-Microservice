-- Create junction table for ManyToMany relationship
CREATE TABLE IF NOT EXISTS product_categories (
    product_id UUID NOT NULL,   -- Definiujemy kolumnę lokalną
    category_id UUID NOT NULL,  -- Definiujemy kolumnę lokalną

    -- Definiujemy klucz główny na podstawie tych kolumn
    PRIMARY KEY (product_id, category_id),

    -- Definiujemy klucze obce wskazujące na kolumny 'uuid' w tabelach rodzicach
    CONSTRAINT fk_product_categories_product
        FOREIGN KEY (product_id) REFERENCES products(uuid) ON DELETE CASCADE,
    CONSTRAINT fk_product_categories_category
        FOREIGN KEY (category_id) REFERENCES categories(uuid) ON DELETE CASCADE
);

-- Tworzymy indeksy na kolumnach product_id i category_id (a nie na uuid!)
CREATE INDEX IF NOT EXISTS idx_product_categories_product ON product_categories(product_id);
CREATE INDEX IF NOT EXISTS idx_product_categories_category ON product_categories(category_id);