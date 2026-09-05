CREATE TABLE IF NOT EXISTS shopping_carts (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36),
    guest_session_token VARCHAR(128) UNIQUE,
    promo_code VARCHAR(50),
    discount_amount NUMERIC(12, 2) DEFAULT 0.00,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_carts_user ON shopping_carts(user_id);
CREATE INDEX IF NOT EXISTS idx_carts_session ON shopping_carts(guest_session_token);

CREATE TABLE IF NOT EXISTS cart_items (
    id VARCHAR(36) PRIMARY KEY,
    cart_id VARCHAR(36) NOT NULL REFERENCES shopping_carts(id) ON DELETE CASCADE,
    product_id VARCHAR(36) NOT NULL,
    variant_id VARCHAR(36) NOT NULL,
    quantity INT NOT NULL CHECK (quantity > 0),
    unit_price NUMERIC(12, 2) NOT NULL,
    currency VARCHAR(3) NOT NULL DEFAULT 'USD',
    product_name VARCHAR(255) NOT NULL,
    product_image VARCHAR(512),
    color VARCHAR(64),
    size VARCHAR(32),
    sku VARCHAR(100),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_cart_variant UNIQUE (cart_id, variant_id)
);
CREATE INDEX IF NOT EXISTS idx_cart_items_cart ON cart_items(cart_id);

CREATE TABLE IF NOT EXISTS promo_codes (
    code VARCHAR(50) PRIMARY KEY,
    discount_type VARCHAR(20) NOT NULL, -- 'PERCENTAGE', 'FIXED_AMOUNT'
    discount_value NUMERIC(12, 2) NOT NULL,
    min_order_amount NUMERIC(12, 2) DEFAULT 0.00,
    max_discount_amount NUMERIC(12, 2),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    expires_at TIMESTAMP WITH TIME ZONE,
    usage_limit INT,
    times_used INT DEFAULT 0
);

-- Seed standard sample promo codes
INSERT INTO promo_codes (code, discount_type, discount_value, min_order_amount, is_active)
SELECT 'FASHION10', 'PERCENTAGE', 10.00, 50.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM promo_codes WHERE code = 'FASHION10');

INSERT INTO promo_codes (code, discount_type, discount_value, min_order_amount, is_active)
SELECT 'WELCOME20', 'FIXED_AMOUNT', 20.00, 100.00, TRUE
WHERE NOT EXISTS (SELECT 1 FROM promo_codes WHERE code = 'WELCOME20');
