package com.projectsystem.model.entity;

import com.projectsystem.model.enums.ProjectStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность проекта.
 * Хранит информацию о проекте и его участниках.
 *
 * @author Евдокимов Д.А.
 */
@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"manager", "members", "tasks"})
@ToString(exclude = {"manager", "members", "tasks"})
public class Project {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private User manager;

    @Column
    private LocalDate startDate;

    @Column
    private LocalDate endDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status = ProjectStatus.ACTIVE;

    @Column
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Task> tasks = new HashSet<>();

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ProjectMember> members = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}