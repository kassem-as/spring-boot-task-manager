package com.example.taskmanager.repository;

import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByUser(User user);

    List<Task> findByUserAndCompleted(User user, boolean completed);

    List<Task> findByUserAndTitleContainingIgnoreCase(User user, String keyword);

    Optional<Task> findByIdAndUser(Long id, User user);

    @Query("SELECT t from Task t WHERE t.user = :user AND t.completed = true ORDER BY t.createdAt DESC")
    List<Task> findCompletedTasksByUser(User user);

    long countByUser(User user);

    long countByUserAndCompleted(User user, boolean completed);

//    List<Task> findByCompleted(boolean completed);
//
//    List<Task> findByTitleContainingIgnoreCase(String keyword);
//
//    Optional<Task> findByTitle(String title);
//
//    @Query("SELECT t FROM Task t WHERE t.completed = true ORDER BY t.createdAt DESC ")
//    List<Task> findCompletedTasksOrderedByDate();
//
//    @Query(value = "SELECT * FROM tasks WHERE title LIKE %:keyword%", nativeQuery = true)
//    List<Task> searchByTitleNative(String keyword);
//
//    long countByCompleted(boolean completed);
//
//    boolean existsByTitle(String title);
}
