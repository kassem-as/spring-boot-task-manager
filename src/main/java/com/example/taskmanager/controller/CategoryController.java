package com.example.taskmanager.controller;

import com.example.taskmanager.dto.CategoryRequestDTO;
import com.example.taskmanager.dto.CategoryResponseDTO;
import com.example.taskmanager.dto.TaskResponseDTO;
import com.example.taskmanager.service.CategoryService;
import com.example.taskmanager.service.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
@Tag(name = "Categories", description = "Category management endpoints")
@SecurityRequirement(name = "bearerAuth")
public class CategoryController {

    private final CategoryService categoryService;
    private final TaskService taskService;

    public CategoryController(CategoryService categoryService, TaskService taskService){
        this.categoryService = categoryService;
        this.taskService = taskService;
    }

    @GetMapping
    @Operation(
            summary = "Get all categories",
            description = "Returns all available categories"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved categories"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public List<CategoryResponseDTO> getAllCategories(){
        return categoryService.getAllCategories();
    }

    @GetMapping("/{id}")
    @Operation(
            summary = "Get category by ID",
            description = "Returns a specific category by its ID"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category found"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public CategoryResponseDTO getCategoryById(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id){
        return categoryService.getCategoryById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create new category",
            description = "Creates a new category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Category created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid input data"),
            @ApiResponse(responseCode = "409", description = "Category with this name already exists")
    })
    public CategoryResponseDTO createCategory(
            @Parameter(description = "Category data", required = true)
            @Valid @RequestBody CategoryRequestDTO requestDTO){
        return categoryService.createCategory(requestDTO);
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update category",
            description = "Updates an existing category"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Category updated successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found"),
            @ApiResponse(responseCode = "400", description = "Invalid input data")
    })
    public CategoryResponseDTO updateCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO requestDTO){
        return categoryService.updateCategory(id, requestDTO);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Delete category",
            description = "Deletes a category by ID. All tasks in this category will also be deleted."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Category deleted successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public void deleteCategory(
            @Parameter(description = "Category ID", required = true)
            @PathVariable Long id){
        categoryService.deleteCategory(id);
    }

    @GetMapping("/{id}/tasks")
    @Operation(
            summary = "Get tasks by category",
            description = "Returns all tasks in a specific category for the authenticated user"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Tasks retrieved successfully"),
            @ApiResponse(responseCode = "404", description = "Category not found")
    })
    public List<TaskResponseDTO> getTasksByCategory(@PathVariable Long id){
        return taskService.getTasksByCategory(id);
    }
}
