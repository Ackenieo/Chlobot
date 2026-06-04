package com.chlobot.platform.app.task;

public enum AgentTaskStatus {
    PENDING,
    ANALYZING,
    PENDING_CLARIFICATION,
    PLANNING,
    PENDING_CONFIRMATION,
    CONFIRMED,
    EXECUTING,
    WAITING_USER_INPUT,
    COMPLETED,
    FAILED,
    CANCELLED
}
