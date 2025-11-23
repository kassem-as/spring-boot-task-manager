package com.example.taskmanager.service;

import com.example.taskmanager.dto.CategoryRequestDTO;
import com.example.taskmanager.dto.CategoryResponseDTO;
import com.example.taskmanager.exception.CategoryNotFoundException;
import com.example.taskmanager.mapper.CategoryMapper;
import com.example.taskmanager.model.Category;
import com.example.taskmanager.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;

    public CategoryService(CategoryRepository categoryRepository, CategoryMapper categoryMapper){
        this.categoryRepository = categoryRepository;
        this.categoryMapper = categoryMapper;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponseDTO> getAllCategories(){
        return categoryRepository.findAll().stream()
                .map(categoryMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CategoryResponseDTO getCategoryById(Long id){
        Category category = categoryRepository.findById(id)
                .orElseThrow(()-> new CategoryNotFoundException(id));
        return categoryMapper.toResponseDTO(category);
    }

    public CategoryResponseDTO createCategory(CategoryRequestDTO requestDTO){
        if(categoryRepository.existsByName(requestDTO.getName())){
            throw new RuntimeException("Category mit dem Namen '" + requestDTO.getName() + "' existiert bereits");
        }

        Category category = categoryMapper.toEntity(requestDTO);
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponseDTO(savedCategory);
    }

    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO requestDTO){
        Category existingCategory = categoryRepository.findById(id)
                .orElseThrow(() -> new CategoryNotFoundException(id));

        if(!existingCategory.getName().equals(requestDTO.getName())
        && categoryRepository.existsByName(requestDTO.getName())){
            throw new RuntimeException("Category mit dem Namen '" + requestDTO.getName() + "' existiert bereits");
        }

        existingCategory.setName(requestDTO.getName());
        existingCategory.setDescription(requestDTO.getDescription());

        Category updatedCategory = categoryRepository.save(existingCategory);
        return categoryMapper.toResponseDTO(updatedCategory);
    }

    public void deleteCategory(Long id){
        if(!categoryRepository.existsById(id)){
            throw new CategoryNotFoundException(id);
        }
        categoryRepository.deleteById(id);
    }
}
