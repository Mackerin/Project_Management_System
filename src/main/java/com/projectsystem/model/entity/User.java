package com.projectsystem.model.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Сущность пользователя системы.
 * Хранит учетные данные и информацию о пользователе.
 *
 * @author Евдокимов Д.А.
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(exclude = {"roles", "projects"})
@ToString(exclude = {"roles", "projects"})
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private Boolean isEnabled = true;

    @Column(length = 10)
    private String twoFaCode;

    @Column
    private LocalDateTime twoFaCodeExpiration;

    @Column
    private LocalDateTime createdAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles = new HashSet<>();

    @OneToMany(mappedBy = "manager", cascade = CascadeType.ALL)
    private Set<Project> managedProjects = new HashSet<>();

    @OneToMany(mappedBy = "creator", cascade = CascadeType.ALL)
    private Set<Task> createdTasks = new HashSet<>();

    @OneToMany(mappedBy = "executor", cascade = CascadeType.ALL)
    private Set<Task> assignedTasks = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}