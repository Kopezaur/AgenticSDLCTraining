package com.taskmanager.mcp.tools;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.mcp.model.Task;
import com.taskmanager.mcp.model.TaskStatus;
import com.taskmanager.mcp.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskToolsTest {

    @Mock
    private TaskRepository taskRepository;

    private TaskTools taskTools;

    @BeforeEach
    void setUp() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        taskTools = new TaskTools(taskRepository, objectMapper);
    }

    @Test
    void insertTasks_validTasks_returnsInsertedCount() {
        Task task = new Task("Test Task", "Description", TaskStatus.TODO, LocalDate.of(2026, 5, 1));
        when(taskRepository.save(any(Task.class))).thenReturn(task);

        String result = taskTools.insertTasks(List.of(task));

        assertTrue(result.contains("\"inserted\": 1"));
        assertTrue(result.contains("\"failed\": 0"));
    }

    @Test
    void insertTasks_blankTitle_countedAsFailed() {
        Task invalid = new Task("", "Description", TaskStatus.TODO, null);

        String result = taskTools.insertTasks(List.of(invalid));

        assertTrue(result.contains("\"inserted\": 0"));
        assertTrue(result.contains("\"failed\": 1"));
    }

    @Test
    void insertTasks_nullTitle_countedAsFailed() {
        Task invalid = new Task();
        invalid.setDescription("No title");

        String result = taskTools.insertTasks(List.of(invalid));

        assertTrue(result.contains("\"inserted\": 0"));
        assertTrue(result.contains("\"failed\": 1"));
    }

    @Test
    void getTasksSummary_returnsCorrectFormat() {
        when(taskRepository.count()).thenReturn(10L);
        when(taskRepository.countByStatusGrouped()).thenReturn(List.of(
                new Object[]{TaskStatus.TODO, 5L},
                new Object[]{TaskStatus.IN_PROGRESS, 3L},
                new Object[]{TaskStatus.DONE, 2L}
        ));

        String result = taskTools.getTasksSummary();

        assertTrue(result.contains("\"totalTasks\":10"));
        assertTrue(result.contains("\"TODO\":5"));
        assertTrue(result.contains("\"IN_PROGRESS\":3"));
        assertTrue(result.contains("\"DONE\":2"));
    }
}
