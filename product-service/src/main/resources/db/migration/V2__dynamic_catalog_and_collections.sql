-- 1. CATEGORIES TABLE
CREATE TABLE IF NOT EXISTS categories (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL UNIQUE,
    description TEXT,
    parent_id VARCHAR(36) REFERENCES categories(id) ON DELETE SET NULL,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    banner_media_id VARCHAR(36),
    banner_media_url VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_categories_slug ON categories(slug);
CREATE INDEX IF NOT EXISTS idx_categories_parent ON categories(parent_id);

-- 2. ITEM TYPES TABLE
CREATE TABLE IF NOT EXISTS item_types (
    id VARCHAR(36) PRIMARY KEY,
    category_id VARCHAR(36) NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    slug VARCHAR(100) NOT NULL,
    description TEXT,
    display_order INT NOT NULL DEFAULT 0,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    size_guide_url VARCHAR(512),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_category_item_type_slug UNIQUE (category_id, slug)
);
CREATE INDEX IF NOT EXISTS idx_item_types_category ON item_types(category_id);
CREATE INDEX IF NOT EXISTS idx_item_types_slug ON item_types(slug);

-- 3. COLLECTIONS TABLE
CREATE TABLE IF NOT EXISTS collections (
    id VARCHAR(36) PRIMARY KEY,
    name VARCHAR(150) NOT NULL,
    slug VARCHAR(150) NOT NULL UNIQUE,
    tagline VARCHAR(255),
    description TEXT,
    hero_media_id VARCHAR(36),
    hero_media_url VARCHAR(512),
    accent_color VARCHAR(16),
    display_order INT NOT NULL DEFAULT 0,
    is_featured BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    starts_at TIMESTAMP WITH TIME ZONE,
    ends_at TIMESTAMP WITH TIME ZONE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_collections_slug ON collections(slug);

-- 4. ALTER PRODUCTS TABLE (Add foreign key columns, keep backward-compatibility)
ALTER TABLE products ADD COLUMN IF NOT EXISTS category_id VARCHAR(36) REFERENCES categories(id) ON DELETE SET NULL;
ALTER TABLE products ADD COLUMN IF NOT EXISTS item_type_id VARCHAR(36) REFERENCES item_types(id) ON DELETE SET NULL;
ALTER TABLE products ADD COLUMN IF NOT EXISTS summary VARCHAR(500);
ALTER TABLE products ADD COLUMN IF NOT EXISTS gallery_media_ids TEXT;
ALTER TABLE products ADD COLUMN IF NOT EXISTS tags TEXT;

CREATE INDEX IF NOT EXISTS idx_products_category_id ON products(category_id);
CREATE INDEX IF NOT EXISTS idx_products_item_type_id ON products(item_type_id);

-- 5. PRODUCT VARIANTS TABLE (SKU, Color, Size, Stock)
CREATE TABLE IF NOT EXISTS product_variants (
    id VARCHAR(36) PRIMARY KEY,
    product_id VARCHAR(36) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    sku VARCHAR(100) NOT NULL UNIQUE,
    color_name VARCHAR(64) NOT NULL,
    color_hex VARCHAR(16),
    size VARCHAR(32) NOT NULL,
    price_override NUMERIC(12, 2),
    stock_quantity INT NOT NULL DEFAULT 0,
    media_ids TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_variants_product ON product_variants(product_id);
CREATE INDEX IF NOT EXISTS idx_variants_sku ON product_variants(sku);

-- 6. COLLECTION PRODUCTS M2M
CREATE TABLE IF NOT EXISTS collection_products (
    collection_id VARCHAR(36) NOT NULL REFERENCES collections(id) ON DELETE CASCADE,
    product_id VARCHAR(36) NOT NULL REFERENCES products(id) ON DELETE CASCADE,
    display_order INT NOT NULL DEFAULT 0,
    curator_note VARCHAR(255),
    PRIMARY KEY (collection_id, product_id)
);
CREATE INDEX IF NOT EXISTS idx_col_prod_prod ON collection_products(product_id);

-- 7. SEED INITIAL STANDARD CATEGORIES & COLLECTIONS (Portable SQL)
INSERT INTO categories (id, name, slug, description, display_order, is_active)
SELECT 'cat_men', 'Men', 'men', 'Men''s apparel, footwear, and accessories', 1, TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'men');

INSERT INTO categories (id, name, slug, description, display_order, is_active)
SELECT 'cat_women', 'Women', 'women', 'Women''s designer fashion, dresses, and footwear', 2, TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'women');

INSERT INTO categories (id, name, slug, description, display_order, is_active)
SELECT 'cat_kids', 'Kids', 'kids', 'Children and toddler fashion', 3, TRUE
WHERE NOT EXISTS (SELECT 1 FROM categories WHERE slug = 'kids');

INSERT INTO item_types (id, category_id, name, slug, display_order, is_active)
SELECT 'it_m_shoes', 'cat_men', 'Shoes', 'shoes', 1, TRUE
WHERE NOT EXISTS (SELECT 1 FROM item_types WHERE slug = 'shoes' AND category_id = 'cat_men');

INSERT INTO item_types (id, category_id, name, slug, display_order, is_active)
SELECT 'it_m_pants', 'cat_men', 'Trousers & Chinos', 'trousers-chinos', 2, TRUE
WHERE NOT EXISTS (SELECT 1 FROM item_types WHERE slug = 'trousers-chinos' AND category_id = 'cat_men');

INSERT INTO item_types (id, category_id, name, slug, display_order, is_active)
SELECT 'it_m_jackets', 'cat_men', 'Jackets & Coats', 'jackets-coats', 3, TRUE
WHERE NOT EXISTS (SELECT 1 FROM item_types WHERE slug = 'jackets-coats' AND category_id = 'cat_men');

INSERT INTO item_types (id, category_id, name, slug, display_order, is_active)
SELECT 'it_w_pants', 'cat_women', 'Wide-Leg Trousers', 'wide-leg-trousers', 1, TRUE
WHERE NOT EXISTS (SELECT 1 FROM item_types WHERE slug = 'wide-leg-trousers' AND category_id = 'cat_women');

INSERT INTO item_types (id, category_id, name, slug, display_order, is_active)
SELECT 'it_w_dresses', 'cat_women', 'Dresses & Silks', 'dresses-silks', 2, TRUE
WHERE NOT EXISTS (SELECT 1 FROM item_types WHERE slug = 'dresses-silks' AND category_id = 'cat_women');

INSERT INTO item_types (id, category_id, name, slug, display_order, is_active)
SELECT 'it_w_shoes', 'cat_women', 'Shoes & Heels', 'shoes-heels', 3, TRUE
WHERE NOT EXISTS (SELECT 1 FROM item_types WHERE slug = 'shoes-heels' AND category_id = 'cat_women');

INSERT INTO collections (id, name, slug, tagline, description, accent_color, display_order, is_featured, is_active)
SELECT 'col_old_money', 'Old Money', 'old-money', 'Classic tailoring and quiet luxury essentials', 'Timeless pieces crafted with exquisite fabrics.', '#2C3E50', 1, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM collections WHERE slug = 'old-money');

INSERT INTO collections (id, name, slug, tagline, description, accent_color, display_order, is_featured, is_active)
SELECT 'col_minimal_luxe', 'Minimal Luxe', 'minimal-luxe', 'Monochrome palettes and tailored silhouettes', 'Modern minimalism meets luxurious comfort.', '#E0D7C6', 2, TRUE, TRUE
WHERE NOT EXISTS (SELECT 1 FROM collections WHERE slug = 'minimal-luxe');
