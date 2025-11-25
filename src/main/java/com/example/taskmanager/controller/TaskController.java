package com.example.taskmanager.controller;

import com.example.taskmanager.dto.TaskRequestDTO;
import com.example.taskmanager.dto.TaskResponseDTO;

import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;


import java.util.List;


@RestController
@RequestMapping("/api/tasks")
@Tag(name = "Tasks", description = "Task Management Endpoints")
@SecurityRequirement(name = "bearerAuth")
public class TaskController {

    private final TaskService taskService;

    public TaskController(TaskService taskService){
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(
            summary = "Get all tasks",
            description = "Returns all tasks for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Successfully retrieved tasks"
            ),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public List<TaskResponseDTO> getAllTasks() {
        return taskService.getAllTasks();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get task by ID",
            description = "Returns a specific task by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task found"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public TaskResponseDTO getTaskById(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long id){
        return taskService.getTaskById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new task",
            description = "Creates a new task for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Task created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public TaskResponseDTO createTask(
            @Parameter(description = "Task Daten", required = true)
            @Valid @RequestBody TaskRequestDTO requestDTO){
        return taskService.createTask(requestDTO);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update task",
            description = "Updates an existing task"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Task updated successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public TaskResponseDTO updateTask(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody TaskRequestDTO requestDTO){
        return taskService.updateTask(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete task",
            description = "Deletes a task by ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public void deleteTask(
            @Parameter(description = "Task ID", required = true)
            @PathVariable Long id){
         taskService.deleteTask(id);

    }

    @GetMapping("/completed")
    @Operation(
            summary = "Get completed tasks",
            description = "Returns all completed tasks for the authenticated user"
    )

    public List<TaskResponseDTO> getCompletedTasks(){
        return taskService.getCompletedTasks();
    }

    @GetMapping("/search")
    @Operation(
            summary = "Search tasks",
            description = "Search tasks by keyword in title"
    )
    public List<TaskResponseDTO> searchTasks(
            @Parameter(description = "Search keyword", required = true)
            @RequestParam String keyword){
        return taskService.searchTasks(keyword);
    }


}
