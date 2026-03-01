package com.example.taskmanager.exception;

public class InvalidVerificationTokenException extends RuntimeException{
    public InvalidVerificationTokenException(String message) {
        super(message);
    }
}
