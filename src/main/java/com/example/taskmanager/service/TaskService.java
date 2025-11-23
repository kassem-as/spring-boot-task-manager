package com.example.taskmanager.service;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.exception.CategoryNotFoundException;
import com.example.taskmanager.exception.TaskNotFoundException;
import com.example.taskmanager.mapper.TaskMapper;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.model.Task;
import com.example.taskmanager.model.User;
import com.example.taskmanager.repository.CategoryRepository;
import com.example.taskmanager.repository.TaskRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TaskService {

    private final TaskRepository taskRepository;
    private final CategoryRepository categoryRepository;
    private final TaskMapper taskMapper;

    public TaskService(TaskRepository taskRepository, CategoryRepository categoryRepository, TaskMapper taskMapper){
        this.taskRepository = taskRepository;
        this.categoryRepository = categoryRepository;
        this.taskMapper = taskMapper;
    }

    private User getCurrentUser() {
        UserDetails userDetails = (UserDetails) SecurityContextHolder.getContext()
                .getAuthentication().getPrincipal();
        return (User) userDetails;
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getAllTasks(){
        User currentUser = getCurrentUser();
        return taskRepository.findByUser(currentUser).stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public TaskResponseDTO getTaskById(Long id){
        User currentUser = getCurrentUser();
        Task task = taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new TaskNotFoundException(id));
        return taskMapper.toResponseDTO(task);
    }

    public TaskResponseDTO createTask(TaskRequestDTO requestDTO){
        User currentUser = getCurrentUser();

        Task task = taskMapper.toEntity(requestDTO);
        task.setUser(currentUser);

        if(requestDTO.getCategoryId() != null){
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(requestDTO.getCategoryId()));
            task.setCategory(category);
        }
        Task savedTask = taskRepository.save(task);
        return taskMapper.toResponseDTO(savedTask);
    }

    public TaskResponseDTO updateTask(Long id, TaskRequestDTO requestDTO){
        User currentUser = getCurrentUser();

        Task existingTask = taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new TaskNotFoundException(id));

        existingTask.setTitle(requestDTO.getTitle());
        existingTask.setDescription(requestDTO.getDescription());
        existingTask.setCompleted(requestDTO.isCompleted());

        if(requestDTO.getCategoryId() != null){
            Category category = categoryRepository.findById(requestDTO.getCategoryId())
                    .orElseThrow(() -> new CategoryNotFoundException(requestDTO.getCategoryId()));
            existingTask.setCategory(category);
        } else {
            existingTask.setCategory(null);
        }

        Task updatedTask = taskRepository.save(existingTask);
        return taskMapper.toResponseDTO(updatedTask);
    }

    public void deleteTask(Long id){
        User currentUser = getCurrentUser();

        Task task = taskRepository.findByIdAndUser(id, currentUser)
                .orElseThrow(() -> new TaskNotFoundException(id));

        taskRepository.delete(task);
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getCompletedTasks(){
        User currentUser = getCurrentUser();
        return taskRepository.findByUserAndCompleted(currentUser,true).stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> searchTasks(String keyword){
        User currentUser = getCurrentUser();
        return taskRepository.findByUserAndTitleContainingIgnoreCase(currentUser, keyword).stream()
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<TaskResponseDTO> getTasksByCategory(Long categoryId){
        User currentUser = getCurrentUser();

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CategoryNotFoundException(categoryId));

        return category.getTasks().stream()
                .filter(task -> task.getUser().equals(currentUser))
                .map(taskMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
}
