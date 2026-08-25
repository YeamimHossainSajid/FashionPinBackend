CREATE TABLE outfits (
    id VARCHAR(36) PRIMARY KEY,
    author_user_id VARCHAR(36) NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    style VARCHAR(50),
    occasion VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE outfit_items (
    id VARCHAR(36) PRIMARY KEY,
    outfit_id VARCHAR(36) NOT NULL REFERENCES outfits(id) ON DELETE CASCADE,
    product_id VARCHAR(36) NOT NULL,
    category VARCHAR(50) NOT NULL,
    position_index INT DEFAULT 0
);

CREATE TABLE fashion_posts (
    id VARCHAR(36) PRIMARY KEY,
    author_user_id VARCHAR(36) NOT NULL,
    outfit_id VARCHAR(36) REFERENCES outfits(id) ON DELETE SET NULL,
    caption TEXT,
    visibility VARCHAR(20) DEFAULT 'PUBLIC',
    style VARCHAR(50),
    occasion VARCHAR(50),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE fashion_post_media (
    post_id VARCHAR(36) NOT NULL REFERENCES fashion_posts(id) ON DELETE CASCADE,
    media_id VARCHAR(36) NOT NULL,
    PRIMARY KEY (post_id, media_id)
);

CREATE TABLE fashion_tags (
    id VARCHAR(36) PRIMARY KEY,
    post_id VARCHAR(36) NOT NULL REFERENCES fashion_posts(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    tag_type VARCHAR(50) NOT NULL
);

CREATE INDEX idx_outfits_author ON outfits(author_user_id);
CREATE INDEX idx_outfit_items_product ON outfit_items(product_id);
CREATE INDEX idx_posts_author ON fashion_posts(author_user_id);
CREATE INDEX idx_posts_style ON fashion_posts(style);
CREATE INDEX idx_posts_occasion ON fashion_posts(occasion);
CREATE INDEX idx_posts_created ON fashion_posts(created_at DESC);
CREATE INDEX idx_tags_name ON fashion_tags(name);
CREATE INDEX idx_tags_type ON fashion_tags(tag_type);
