package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("TaskService Tests")
public class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private TaskMapper taskMapper;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    @InjectMocks
    private TaskService taskService;

    private User testUser;
    private Task testTask;
    private TaskRequestDTO requestDTO;
    private TaskResponseDTO responseDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");

        SecurityContextHolder.setContext(securityContext);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(testUser);

        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setCompleted(false);
        testTask.setCreatedAt(LocalDateTime.now());
        testTask.setUser(testUser);

        requestDTO = new TaskRequestDTO("Test Task", "Test Description", false, null);
        responseDTO = new TaskResponseDTO(1L, "Test Task", "Test Description", false, LocalDateTime.now(), null);
    }

    @Test
    @DisplayName("Sollte alle Tasks des Users zurückgeben")
    void getAllTasks_ShouldReturnUserTasks() {
        Task task2 = new Task();
        task2.setId(2L);
        task2.setTitle("Task 2");
        task2.setUser(testUser);

        List<Task> tasks = Arrays.asList(testTask, task2);

        when(taskRepository.findByUser(testUser)).thenReturn(tasks);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

        List<TaskResponseDTO> result = taskService.getAllTasks();

        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findByUser(testUser);
        verify(taskMapper, times(2)).toResponseDTO(any(Task.class));
    }

    @Test
    @DisplayName("Sollte Task nach ID zurückgeben")
    void getTaskById_WhenTaskExists_ShouldReturnTask() {

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTask));
        when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

        TaskResponseDTO result = taskService.getTaskById(1L);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).findByIdAndUser(1L, testUser);
    }

    @Test
    @DisplayName("Sollte Exception werfen wenn Task nicht existiert")
    void getTaskById_WhenTaskNotExists_ShouldThrowException() {

        when(taskRepository.findByIdAndUser(999L, testUser)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> taskService.getTaskById(999L))
                .isInstanceOf(TaskNotFoundException.class)
                .hasMessageContaining("999");

        verify(taskRepository, times(1)).findByIdAndUser(999L, testUser);
    }

    @Test
    @DisplayName("Sollte neuen Task erstellen")
    void createTask_ShouldSaveAndReturnTask() {
        when(taskMapper.toEntity(requestDTO)).thenReturn(testTask);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(testTask)).thenReturn(responseDTO);

        TaskResponseDTO result = taskService.createTask(requestDTO);

        assertNotNull(result);
        assertEquals("Test Task", result.getTitle());
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(taskMapper, times(1)).toEntity(requestDTO);
        verify(taskMapper, times(1)).toResponseDTO(testTask);
    }

    @Test
    @DisplayName("Sollte Task aktualisieren")
    void updateTask_WhenTaskExists_ShouldUpdateAndReturn() {
        TaskRequestDTO updateDto = new TaskRequestDTO(
                "Updated Task",
                "Updated Description",
                true,
                null
        );

        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

        TaskResponseDTO result = taskService.updateTask(1L, updateDto);

        assertNotNull(result);
        verify(taskRepository, times(1)).findByIdAndUser(1L, testUser);
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Sollte Task löschen")
    void deleteTask_WhenTaskExists_ShouldDelete() {
        when(taskRepository.findByIdAndUser(1L, testUser)).thenReturn(Optional.of(testTask));
        doNothing().when(taskRepository).delete(testTask);

        taskService.deleteTask(1L);
        verify(taskRepository, times(1)).findByIdAndUser(1L, testUser);
        verify(taskRepository, times(1)).delete(testTask);
    }

    @Test
    @DisplayName("Sollte nur completed Tasks zurückgeben")
    void getCompletedTasks_ShouldReturnOnlyCompletedTasks() {
        testTask.setCompleted(true);
        List<Task> completedTasks = Arrays.asList(testTask);

        when(taskRepository.findByUserAndCompleted(testUser, true)).thenReturn(completedTasks);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

        List<TaskResponseDTO> result = taskService.getCompletedTasks();

        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByUserAndCompleted(testUser, true);
    }

    @Test
    @DisplayName("Sollte Tasks nach Keyword suchen")
    void searchTasks_ShouldReturnMatchingTasks() {
        // Arrange
        String keyword = "Test";
        List<Task> matchingTasks = Arrays.asList(testTask);

        when(taskRepository.findByUserAndTitleContainingIgnoreCase(testUser, keyword)).thenReturn(matchingTasks);
        when(taskMapper.toResponseDTO(any(Task.class))).thenReturn(responseDTO);

        // Act
        List<TaskResponseDTO> result = taskService.searchTasks(keyword);

        // Assert
        assertEquals(1, result.size());
        verify(taskRepository, times(1)).findByUserAndTitleContainingIgnoreCase(testUser, keyword);
    }
}
