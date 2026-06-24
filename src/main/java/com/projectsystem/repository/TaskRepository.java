package com.projectsystem.repository;

import com.projectsystem.model.entity.Task;
import com.projectsystem.model.entity.User;
import com.projectsystem.model.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

/**
 * Репозиторий для работы с задачами.
 *
 * @author Евдокимов Д.А.
 */
@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByExecutor(User executor);

    List<Task> findByCreator(User creator);

    List<Task> findByStatus(TaskStatus status);

    List<Task> findByProjectId(Long projectId);

    List<Task> findByExecutorAndStatus(User executor, TaskStatus status);
}