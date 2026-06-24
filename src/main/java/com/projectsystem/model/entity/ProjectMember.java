package com.projectsystem.model.entity;

import com.projectsystem.model.enums.ProjectRole;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Сущность участника проекта.
 * Определяет роль пользователя в конкретном проекте.
 *
 * @author Евдокимов Д.А.
 */
@Entity
@Table(name = "project_members")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectRole roleInProject;
}