package com.taskmanager.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.taskmanager.exception.GlobalExceptionHandler;
import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import java.util.Arrays;
import java.util.Collections;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TaskController.class)
@Import(GlobalExceptionHandler.class)
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TaskService taskService;

    private ObjectMapper objectMapper;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        sampleTask = new Task("Test Task", "Description", TaskStatus.TODO, LocalDate.of(2026, 4, 1));
        sampleTask.setId(1L);
    }

    @Test
    void getAllTasks_returnsTaskList() throws Exception {
        when(taskService.getAllTasks()).thenReturn(List.of(sampleTask));

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void getTaskById_existingId_returnsTask() throws Exception {
        when(taskService.getTaskById(1L)).thenReturn(sampleTask);

        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void getTaskById_nonExistingId_returns404() throws Exception {
        when(taskService.getTaskById(99L)).thenThrow(new TaskNotFoundException("Task not found with id: 99"));

        mockMvc.perform(get("/api/tasks/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Task not found with id: 99"));
    }

    @Test
    void createTask_validInput_returns201() throws Exception {
        when(taskService.createTask(any(Task.class))).thenReturn(sampleTask);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTask)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void createTask_blankTitle_returns400() throws Exception {
        Task invalid = new Task("", "Description", TaskStatus.TODO, null);

        mockMvc.perform(post("/api/tasks")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").exists());
    }

    @Test
    void updateTask_validInput_returns200() throws Exception {
        when(taskService.updateTask(eq(1L), any(Task.class))).thenReturn(sampleTask);

        mockMvc.perform(put("/api/tasks/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleTask)))
                .andExpect(status().isOk());
    }

    @Test
    void deleteTask_existingId_returns204() throws Exception {
        doNothing().when(taskService).deleteTask(1L);

        mockMvc.perform(delete("/api/tasks/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteTask_nonExistingId_returns404() throws Exception {
        doThrow(new TaskNotFoundException("Task not found with id: 99")).when(taskService).deleteTask(99L);

        mockMvc.perform(delete("/api/tasks/99"))
                .andExpect(status().isNotFound());
    }
}
