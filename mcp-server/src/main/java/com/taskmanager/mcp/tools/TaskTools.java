package com.taskmanager.mcp.tools;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.taskmanager.mcp.dto.TaskSummary;
import com.taskmanager.mcp.model.Task;
import com.taskmanager.mcp.model.TaskStatus;
import com.taskmanager.mcp.repository.TaskRepository;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class TaskTools {

    private final TaskRepository taskRepository;
    private final ObjectMapper objectMapper;

    public TaskTools(TaskRepository taskRepository, ObjectMapper objectMapper) {
        this.taskRepository = taskRepository;
        this.objectMapper = objectMapper;
    }

    @Tool(name = "mcp-tasks", description = "Accepts a list of Task objects and inserts them into the database. Each task should have: title (required, max 100 chars), description (optional, max 500 chars), status (TODO, IN_PROGRESS, or DONE), dueDate (optional, yyyy-MM-dd). Returns the count of inserted and failed records.")
    public String insertTasks(@ToolParam(description = "List of task objects to insert") List<Task> tasks) {
        int inserted = 0;
        int failed = 0;

        for (Task task : tasks) {
            try {
                if (task.getTitle() == null || task.getTitle().isBlank()) {
                    failed++;
                    continue;
                }
                if (task.getTitle().length() > 100) {
                    task.setTitle(task.getTitle().substring(0, 100));
                }
                if (task.getDescription() != null && task.getDescription().length() > 500) {
                    task.setDescription(task.getDescription().substring(0, 500));
                }
                if (task.getStatus() == null) {
                    task.setStatus(TaskStatus.TODO);
                }
                task.setId(null);
                taskRepository.save(task);
                inserted++;
            } catch (Exception e) {
                failed++;
            }
        }

        return String.format("{\"inserted\": %d, \"failed\": %d}", inserted, failed);
    }

    @Tool(name = "mcp-tasks-summary", description = "Returns summary statistics about all tasks in the database, including total count and breakdown by status (TODO, IN_PROGRESS, DONE).")
    public String getTasksSummary() {
        List<Object[]> statusCounts = taskRepository.countByStatusGrouped();
        long total = taskRepository.count();

        Map<String, Long> byStatus = new LinkedHashMap<>();
        byStatus.put("TODO", 0L);
        byStatus.put("IN_PROGRESS", 0L);
        byStatus.put("DONE", 0L);

        for (Object[] row : statusCounts) {
            TaskStatus status = (TaskStatus) row[0];
            Long count = (Long) row[1];
            byStatus.put(status.name(), count);
        }

        TaskSummary summary = new TaskSummary(total, byStatus);
        try {
            return objectMapper.writeValueAsString(summary);
        } catch (JsonProcessingException e) {
            return String.format("{\"totalTasks\": %d, \"byStatus\": %s}", total, byStatus);
        }
    }
}
