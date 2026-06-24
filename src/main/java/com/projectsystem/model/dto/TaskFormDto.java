package com.projectsystem.model.dto;

import com.projectsystem.model.enums.TaskStatus;
import com.projectsystem.model.enums.TaskPriority;
import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;

/**
 * DTO для формы создания/редактирования задачи.
 *
 * @author Евдокимов Д.А.
 */
@Data
public class TaskFormDto {

    private Long id;

    @NotBlank(message = "Заголовок обязателен")
    @Size(max = 200, message = "Заголовок не должен превышать 200 символов")
    private String title;

    @Size(max = 5000, message = "Описание не должно превышать 5000 символов")
    private String description;

    private TaskStatus status;

    private TaskPriority priority;

    private Long projectId;

    private Long executorId;

    private LocalDate deadline;
}