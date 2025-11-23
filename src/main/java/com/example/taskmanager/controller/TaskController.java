package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;

import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/tasks")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping
    public List<TaskResponseDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    public TaskResponseDTO getTaskById(@PathVariable Long id){
        return taskService.getTaskById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponseDTO createTask(@Valid @RequestBody TaskRequestDTO requestDTO){
        return taskService.createTask(requestDTO);
    }

    @PutMapping("/{id}")
    public TaskResponseDTO updateTask(@PathVariable Long id, @Valid @RequestBody TaskRequestDTO requestDTO){
        return taskService.updateTask(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id){
         taskService.deleteTask(id);

    }

    @GetMapping("/completed")
    public List<TaskResponseDTO> getCompletedTasks(){
        return taskService.getCompletedTasks();
    }

    @GetMapping("/search")
    public List<TaskResponseDTO> searchTasks(@RequestParam String keyword){
        return taskService.searchTasks(keyword);
    }


}
