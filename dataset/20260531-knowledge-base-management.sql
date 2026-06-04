CREATE TABLE IF NOT EXISTS knowledge_base (
    id VARCHAR(255) PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    enabled BOOLEAN DEFAULT true,
    is_delete BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS knowledge_base_enabled_idx ON knowledge_base (enabled, is_delete);

ALTER TABLE document_embeddings
    ADD COLUMN IF NOT EXISTS knowledge_base_id VARCHAR(255) REFERENCES knowledge_base(id),
    ADD COLUMN IF NOT EXISTS enabled BOOLEAN DEFAULT true,
    ADD COLUMN IF NOT EXISTS is_delete BOOLEAN DEFAULT false;

CREATE INDEX IF NOT EXISTS document_embeddings_knowledge_base_idx
    ON document_embeddings (knowledge_base_id, enabled, is_delete);
