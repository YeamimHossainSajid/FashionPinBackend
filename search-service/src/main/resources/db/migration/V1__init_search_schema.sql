-- Flyway Migration V1__init_search_schema.sql for search_db

CREATE TABLE IF NOT EXISTS product_search_index (
    id UUID PRIMARY KEY,
    brand_id UUID NOT NULL,
    title VARCHAR(255) NOT NULL,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    subcategory VARCHAR(100),
    colors TEXT[],
    sizes TEXT[],
    price_amount NUMERIC(12, 2) NOT NULL,
    price_currency VARCHAR(3) NOT NULL,
    tags TEXT[],
    in_stock BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_product_search_brand_id ON product_search_index(brand_id);
CREATE INDEX IF NOT EXISTS idx_product_search_category ON product_search_index(category);
CREATE INDEX IF NOT EXISTS idx_product_search_title ON product_search_index(title);
CREATE INDEX IF NOT EXISTS idx_product_search_price_amount ON product_search_index(price_amount);
CREATE INDEX IF NOT EXISTS idx_product_search_in_stock ON product_search_index(in_stock);
CREATE INDEX IF NOT EXISTS idx_product_search_colors_gin ON product_search_index USING GIN (colors);
CREATE INDEX IF NOT EXISTS idx_product_search_sizes_gin ON product_search_index USING GIN (sizes);
CREATE INDEX IF NOT EXISTS idx_product_search_tags_gin ON product_search_index USING GIN (tags);

CREATE TABLE IF NOT EXISTS fashion_post_search_index (
    id UUID PRIMARY KEY,
    author_user_id UUID NOT NULL,
    caption TEXT,
    style VARCHAR(100),
    occasion VARCHAR(100),
    tags TEXT[],
    media_ids UUID[],
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_post_search_author_user_id ON fashion_post_search_index(author_user_id);
CREATE INDEX IF NOT EXISTS idx_post_search_style ON fashion_post_search_index(style);
CREATE INDEX IF NOT EXISTS idx_post_search_occasion ON fashion_post_search_index(occasion);
CREATE INDEX IF NOT EXISTS idx_post_search_tags_gin ON fashion_post_search_index USING GIN (tags);
