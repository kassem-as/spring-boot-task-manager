package com.example.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TaskRequestDTO {

    @NotBlank(message = "Title darf nicht leer sein")
    @Size(min = 3, max = 100, message = "Title muss zwischen 3 und 100 Zeichen lang sein")
    private String title;

    @Size(max = 500, message = "Description darf maximal 500 Zeichen lang sein")
    private String description;

    private boolean completed;

    private Long categoryId;
}
