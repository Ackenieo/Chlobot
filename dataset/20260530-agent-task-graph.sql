USE chlobot;

CREATE TABLE IF NOT EXISTS agent_task_node (
    id VARCHAR(64) NOT NULL,
    task_id VARCHAR(64) NOT NULL,
    name VARCHAR(255) NOT NULL,
    node_type VARCHAR(80) NOT NULL,
    status VARCHAR(50) NOT NULL,
    owner VARCHAR(120) NULL,
    priority INT DEFAULT 0,
    payload_json JSON NULL,
    result_json JSON NULL,
    error_message TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (task_id, id),
    INDEX idx_node_task_id (task_id),
    INDEX idx_node_status (status),
    INDEX idx_node_owner (owner)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent DAG 节点表';

CREATE TABLE IF NOT EXISTS agent_task_edge (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    from_node_id VARCHAR(64) NOT NULL,
    to_node_id VARCHAR(64) NOT NULL,
    edge_type VARCHAR(50) NOT NULL DEFAULT 'BLOCKS',
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_edge (task_id, from_node_id, to_node_id),
    INDEX idx_edge_from (from_node_id),
    INDEX idx_edge_to (to_node_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent DAG 边表';

CREATE TABLE IF NOT EXISTS agent_task_node_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    node_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    payload_json JSON NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_node_event_task (task_id),
    INDEX idx_node_event_node (node_id),
    INDEX idx_node_event_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent DAG 节点事件表';
