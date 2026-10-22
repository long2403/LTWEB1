CREATE TABLE reader_collections (
    id SERIAL PRIMARY KEY,
    username VARCHAR(30) NOT NULL,
    collection_name VARCHAR(40) NOT NULL,
    book_id INTEGER NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_reader_collections_entry UNIQUE (username, collection_name, book_id)
);

CREATE INDEX idx_reader_collections_username
    ON reader_collections (username, collection_name, created_at DESC);

CREATE TABLE content_reports (
    id SERIAL PRIMARY KEY,
    username VARCHAR(30) NOT NULL,
    book_id INTEGER NOT NULL,
    chapter_number INTEGER,
    reason VARCHAR(40) NOT NULL,
    details VARCHAR(1000),
    status VARCHAR(20) NOT NULL DEFAULT 'OPEN',
    created_at TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    resolved_at TIMESTAMPTZ
);

CREATE INDEX idx_content_reports_status_created
    ON content_reports (status, created_at DESC);
