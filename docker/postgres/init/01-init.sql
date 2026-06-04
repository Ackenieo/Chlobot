-- PostgreSQL 初始化脚本
CREATE EXTENSION IF NOT EXISTS vector;

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

CREATE TABLE IF NOT EXISTS document_embeddings (
    id SERIAL PRIMARY KEY,
    document_id VARCHAR(255) NOT NULL,
    chunk_id VARCHAR(255) UNIQUE NOT NULL,
    title VARCHAR(500),
    content TEXT NOT NULL,
    embedding vector(1024),
    metadata JSONB,
    knowledge_base_id VARCHAR(255) REFERENCES knowledge_base(id),
    enabled BOOLEAN DEFAULT true,
    is_delete BOOLEAN DEFAULT false,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS document_embeddings_document_id_idx ON document_embeddings (document_id);
CREATE INDEX IF NOT EXISTS document_embeddings_knowledge_base_idx ON document_embeddings (knowledge_base_id, enabled, is_delete);
CREATE INDEX IF NOT EXISTS document_embeddings_embedding_idx
ON document_embeddings
USING hnsw (embedding vector_cosine_ops)
WITH (m = 16, ef_construction = 64);
CREATE INDEX IF NOT EXISTS document_embeddings_content_idx ON document_embeddings USING gin (to_tsvector('simple', content));
CREATE INDEX IF NOT EXISTS document_embeddings_metadata_idx ON document_embeddings USING gin (metadata);

COMMENT ON TABLE document_embeddings IS '文档向量嵌入表';
COMMENT ON COLUMN document_embeddings.document_id IS '文档唯一标识';
COMMENT ON COLUMN document_embeddings.chunk_id IS '文档分块唯一标识';
COMMENT ON COLUMN document_embeddings.title IS '文档标题';
COMMENT ON COLUMN document_embeddings.content IS '文档文本内容';
COMMENT ON COLUMN document_embeddings.embedding IS '1024 维向量嵌入';
COMMENT ON COLUMN document_embeddings.metadata IS 'JSONB 元数据';
