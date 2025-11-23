package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.security.JwtAuthenticationFilter;
import com.example.taskmanager.service.TaskService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;

import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;


import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = TaskController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = JwtAuthenticationFilter.class
        ),
        excludeAutoConfiguration = {
                org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class
        }
)
@DisplayName("TaskController Tests")
public class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService taskService;


    private TaskResponseDTO responseDTO;
    private TaskRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        responseDTO = new TaskResponseDTO(
                1L,
                "Test Task",
                "Test Description",
                false,
                LocalDateTime.now(),
                null
        );

        requestDTO = new TaskRequestDTO(
              "Test Task",
              "Test Description",
              false,
              null
        );
    }

    @Test
    @DisplayName("GET /api/tasks - Sollte alle Tasks zurückgeben")
    @WithMockUser
    void getAllTasks_ShouldReturnTaskList() throws Exception{
        // Arrange
        List<TaskResponseDTO> tasks = Arrays.asList(responseDTO);
        when(taskService.getAllTasks()).thenReturn(tasks);

        // Act & Assert
        mockMvc.perform(get("/api/tasks"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].title").value("Test Task"))
                .andExpect(jsonPath("$[0].completed").value(false));
    }

    @Test
    @DisplayName("GET api/tasks/{id} - Sollte Task nach ID zurückgeben")
    @WithMockUser
    void getTaskById_WhenTaskExists_ShouldReturnTask() throws Exception {
        // Arrange
        when(taskService.getTaskById(1L)).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/tasks/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("POST /api/tasks - Sollte neuen Task erstellen")
    @WithMockUser
    void createTask_WithValidData_ShouldReturnCreatedTask() throws Exception {
        // Arrange
        when(taskService.createTask(any(TaskRequestDTO.class))).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(requestDTO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    @DisplayName("POST /api/tasks - Sollte 400 bei ungültigen Daten zurückgeben")
    @WithMockUser
    void createTask_WithInvalidData_ShouldReturnBadRequest() throws Exception {
        // Arrange - Invalider Request (title zu kurz)
        TaskRequestDTO invalidRequest = new TaskRequestDTO("Hi", "Description", false, null);

        // Act & Assert
        mockMvc.perform(post("/api/tasks")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidRequest)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/tasks/{id} - Sollte Task aktualisieren")
    @WithMockUser
    void updateTask_WithValidData_ShouldReturnUpdatedTask() throws Exception {
        // Arrange
        TaskRequestDTO updateRequest = new TaskRequestDTO(
                "Updated Task",
                "Updated Description",
                true,
                null
        );

        TaskResponseDTO updatedResponse = new TaskResponseDTO(
                1L,
                "Updated Task",
                "Updated Description",
                true,
                LocalDateTime.now(),
                null
        );

        when(taskService.updateTask(eq(1L), any(TaskRequestDTO.class))).thenReturn(updatedResponse);

        // Act & Assert
        mockMvc.perform(put("/api/tasks/1")
                .with(csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.completed").value(true));
    }

    @Test
    @DisplayName("DELETE /api/tasks/{id} - Sollte Task löschen")
    @WithMockUser
    void deleteTask_ShouldReturnNoContent() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/tasks/1")
                .with(csrf()))
                .andExpect(status().isNoContent());

    }

    @Test
    @DisplayName("GET /api/tasks/completed - Sollte nur completed Tasks zurückgeben")
    @WithMockUser
    void getCompletedTasks_ShouldReturnCompletedTasks() throws Exception {
        // Arrange
        TaskResponseDTO completedTasks = new TaskResponseDTO(
                1L,
                "Completed Task",
                "Description",
                true,
                LocalDateTime.now(),
                null
        );

        when(taskService.getCompletedTasks()).thenReturn(Arrays.asList(completedTasks));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/completed"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].completed").value(true));
    }

    @Test
    @DisplayName("GET /api/tasks/search - Sollte Tasks nach Keyword suchen")
    @WithMockUser
    void searchTasks_ShouldReturnMatchingTasks() throws Exception {
        // Arrange
        when(taskService.searchTasks("Test")).thenReturn(Arrays.asList(responseDTO));

        // Act & Assert
        mockMvc.perform(get("/api/tasks/search")
                .param("keyword", "Test"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$",hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

}
