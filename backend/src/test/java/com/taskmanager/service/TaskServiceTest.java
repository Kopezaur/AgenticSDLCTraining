package com.taskmanager.service;

import com.taskmanager.exception.TaskNotFoundException;
import com.taskmanager.model.Task;
import com.taskmanager.model.TaskStatus;
import com.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    private Task sampleTask;

    @BeforeEach
    void setUp() {
        sampleTask = new Task("Test Task", "A test description", TaskStatus.TODO, LocalDate.of(2026, 4, 1));
        sampleTask.setId(1L);
    }

    @Test
    void getAllTasks_returnsAllTasks() {
        when(taskRepository.findAll()).thenReturn(List.of(sampleTask));

        List<Task> tasks = taskService.getAllTasks();

        assertEquals(1, tasks.size());
        assertEquals("Test Task", tasks.get(0).getTitle());
    }

    @Test
    void getTaskById_existingId_returnsTask() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        Task task = taskService.getTaskById(1L);

        assertEquals("Test Task", task.getTitle());
    }

    @Test
    void getTaskById_nonExistingId_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.getTaskById(99L));
    }

    @Test
    void createTask_setsDefaultStatusAndSaves() {
        Task newTask = new Task();
        newTask.setTitle("New Task");
        when(taskRepository.save(any(Task.class))).thenReturn(newTask);

        Task created = taskService.createTask(newTask);

        assertEquals(TaskStatus.TODO, created.getStatus());
        verify(taskRepository).save(newTask);
    }

    @Test
    void updateTask_existingId_updatesFields() {
        Task updated = new Task("Updated Title", "Updated Desc", TaskStatus.DONE, LocalDate.of(2026, 5, 1));
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));
        when(taskRepository.save(any(Task.class))).thenReturn(sampleTask);

        taskService.updateTask(1L, updated);

        assertEquals("Updated Title", sampleTask.getTitle());
        assertEquals(TaskStatus.DONE, sampleTask.getStatus());
        verify(taskRepository).save(sampleTask);
    }

    @Test
    void deleteTask_existingId_deletes() {
        when(taskRepository.findById(1L)).thenReturn(Optional.of(sampleTask));

        taskService.deleteTask(1L);

        verify(taskRepository).delete(sampleTask);
    }

    @Test
    void deleteTask_nonExistingId_throwsException() {
        when(taskRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(TaskNotFoundException.class, () -> taskService.deleteTask(99L));
    }
}
