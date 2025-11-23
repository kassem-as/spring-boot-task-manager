package com.example.taskmanager.repository;

import com.example.taskmanager.model.Role;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@DisplayName("TaskRepository Tests")
public class TaskRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp(){
        // Test User erstellen
        testUser = new User();
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setPassword("password");
        testUser.setRole(Role.USER);
        testUser = entityManager.persistAndFlush(testUser);
    }

    @Test
    @DisplayName("Sollte Task speichern und laden können")
    void saveAndFindTask() {
        // Arrange
        Task task = new Task();
        task.setTitle("Test Task");
        task.setDescription("Test Description");
        task.setCompleted(false);
        task.setUser(testUser);

        // Act
        Task savedTask = taskRepository.save(task);
        Optional<Task> foundTask = taskRepository.findById(savedTask.getId());

        // Assert
        assertThat(foundTask).isPresent();
        assertThat(foundTask.get().getTitle()).isEqualTo("Test Task");
        assertThat(foundTask.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Sollte alle Tasks eines Users finden")
    void findByUser_ShouldReturnUserTasks(){
        // Arrange
        Task task1 = createTask("Task 1", testUser);
        Task task2 = createTask("Task 2", testUser);

        User otherUser = createUser("other", "other@example.com");
        Task task3 = createTask("Task 3", otherUser);

        entityManager.flush();

        //Act
        List<Task> userTasks = taskRepository.findByUser(testUser);

        // Assert
        assertThat(userTasks).hasSize(2);
        assertThat(userTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Task 1", "Task 2");
    }

    @Test
    @DisplayName("Sollte nur completed Tasks finden")
    void findByUserAndCompleted_ShouldReturnOnlyCompletedTasks() {
        // Arrange
        Task completedTask = createTask("Completed", testUser);
        completedTask.setCompleted(true);
        entityManager.persist(completedTask);

        Task incompleteTask = createTask("Incomplete", testUser);
        incompleteTask.setCompleted(false);
        entityManager.persist(incompleteTask);

        entityManager.flush();

        // Act
        List<Task> completedTasks = taskRepository.findByUserAndCompleted(testUser, true);

        // Assert
        assertThat(completedTasks).hasSize(1);
        assertThat(completedTasks.get(0).isCompleted()).isTrue();
    }

    @Test
    @DisplayName("Sollte Tasks nach Title-Keyword suchen")
    void findByUserAndTitleContaining_ShouldReturnMatchingTasks() {
        // Arrange
        createTask("Spring Boot lernen", testUser);
        createTask("Java Basics", testUser);
        createTask("Spring Security", testUser);
        entityManager.flush();

        // Act
        List<Task> springTasks = taskRepository.findByUserAndTitleContainingIgnoreCase(testUser, "spring");

        // Assert
        assertThat(springTasks).hasSize(2);
        assertThat(springTasks).extracting(Task::getTitle)
                .containsExactlyInAnyOrder("Spring Boot lernen", "Spring Security");
    }

    @Test
    @DisplayName("Sollte Task nach ID und User finden")
    void findByIdAndUser_ShouldReturnTaskIfBelongsToUser() {
        // Arrange
        Task task = createTask("My Task", testUser);
        entityManager.flush();

        User otherUser = createUser("other", "other@example.com");

        // Act
        Optional<Task> found = taskRepository.findByIdAndUser(task.getId(), testUser);
        Optional<Task> notFound = taskRepository.findByIdAndUser(task.getId(), otherUser);

        // Assert
        assertThat(found).isPresent();
        assertThat(notFound).isEmpty();
    }

    @Test
    @DisplayName("Sollte Anzahl der User-Tasks zählen")
    void countByUser_ShouldReturnCorrectCount() {
        // Arrange
        createTask("Task 1", testUser);
        createTask("Task 2", testUser);
        createTask("Task 3", testUser);
        entityManager.flush();

        // Act
        long count = taskRepository.countByUser(testUser);

        // Assert
        assertThat(count).isEqualTo(3);
    }

    // Helper Methods
    private Task createTask(String title, User user){
        Task task = new Task();
        task.setTitle(title);
        task.setDescription("Description");
        task.setCompleted(false);
        task.setUser(user);
        return entityManager.persist(task);
    }

    private User createUser(String username, String email) {
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword("password");
        user.setRole(Role.USER);
        return entityManager.persistAndFlush(user);
    }
}
