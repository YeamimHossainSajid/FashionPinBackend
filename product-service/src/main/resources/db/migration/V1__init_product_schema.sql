CREATE TABLE IF NOT EXISTS products (
    id VARCHAR(36) PRIMARY KEY,
    brand_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    description TEXT,
    category VARCHAR(100) NOT NULL,
    subcategory VARCHAR(100),
    product_type VARCHAR(100),
    status VARCHAR(32) NOT NULL,
    price NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL,
    primary_media_id VARCHAR(36),
    media_ids TEXT,
    gender VARCHAR(32),
    color VARCHAR(64),
    size VARCHAR(32),
    material VARCHAR(128),
    pattern VARCHAR(128),
    style VARCHAR(128),
    season VARCHAR(64),
    occasion VARCHAR(128),
    fit VARCHAR(64),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_products_category ON products(category);
CREATE INDEX IF NOT EXISTS idx_products_brand_id ON products(brand_id);
CREATE INDEX IF NOT EXISTS idx_products_status ON products(status);

CREATE TABLE IF NOT EXISTS outbox_events (
    id VARCHAR(36) PRIMARY KEY,
    aggregate_type VARCHAR(255) NOT NULL,
    aggregate_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(255) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(32) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL,
    processed_at TIMESTAMP WITH TIME ZONE
);
