package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CategoryRequestDTO;
import com.example.taskmanager.dto.CategoryResponseDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private final CategoryService categoryService;
    private final TaskService taskService;

    public CategoryController(CategoryService categoryService, TaskService taskService){
        this.categoryService = categoryService;
        this.taskService = taskService;
    }

    @GetMapping
    public List<CategoryResponseDTO> getAllCategories(){
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    public CategoryResponseDTO getCategoryById(@PathVariable Long id){
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryResponseDTO createCategory(@Valid @RequestBody CategoryRequestDTO requestDTO){
        return categoryService.createCategory(requestDTO);
    }

    @PutMapping("/{id}")
    public CategoryResponseDTO updateCategory(@PathVariable Long id, @Valid @RequestBody CategoryRequestDTO requestDTO){
        return categoryService.updateCategory(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategory(@PathVariable Long id){
        categoryService.deleteCategory(id);
    }

    @GetMapping("/{id}/tasks")
    public List<TaskResponseDTO> getTasksByCategory(@PathVariable Long id){
        return taskService.getTasksByCategory(id);
    }
}
