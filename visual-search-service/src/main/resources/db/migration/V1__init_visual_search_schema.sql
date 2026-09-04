-- Flyway Migration V1__init_visual_search_schema.sql for visual_search_db

CREATE TABLE IF NOT EXISTS visual_search_jobs (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    input_media_id VARCHAR(255),
    query_image_url VARCHAR(1024),
    status VARCHAR(50) NOT NULL,
    error_details TEXT,
    created_at TIMESTAMPTZ NOT NULL,
    completed_at TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_visual_search_jobs_user_id ON visual_search_jobs(user_id);
CREATE INDEX IF NOT EXISTS idx_visual_search_jobs_status ON visual_search_jobs(status);

CREATE TABLE IF NOT EXISTS visual_similarity_results (
    id UUID PRIMARY KEY,
    job_id UUID NOT NULL REFERENCES visual_search_jobs(id) ON DELETE CASCADE,
    product_id UUID NOT NULL,
    similarity_score REAL NOT NULL,
    category VARCHAR(100),
    bounding_box VARCHAR(255),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_visual_similarity_job_id ON visual_similarity_results(job_id);
CREATE INDEX IF NOT EXISTS idx_visual_similarity_product_id ON visual_similarity_results(product_id);

CREATE TABLE IF NOT EXISTS visual_vector_index (
    product_id UUID PRIMARY KEY,
    embedding_dimension INT NOT NULL,
    vector_data BYTEA NOT NULL,
    category VARCHAR(100),
    created_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_visual_vector_category ON visual_vector_index(category);
