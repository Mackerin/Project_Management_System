package com.projectsystem.model.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * DTO для запроса двухфакторной аутентификации.
 *
 * @author Евдокимов Д.А.
 */
@Data
public class TwoFaRequest {

    @NotBlank(message = "Код подтверждения обязателен")
    @Size(min = 6, max = 6, message = "Код должен содержать 6 цифр")
    private String code;

    private Long userId;
}