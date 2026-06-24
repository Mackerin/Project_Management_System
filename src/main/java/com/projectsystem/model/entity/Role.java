package com.projectsystem.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Сущность роли пользователя.
 * Определяет права доступа в системе.
 *
 * @author Евдокимов Д.А.
 */
@Entity
@Table(name = "roles")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"users"})
@ToString(exclude = {"users"})
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;

    /**
     * Стандартные роли системы
     */
    public static final String ROLE_MANAGER = "ROLE_MANAGER";
    public static final String ROLE_EXECUTOR = "ROLE_EXECUTOR";
    public static final String ROLE_OBSERVER = "ROLE_OBSERVER";
}