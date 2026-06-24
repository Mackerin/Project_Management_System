package com.projectsystem.model.entity;

import com.projectsystem.model.enums.TaskStatus;
import com.projectsystem.model.enums.TaskPriority;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность задачи.
 * Хранит информацию о задаче в рамках проекта.
 *
 * @author Евдокимов Д.А.
 */
@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"project", "creator", "executor", "comments"})
@ToString(exclude = {"project", "creator", "executor", "comments"})
public class Task {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status = TaskStatus.NEW;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority = TaskPriority.MEDIUM;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "executor_id")
    private User executor;

    @Column
    private LocalDate deadline;

    @Column
    private LocalDateTime createdAt;

    @Column
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "task", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Comment> comments = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}