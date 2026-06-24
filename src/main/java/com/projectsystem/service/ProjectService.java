package com.projectsystem.service;

import com.projectsystem.model.entity.Project;
import com.projectsystem.model.entity.User;
import com.projectsystem.model.enums.ProjectStatus;
import com.projectsystem.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления проектами.
 *
 * @author Евдокимов Д.А.
 */
@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;

    @Transactional(readOnly = true)
    public Optional<Project> findById(Long id) {
        return projectRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Project> findAll() {
        return projectRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Project> findByManager(User manager) {
        return projectRepository.findByManager(manager);
    }

    @Transactional(readOnly = true)
    public List<Project> findByStatus(ProjectStatus status) {
        return projectRepository.findByStatus(status);
    }

    @Transactional
    public Project createProject(String name, String description, User manager,
                                 LocalDate startDate, LocalDate endDate) {
        Project project = new Project();
        project.setName(name);
        project.setDescription(description);
        project.setManager(manager);
        project.setStartDate(startDate);
        project.setEndDate(endDate);
        project.setStatus(ProjectStatus.ACTIVE);

        return projectRepository.save(project);
    }

    @Transactional
    public Project updateProject(Long id, String name, String description,
                                 LocalDate startDate, LocalDate endDate) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        project.setName(name);
        project.setDescription(description);
        project.setStartDate(startDate);
        project.setEndDate(endDate);

        return projectRepository.save(project);
    }

    @Transactional
    public void deleteProject(Long id) {
        projectRepository.deleteById(id);
    }

    @Transactional
    public Project closeProject(Long id) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));
        project.setStatus(ProjectStatus.CLOSED);
        return projectRepository.save(project);
    }
}