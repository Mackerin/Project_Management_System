package com.projectsystem.repository;

import com.projectsystem.model.entity.Project;
import com.projectsystem.model.entity.User;
import com.projectsystem.model.enums.ProjectStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Репозиторий для работы с проектами.
 *
 * @author Евдокимов Д.А.
 */
@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByManager(User manager);

    List<Project> findByStatus(ProjectStatus status);

    List<Project> findByNameContainingIgnoreCase(String name);
}