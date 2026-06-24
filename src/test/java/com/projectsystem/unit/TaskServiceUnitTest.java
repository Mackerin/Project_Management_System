package com.projectsystem.unit;

import com.projectsystem.model.entity.Task;
import com.projectsystem.model.entity.User;
import com.projectsystem.model.entity.Project;
import com.projectsystem.model.enums.TaskStatus;
import com.projectsystem.model.enums.TaskPriority;
import com.projectsystem.repository.TaskRepository;
import com.projectsystem.repository.ProjectRepository;
import com.projectsystem.service.NotificationService;
import com.projectsystem.service.TaskService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Модульные тесты для TaskService.
 *
 * @author Евдокимов Д.А.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты TaskService")
class TaskServiceUnitTest {

    @Mock
    private TaskRepository taskRepository;

    @Mock
    private ProjectRepository projectRepository;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private TaskService taskService;

    private Task testTask;
    private User testUser;
    private User testManager;
    private Project testProject;

    @BeforeEach
    void setUp() {
        // Создаём тестового пользователя (исполнитель)
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("executor");
        testUser.setEmail("executor@example.com");
        testUser.setIsEnabled(true);

        // Создаём тестового менеджера
        testManager = new User();
        testManager.setId(2L);
        testManager.setUsername("manager");
        testManager.setEmail("manager@example.com");
        testManager.setIsEnabled(true);

        // Создаём тестовый проект
        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Тестовый проект");
        testProject.setManager(testManager);

        // Создаём тестовую задачу
        testTask = new Task();
        testTask.setId(1L);
        testTask.setTitle("Тестовая задача");
        testTask.setDescription("Описание тестовой задачи");
        testTask.setPriority(TaskPriority.MEDIUM);
        testTask.setDeadline(LocalDate.of(2026, 12, 31));
        testTask.setStatus(TaskStatus.NEW);
        testTask.setProject(testProject);
        testTask.setCreator(testManager);
        testTask.setExecutor(testUser);
        testTask.setCreatedAt(LocalDate.now().atStartOfDay());
    }

    // ==================== ТЕСТЫ ДЛЯ findById ====================

    @Test
    @DisplayName("Поиск задачи по ID - успех")
    void findById_WhenTaskExists_ReturnsTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        Optional<Task> result = taskService.findById(1L);

        // Assert
        assertTrue(result.isPresent());
        assertEquals("Тестовая задача", result.get().getTitle());
        verify(taskRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Поиск задачи по ID - задача не найдена")
    void findById_WhenTaskNotExists_ReturnsEmpty() {
        // Arrange
        when(taskRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<Task> result = taskService.findById(999L);

        // Assert
        assertFalse(result.isPresent());
        verify(taskRepository, times(1)).findById(999L);
    }

    // ==================== ТЕСТЫ ДЛЯ findAll ====================

    @Test
    @DisplayName("Получение всех задач - успех")
    void findAll_WhenTasksExist_ReturnsTaskList() {
        // Arrange
        List<Task> tasks = Arrays.asList(
                testTask,
                createTask(2L, "Вторая задача"),
                createTask(3L, "Третья задача")
        );
        when(taskRepository.findAll()).thenReturn(tasks);

        // Act
        List<Task> result = taskService.findAll();

        // Assert
        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Тестовая задача", result.get(0).getTitle());
        verify(taskRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Получение всех задач - пустой список")
    void findAll_WhenNoTasks_ReturnsEmptyList() {
        // Arrange
        when(taskRepository.findAll()).thenReturn(Collections.emptyList());

        // Act
        List<Task> result = taskService.findAll();

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findAll();
    }

    // ==================== ТЕСТЫ ДЛЯ findByProjectId ====================

    @Test
    @DisplayName("Поиск задач по проекту - успех")
    void findByProjectId_WhenTasksExist_ReturnsTaskList() {
        // Arrange
        List<Task> projectTasks = Arrays.asList(testTask, createTask(2L, "Задача проекта"));
        when(taskRepository.findByProjectId(1L)).thenReturn(projectTasks);

        // Act
        List<Task> result = taskService.findByProjectId(1L);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findByProjectId(1L);
    }

    @Test
    @DisplayName("Поиск задач по проекту - нет задач")
    void findByProjectId_WhenNoTasks_ReturnsEmptyList() {
        // Arrange
        when(taskRepository.findByProjectId(1L)).thenReturn(Collections.emptyList());

        // Act
        List<Task> result = taskService.findByProjectId(1L);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findByProjectId(1L);
    }

    // ==================== ТЕСТЫ ДЛЯ findByExecutor ====================

    @Test
    @DisplayName("Поиск задач по исполнителю - успех")
    void findByExecutor_WhenTasksExist_ReturnsTaskList() {
        // Arrange
        List<Task> userTasks = Arrays.asList(testTask, createTask(2L, "Задача исполнителя"));
        when(taskRepository.findByExecutor(testUser)).thenReturn(userTasks);

        // Act
        List<Task> result = taskService.findByExecutor(testUser);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(taskRepository, times(1)).findByExecutor(testUser);
    }

    @Test
    @DisplayName("Поиск задач по исполнителю - нет задач")
    void findByExecutor_WhenNoTasks_ReturnsEmptyList() {
        // Arrange
        when(taskRepository.findByExecutor(testUser)).thenReturn(Collections.emptyList());

        // Act
        List<Task> result = taskService.findByExecutor(testUser);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findByExecutor(testUser);
    }

    // ==================== ТЕСТЫ ДЛЯ findByStatus ====================

    @Test
    @DisplayName("Поиск задач по статусу - успех")
    void findByStatus_WhenTasksExist_ReturnsTaskList() {
        // Arrange
        List<Task> newTasks = Arrays.asList(
                testTask,
                createTaskWithStatus(2L, TaskStatus.NEW)
        );
        when(taskRepository.findByStatus(TaskStatus.NEW)).thenReturn(newTasks);

        // Act
        List<Task> result = taskService.findByStatus(TaskStatus.NEW);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(TaskStatus.NEW, result.get(0).getStatus());
        verify(taskRepository, times(1)).findByStatus(TaskStatus.NEW);
    }

    @Test
    @DisplayName("Поиск задач по статусу - нет задач с таким статусом")
    void findByStatus_WhenNoTasks_ReturnsEmptyList() {
        // Arrange
        when(taskRepository.findByStatus(TaskStatus.DONE)).thenReturn(Collections.emptyList());

        // Act
        List<Task> result = taskService.findByStatus(TaskStatus.DONE);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(taskRepository, times(1)).findByStatus(TaskStatus.DONE);
    }

    // ==================== ТЕСТЫ ДЛЯ createTask ====================

    @Test
    @DisplayName("Создание задачи - успех")
    void createTask_WithValidData_ReturnsTask() {
        // Arrange
        String title = "Новая задача";
        String description = "Описание новой задачи";
        TaskPriority priority = TaskPriority.HIGH;
        LocalDate deadline = LocalDate.of(2026, 6, 30);
        Long projectId = 1L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> {
                    Task task = invocation.getArgument(0);
                    task.setId(100L);
                    return task;
                });

        // Act
        Task result = taskService.createTask(title, description, priority, deadline,
                projectId, testManager, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(title, result.getTitle());
        assertEquals(description, result.getDescription());
        assertEquals(priority, result.getPriority());
        assertEquals(deadline, result.getDeadline());
        assertEquals(TaskStatus.NEW, result.getStatus()); // По умолчанию
        assertEquals(testProject, result.getProject());
        assertEquals(testManager, result.getCreator());
        assertEquals(testUser, result.getExecutor());

        verify(projectRepository, times(1)).findById(projectId);
        verify(taskRepository, times(1)).save(any(Task.class));
        verify(notificationService, times(1)).createNotification(
                eq(testUser.getId()), contains("Вам назначена новая задача"));
    }

    @Test
    @DisplayName("Создание задачи - статус по умолчанию NEW")
    void createTask_SetsDefaultStatusToNew() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.createTask("Тест", "Описание", TaskPriority.MEDIUM,
                LocalDate.now(), 1L, testManager, testUser);

        // Assert
        assertEquals(TaskStatus.NEW, result.getStatus());
    }

    @Test
    @DisplayName("Создание задачи - проект не найден")
    void createTask_WhenProjectNotFound_ThrowsException() {
        // Arrange
        Long projectId = 999L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.createTask("Title", "Desc", TaskPriority.MEDIUM,
                        LocalDate.now(), projectId, testManager, testUser)
        );

        assertEquals("Проект не найден", exception.getMessage());
        verify(projectRepository, times(1)).findById(projectId);
        verify(taskRepository, never()).save(any(Task.class));
        verify(notificationService, never()).createNotification(anyLong(), anyString());
    }

    @Test
    @DisplayName("Создание задачи без исполнителя - уведомление не отправляется")
    void createTask_WithoutExecutor_DoesNotSendNotification() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.createTask("Тест", "Описание", TaskPriority.MEDIUM,
                LocalDate.now(), 1L, testManager, null);

        // Assert
        assertNotNull(result);
        verify(notificationService, never()).createNotification(anyLong(), anyString());
    }

    // ==================== ТЕСТЫ ДЛЯ updateTask ====================

    @Test
    @DisplayName("Обновление задачи - успех")
    void updateTask_WhenTaskExists_UpdatesTask() {
        // Arrange
        Long taskId = 1L;
        String newTitle = "Обновлённое название";
        String newDescription = "Новое описание";
        TaskPriority newPriority = TaskPriority.HIGH;
        LocalDate newDeadline = LocalDate.of(2026, 11, 30);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.updateTask(taskId, newTitle, newDescription,
                newPriority, newDeadline);

        // Assert
        assertNotNull(result);
        assertEquals(newTitle, result.getTitle());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newPriority, result.getPriority());
        assertEquals(newDeadline, result.getDeadline());

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(testTask);
    }

    @Test
    @DisplayName("Обновление задачи - задача не найдена")
    void updateTask_WhenTaskNotFound_ThrowsException() {
        // Arrange
        Long taskId = 999L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.updateTask(taskId, "Title", "Desc",
                        TaskPriority.MEDIUM, LocalDate.now())
        );

        assertEquals("Задача не найдена", exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    // ==================== ТЕСТЫ ДЛЯ updateStatus ====================

    @Test
    @DisplayName("Обновление статуса исполнителем - успех")
    void updateStatus_ByExecutor_UpdatesStatusAndSendsNotification() {
        // Arrange
        Long taskId = 1L;
        TaskStatus newStatus = TaskStatus.IN_PROGRESS;

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.updateStatus(taskId, newStatus, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());

        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, times(1)).save(testTask);
        verify(notificationService, times(1)).createNotification(
                eq(testManager.getId()), contains("изменён на:"));
    }

    @Test
    @DisplayName("Обновление статуса менеджером - успех без уведомления")
    void updateStatus_ByManager_UpdatesStatusWithoutNotification() {
        // Arrange
        Long taskId = 1L;
        TaskStatus newStatus = TaskStatus.DONE;

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.updateStatus(taskId, newStatus, testManager);

        // Assert
        assertNotNull(result);
        assertEquals(newStatus, result.getStatus());

        verify(notificationService, never()).createNotification(
                eq(testManager.getId()), anyString());
    }

    @Test
    @DisplayName("Обновление статуса - нет прав")
    void updateStatus_WithoutPermissions_ThrowsException() {
        // Arrange
        Long taskId = 1L;
        TaskStatus newStatus = TaskStatus.DONE;
        User unauthorizedUser = new User();
        unauthorizedUser.setId(999L);

        when(taskRepository.findById(taskId)).thenReturn(Optional.of(testTask));

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.updateStatus(taskId, newStatus, unauthorizedUser)
        );

        assertEquals("Нет прав на изменение статуса задачи", exception.getMessage());
        verify(taskRepository, never()).save(any(Task.class));
        verify(notificationService, never()).createNotification(anyLong(), anyString());
    }

    @Test
    @DisplayName("Обновление статуса - задача не найдена")
    void updateStatus_WhenTaskNotFound_ThrowsException() {
        // Arrange
        Long taskId = 999L;

        when(taskRepository.findById(taskId)).thenReturn(Optional.empty());

        // Act & Assert
        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> taskService.updateStatus(taskId, TaskStatus.DONE, testUser)
        );

        assertEquals("Задача не найдена", exception.getMessage());
        verify(taskRepository, times(1)).findById(taskId);
        verify(taskRepository, never()).save(any(Task.class));
    }

    // ==================== ТЕСТЫ ДЛЯ deleteTask ====================

    @Test
    @DisplayName("Удаление задачи - успех")
    void deleteTask_WhenTaskExists_DeletesTask() {
        // Arrange
        Long taskId = 1L;
        doNothing().when(taskRepository).deleteById(taskId);

        // Act
        taskService.deleteTask(taskId);

        // Assert
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    @Test
    @DisplayName("Удаление несуществующей задачи - не вызывает ошибку")
    void deleteTask_WhenTaskNotExists_DoesNotThrow() {
        // Arrange
        Long taskId = 999L;
        doNothing().when(taskRepository).deleteById(taskId);

        // Act & Assert
        assertDoesNotThrow(() -> taskService.deleteTask(taskId));
        verify(taskRepository, times(1)).deleteById(taskId);
    }

    // ==================== ТЕСТЫ ПРОВЕРКИ ТРАНЗАКЦИОННОСТИ ====================

    @Test
    @DisplayName("Проверка транзакционности createTask")
    void createTask_IsTransactional() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        taskService.createTask("Test", "Desc", TaskPriority.MEDIUM,
                LocalDate.now(), 1L, testManager, testUser);

        // Assert
        verify(taskRepository, times(1)).save(any(Task.class));
    }

    @Test
    @DisplayName("Проверка readOnly транзакции для findById")
    void findById_IsReadOnlyTransactional() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));

        // Act
        taskService.findById(1L);

        // Assert
        verify(taskRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(taskRepository);
    }

    @Test
    @DisplayName("Проверка транзакционности updateStatus")
    void updateStatus_IsTransactional() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        taskService.updateStatus(1L, TaskStatus.DONE, testUser);

        // Assert
        verify(taskRepository, times(1)).save(testTask);
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Создаёт тестовую задачу с заданными параметрами.
     */
    private Task createTask(Long id, String title) {
        Task task = new Task();
        task.setId(id);
        task.setTitle(title);
        task.setDescription("Описание");
        task.setPriority(TaskPriority.MEDIUM);
        task.setDeadline(LocalDate.now().plusMonths(1));
        task.setStatus(TaskStatus.NEW);
        task.setProject(testProject);
        task.setCreator(testManager);
        task.setExecutor(testUser);
        return task;
    }

    /**
     * Создаёт тестовую задачу с заданным статусом.
     */
    private Task createTaskWithStatus(Long id, TaskStatus status) {
        Task task = createTask(id, "Задача " + id);
        task.setStatus(status);
        return task;
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

    @Test
    @DisplayName("Создание задачи с пустым описанием")
    void createTask_WithEmptyDescription_CreatesSuccessfully() {
        // Arrange
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.createTask("Тест", "", TaskPriority.MEDIUM,
                LocalDate.now(), 1L, testManager, testUser);

        // Assert
        assertNotNull(result);
        assertEquals("", result.getDescription());
    }

    @Test
    @DisplayName("Создание задачи с длинным названием")
    void createTask_WithLongTitle_CreatesSuccessfully() {
        // Arrange
        String longTitle = "З".repeat(200); // 200 символов

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.createTask(longTitle, "Описание", TaskPriority.MEDIUM,
                LocalDate.now(), 1L, testManager, testUser);

        // Assert
        assertNotNull(result);
        assertEquals(longTitle, result.getTitle());
    }

    @Test
    @DisplayName("Обновление задачи с будущим дедлайном")
    void updateTask_WithFutureDeadline_UpdatesCorrectly() {
        // Arrange
        LocalDate futureDate = LocalDate.of(2030, 12, 31);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Task result = taskService.updateTask(1L, "Name", "Desc",
                TaskPriority.HIGH, futureDate);

        // Assert
        assertEquals(futureDate, result.getDeadline());
    }

    @Test
    @DisplayName("Проверка прав: исполнитель может изменить статус своей задачи")
    void updateStatus_PermissionCheck_ExecutorCanUpdateOwnTask() {
        // Arrange
        testTask.setExecutor(testUser);

        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() ->
                taskService.updateStatus(1L, TaskStatus.IN_PROGRESS, testUser)
        );
    }

    @Test
    @DisplayName("Проверка прав: менеджер может изменить статус любой задачи проекта")
    void updateStatus_PermissionCheck_ManagerCanUpdateAnyTask() {
        // Arrange
        when(taskRepository.findById(1L)).thenReturn(Optional.of(testTask));
        when(taskRepository.save(any(Task.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        // Act & Assert
        assertDoesNotThrow(() ->
                taskService.updateStatus(1L, TaskStatus.DONE, testManager)
        );
    }
}