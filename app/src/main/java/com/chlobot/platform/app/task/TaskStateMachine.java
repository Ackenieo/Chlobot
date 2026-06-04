package com.chlobot.platform.app.task;

import com.chlobot.platform.common.error.BadRequestException;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public final class TaskStateMachine {

    private static final Map<AgentTaskStatus, Set<AgentTaskStatus>> ALLOWED = new EnumMap<>(AgentTaskStatus.class);

    static {
        ALLOWED.put(AgentTaskStatus.PENDING, EnumSet.of(AgentTaskStatus.ANALYZING, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.ANALYZING, EnumSet.of(AgentTaskStatus.PENDING_CLARIFICATION, AgentTaskStatus.PLANNING,
                AgentTaskStatus.FAILED, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.PENDING_CLARIFICATION, EnumSet.of(AgentTaskStatus.PLANNING, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.PLANNING, EnumSet.of(AgentTaskStatus.PENDING_CONFIRMATION, AgentTaskStatus.EXECUTING,
                AgentTaskStatus.FAILED, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.PENDING_CONFIRMATION, EnumSet.of(AgentTaskStatus.CONFIRMED, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.CONFIRMED, EnumSet.of(AgentTaskStatus.EXECUTING, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.EXECUTING, EnumSet.of(AgentTaskStatus.COMPLETED, AgentTaskStatus.FAILED,
                AgentTaskStatus.WAITING_USER_INPUT, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.WAITING_USER_INPUT, EnumSet.of(AgentTaskStatus.EXECUTING, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.COMPLETED, EnumSet.noneOf(AgentTaskStatus.class));
        ALLOWED.put(AgentTaskStatus.FAILED, EnumSet.of(AgentTaskStatus.PENDING, AgentTaskStatus.CANCELLED));
        ALLOWED.put(AgentTaskStatus.CANCELLED, EnumSet.of(AgentTaskStatus.PENDING));
    }

    private TaskStateMachine() {
    }

    public static void validateTransition(AgentTaskStatus from, AgentTaskStatus to) {
        Set<AgentTaskStatus> allowed = ALLOWED.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new BadRequestException("Invalid task transition: " + from + " -> " + to);
        }
    }
}
