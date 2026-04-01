package com.taskmanager.mcp.dto;

import java.util.Map;

public class TaskSummary {

    private long totalTasks;
    private Map<String, Long> byStatus;

    public TaskSummary(long totalTasks, Map<String, Long> byStatus) {
        this.totalTasks = totalTasks;
        this.byStatus = byStatus;
    }

    public long getTotalTasks() {
        return totalTasks;
    }

    public Map<String, Long> getByStatus() {
        return byStatus;
    }
}
