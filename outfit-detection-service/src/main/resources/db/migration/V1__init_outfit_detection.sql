CREATE TABLE detection_jobs (
    id VARCHAR(36) PRIMARY KEY,
    user_id VARCHAR(36) NOT NULL,
    input_media_id VARCHAR(36) NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'CREATED',
    error_message TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detection_results (
    id VARCHAR(36) PRIMARY KEY,
    job_id VARCHAR(36) NOT NULL REFERENCES detection_jobs(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE detected_items (
    id VARCHAR(36) PRIMARY KEY,
    result_id VARCHAR(36) NOT NULL REFERENCES detection_results(id) ON DELETE CASCADE,
    category VARCHAR(50) NOT NULL,
    confidence FLOAT,
    bounding_box_json TEXT,
    product_candidate_ids_json TEXT,
    attributes_json TEXT
);

CREATE INDEX idx_jobs_user ON detection_jobs(user_id);
CREATE INDEX idx_jobs_status ON detection_jobs(status);
CREATE INDEX idx_jobs_created ON detection_jobs(created_at DESC);
CREATE INDEX idx_jobs_input_media ON detection_jobs(input_media_id);
