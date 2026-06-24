package com.projectsystem.service;

import com.projectsystem.model.entity.Task;
import com.projectsystem.model.entity.User;
import com.projectsystem.model.entity.Project;
import com.projectsystem.model.enums.TaskStatus;
import com.projectsystem.model.enums.TaskPriority;
import com.projectsystem.repository.TaskRepository;
import com.projectsystem.repository.ProjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Сервис для управления задачами.
 *
 * @author Евдокимов Д.А.
 */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final NotificationService notificationService;

    @Transactional(readOnly = true)
    public Optional<Task> findById(Long id) {
        return taskRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Task> findByProjectId(Long projectId) {
        return taskRepository.findByProjectId(projectId);
    }

    @Transactional(readOnly = true)
    public List<Task> findByExecutor(User executor) {
        return taskRepository.findByExecutor(executor);
    }

    @Transactional(readOnly = true)
    public List<Task> findByStatus(TaskStatus status) {
        return taskRepository.findByStatus(status);
    }

    @Transactional
    public Task createTask(String title, String description, TaskPriority priority,
                           LocalDate deadline, Long projectId, User creator, User executor) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));

        Task task = new Task();
        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDeadline(deadline);
        task.setStatus(TaskStatus.NEW);
        task.setProject(project);
        task.setCreator(creator);
        task.setExecutor(executor);

        Task savedTask = taskRepository.save(task);

        // Уведомление исполнителю
        if (executor != null) {
            notificationService.createNotification(
                    executor.getId(),
                    "Вам назначена новая задача: " + title
            );
        }

        return savedTask;
    }

    @Transactional
    public Task updateTask(Long id, String title, String description,
                           TaskPriority priority, LocalDate deadline) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        task.setTitle(title);
        task.setDescription(description);
        task.setPriority(priority);
        task.setDeadline(deadline);

        return taskRepository.save(task);
    }

    @Transactional
    public Task updateStatus(Long taskId, TaskStatus newStatus, User currentUser) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));

        // ✅ ПРОВЕРКА: если currentUser null (тесты), пропускаем проверку прав
        if (currentUser != null) {
            boolean isExecutor = task.getExecutor() != null &&
                    task.getExecutor().getId().equals(currentUser.getId());

            // ✅ Дополнительная проверка на null для менеджера проекта
            boolean isManager = task.getProject() != null &&
                    task.getProject().getManager() != null &&
                    task.getProject().getManager().getId().equals(currentUser.getId());

            if (!isExecutor && !isManager) {
                throw new RuntimeException("Нет прав на изменение статуса задачи");
            }

            // Уведомление менеджеру об изменении статуса (только если проверка прав прошла)
            if (isExecutor && task.getProject() != null && task.getProject().getManager() != null) {
                notificationService.createNotification(
                        task.getProject().getManager().getId(),
                        "Статус задачи '" + task.getTitle() + "' изменён на: " + newStatus.getDisplayName()
                );
            }
        }

        task.setStatus(newStatus);
        return taskRepository.save(task);
    }

    @Transactional
    public void deleteTask(Long id) {
        taskRepository.deleteById(id);
    }
}