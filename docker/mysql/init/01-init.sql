-- MySQL 初始化脚本
CREATE DATABASE IF NOT EXISTS chlobot
DEFAULT CHARACTER SET utf8mb4
DEFAULT COLLATE utf8mb4_unicode_ci;

USE chlobot;

CREATE TABLE IF NOT EXISTS agent_session (
    id VARCHAR(64) PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    metadata JSON NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT DEFAULT 0,
    INDEX idx_session_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 会话表';

CREATE TABLE IF NOT EXISTS conversation_message (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    conversation_id VARCHAR(255) NULL COMMENT '兼容旧字段',
    message_type VARCHAR(50) NOT NULL,
    role VARCHAR(50) NULL,
    content TEXT,
    metadata JSON,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT DEFAULT 0,
    INDEX idx_session_id (session_id),
    INDEX idx_conversation_id (conversation_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='对话消息表';

CREATE TABLE IF NOT EXISTS agent_task (
    id VARCHAR(64) PRIMARY KEY,
    session_id VARCHAR(64) NOT NULL,
    input TEXT NOT NULL,
    mode VARCHAR(50) NOT NULL,
    status VARCHAR(50) NOT NULL,
    risk_level VARCHAR(50) DEFAULT 'LOW',
    budget_json JSON NULL,
    result_json JSON NULL,
    error_code VARCHAR(100) NULL,
    error_message TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_task_session_id (session_id),
    INDEX idx_task_status (status),
    INDEX idx_task_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 任务表';

CREATE TABLE IF NOT EXISTS agent_task_plan (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    version INT NOT NULL DEFAULT 1,
    status VARCHAR(50) NOT NULL,
    plan_json JSON NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_plan_task_id (task_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 任务计划表';

CREATE TABLE IF NOT EXISTS agent_task_event (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    event_type VARCHAR(80) NOT NULL,
    sequence_no BIGINT NOT NULL,
    payload_json JSON NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_task_sequence (task_id, sequence_no),
    INDEX idx_event_task_id (task_id),
    INDEX idx_event_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 任务事件表';

CREATE TABLE IF NOT EXISTS agent_tool_call (
    id VARCHAR(64) PRIMARY KEY,
    task_id VARCHAR(64) NOT NULL,
    tool_name VARCHAR(120) NOT NULL,
    permission_level VARCHAR(20) NOT NULL,
    input_json JSON NULL,
    output_json JSON NULL,
    status VARCHAR(50) NOT NULL,
    requires_confirmation TINYINT DEFAULT 0,
    confirmed_by VARCHAR(120) NULL,
    confirmed_at DATETIME NULL,
    rejection_reason TEXT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_tool_task_id (task_id),
    INDEX idx_tool_status (status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 工具调用表';

CREATE TABLE IF NOT EXISTS agent_audit_log (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    actor_id VARCHAR(120) NULL,
    action VARCHAR(120) NOT NULL,
    target_type VARCHAR(80) NOT NULL,
    target_id VARCHAR(120) NOT NULL,
    payload_json JSON NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_audit_target (target_type, target_id),
    INDEX idx_audit_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 审计日志表';

CREATE TABLE IF NOT EXISTS agent_memory (
    id VARCHAR(64) PRIMARY KEY,
    memory_type VARCHAR(50) NOT NULL,
    content TEXT NOT NULL,
    source VARCHAR(255) NULL,
    confidence DECIMAL(5,4) DEFAULT 1.0000,
    metadata JSON NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    updated_at DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    is_delete TINYINT DEFAULT 0,
    INDEX idx_memory_type (memory_type),
    FULLTEXT KEY ft_memory_content (content)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='Agent 长期记忆表';

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
