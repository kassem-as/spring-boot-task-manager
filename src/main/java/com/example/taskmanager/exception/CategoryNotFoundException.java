package com.example.taskmanager.exception;

public class CategoryNotFoundException extends RuntimeException {

    public CategoryNotFoundException(Long id){
        super("Category mit ID " + id + " wurde nicht gefunden");
    }

    public CategoryNotFoundException(String message){
        super(message);
    }
}
