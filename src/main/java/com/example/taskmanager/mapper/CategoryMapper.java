package com.example.taskmanager.mapper;

import com.example.taskmanager.dto.CategoryRequestDTO;
import com.example.taskmanager.dto.CategoryResponseDTO;
import com.example.taskmanager.model.Category;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {

    public Category toEntity(CategoryRequestDTO dto){
        Category category = new Category();
        category.setName(dto.getName());
        category.setDescription(dto.getDescription());
        return category;
    }

    public CategoryResponseDTO toResponseDTO(Category category){
        return new CategoryResponseDTO(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getTasks() != null ? category.getTasks().size() : 0
        );
    }
}
