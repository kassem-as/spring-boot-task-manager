package com.example.taskmanager.integration;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("Task Integration Test")
class TaskIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        // User erstellen und einloggen
        User user = new User();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPassword(passwordEncoder.encode("password123"));
        user.setRole(Role.USER);
        userRepository.save(user);

        // JWT Token holen
        String loginRequest = """
                {
                    "username": "testuser",
                    "password": "password123"
                }
                """;

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginRequest))
                .andExpect(status().isOk())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        jwtToken = objectMapper.readTree(response).get("token").asText();
    }

    @Test
    @DisplayName("Vollständiger CRUD-Flow sollte funktionieren")
    void completeCrudFlow() throws Exception {
        // 1. Task erstellen
        TaskRequestDTO createRequest = new TaskRequestDTO(
                "Integration Test Task",
                "Testing full flow",
                false,
                null
        );

        MvcResult createResult = mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Test Task"))
                .andReturn();

        String createResponse = createResult.getResponse().getContentAsString();
        Long taskId = objectMapper.readTree(createResponse).get("id").asLong();

        // 2. Task abrufen
        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(taskId))
                .andExpect(jsonPath("$.title").value("Integration Test Task"));

        // 3. Alle Tasks abrufen
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));

        // 4. Task aktualisieren
        TaskRequestDTO updateRequest = new TaskRequestDTO(
                "Updated Task",
                "Updated Description",
                true,
                null
        );

        mockMvc.perform(put("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"))
                .andExpect(jsonPath("$.completed").value(true));

        // 5. Task löschen
        mockMvc.perform(delete("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNoContent());

        // 6. Prüfen, dass Task gelöscht wurde
        mockMvc.perform(get("/api/tasks/" + taskId)
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("User sollte nur eigene Tasks sehen")
    void userShouldOnlySeeOwnTasks() throws Exception {
        // User 1 erstellt Task
        TaskRequestDTO task1 = new TaskRequestDTO("User 1 Task", "Description", false, null);

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated());

        // User 2 erstellen und einloggen
        User user2 = new User();
        user2.setUsername("testuser2");
        user2.setEmail("test2@example.com");
        user2.setPassword(passwordEncoder.encode("password123"));
        user2.setRole(Role.USER);
        userRepository.save(user2);

        String login2Request = """
                {
                    "username": "testuser2",
                    "password": "password123"
                }
                """;

        MvcResult result2 = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login2Request))
                .andExpect(status().isOk())
                .andReturn();

        String response2 = result2.getResponse().getContentAsString();
        String jwtToken2 = objectMapper.readTree(response2).get("token").asText();

        // User 2 sollte keine Tasks sehen
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken2))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(0)));

        // User 1 sollte seinen Task sehen
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("Zugriff ohne Token sollte 403 zurückgeben")
    void accessWithoutToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Zugriff mit ungültigem Token sollte 403 zurückgeben")
    void accessWithInvalidToken_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/tasks")
                .header("Authorization", "Bearer invalid.token.here"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("Search sollte Tasks filtern")
    void searchShouldFilterTasks() throws Exception {
        // Mehrere Tasks erstellen
        TaskRequestDTO task1 = new TaskRequestDTO("Spring Boot lernen", "Description", false, null);
        TaskRequestDTO task2 = new TaskRequestDTO("Java Basics", "Description", false, null);
        TaskRequestDTO task3 = new TaskRequestDTO("Spring Security", "Description", false, null);

        mockMvc.perform(post("/api/tasks")
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task3)))
                .andExpect(status().isCreated());

        // Nach "Spring" suchen
        mockMvc.perform(get("/api/tasks/search")
                .param("keyword", "Spring")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].title").value("Spring Boot lernen"))
                .andExpect(jsonPath("$[1].title").value("Spring Security"));
    }

    @Test
    @DisplayName("Completed Tasks sollte nur abgeschlossene Tasks zurückgeben")
    void completedTasksShouldReturnOnlyCompletedTasks() throws Exception {
        // Task 1 - nicht abgeschlossen
        TaskRequestDTO task1 = new TaskRequestDTO("Task 1", "Description", false, null);
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task1)))
                .andExpect(status().isCreated());

        // Task 2 - abgeschlossen
        TaskRequestDTO task2 = new TaskRequestDTO("Task 2", "Description", true, null);
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task2)))
                .andExpect(status().isCreated());

        // Task 3 - abgeschlossen
        TaskRequestDTO task3 = new TaskRequestDTO("Task 3", "Description", true, null);
        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(task3)))
                .andExpect(status().isCreated());

        // Nur completed Tasks abrufen
        mockMvc.perform(get("/api/tasks/completed")
                .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("Validierung sollte invalide Requests ablehnen")
    void validationShouldRejectInvalidRequests() throws Exception {
        // Title zu kurz
        TaskRequestDTO invalidTask = new TaskRequestDTO("Hi", "Description", false, null);

        mockMvc.perform(post("/api/tasks")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidTask)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.title").exists());
    }



}
