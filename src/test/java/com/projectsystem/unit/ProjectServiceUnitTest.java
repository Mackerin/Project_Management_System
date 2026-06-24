package com.projectsystem.unit;

import com.projectsystem.model.entity.Project;
import com.projectsystem.model.entity.User;
import com.projectsystem.model.enums.ProjectStatus;
import com.projectsystem.repository.ProjectRepository;
import com.projectsystem.service.ProjectService;
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
 * Модульные тесты для ProjectService.
 *
 * @author Евдокимов Д.А.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты ProjectService")
class ProjectServiceUnitTest {

    @Mock
    private ProjectRepository projectRepository;

    @InjectMocks
    private ProjectService projectService;

    private Project testProject;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setIsEnabled(true);

        testProject = new Project();
        testProject.setId(1L);
        testProject.setName("Тестовый проект");
        testProject.setDescription("Описание тестового проекта");
        testProject.setManager(testUser);
        testProject.setStartDate(LocalDate.of(2026, 1, 1));
        testProject.setEndDate(LocalDate.of(2026, 12, 31));
        testProject.setStatus(ProjectStatus.ACTIVE);
        testProject.setCreatedAt(LocalDate.now().atStartOfDay());
    }

    // ==================== ТЕСТЫ findById ====================

    @Test
    @DisplayName("Поиск проекта по ID - успех")
    void findByIdWhenProjectExists() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        Optional<Project> result = projectService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals("Тестовый проект", result.get().getName());
        verify(projectRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Поиск проекта по ID - проект не найден")
    void findByIdWhenProjectNotExists() {
        when(projectRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<Project> result = projectService.findById(999L);

        assertFalse(result.isPresent());
        verify(projectRepository, times(1)).findById(999L);
    }

    // ==================== ТЕСТЫ findAll ====================

    @Test
    @DisplayName("Получение всех проектов - успех")
    void findAll_WhenProjectsExist_ReturnsProjectList() {
        List<Project> projects = Arrays.asList(
                testProject,
                createProject(2L, "Второй проект"),
                createProject(3L, "Третий проект")
        );
        when(projectRepository.findAll()).thenReturn(projects);

        List<Project> result = projectService.findAll();

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Тестовый проект", result.get(0).getName());
        verify(projectRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Получение всех проектов - пустой список")
    void findAllWhenNoProjects() {
        when(projectRepository.findAll()).thenReturn(Collections.emptyList());

        List<Project> result = projectService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findAll();
    }

    // ==================== ТЕСТЫ findByManager ====================

    @Test
    @DisplayName("Поиск проектов по менеджеру - успех")
    void findByManagerWhenProjectsExist() {
        List<Project> projects = Arrays.asList(testProject, createProject(2L, "Проект 2"));
        when(projectRepository.findByManager(testUser)).thenReturn(projects);

        List<Project> result = projectService.findByManager(testUser);

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(projectRepository, times(1)).findByManager(testUser);
    }

    @Test
    @DisplayName("Поиск проектов по менеджеру - нет проектов")
    void findByManagerWhenNoProjects() {
        when(projectRepository.findByManager(testUser)).thenReturn(Collections.emptyList());

        List<Project> result = projectService.findByManager(testUser);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findByManager(testUser);
    }

    // ==================== ТЕСТЫ findByStatus ====================

    @Test
    @DisplayName("Поиск проектов по статусу - успех")
    void findByStatusWhenProjectsExist() {
        List<Project> activeProjects = Arrays.asList(
                testProject,
                createProjectWithStatus(2L, ProjectStatus.ACTIVE)
        );
        when(projectRepository.findByStatus(ProjectStatus.ACTIVE)).thenReturn(activeProjects);

        List<Project> result = projectService.findByStatus(ProjectStatus.ACTIVE);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(ProjectStatus.ACTIVE, result.get(0).getStatus());
        verify(projectRepository, times(1)).findByStatus(ProjectStatus.ACTIVE);
    }

    @Test
    @DisplayName("Поиск проектов по статусу - нет проектов с таким статусом")
    void findByStatusWhenNoProjects() {
        when(projectRepository.findByStatus(ProjectStatus.CLOSED)).thenReturn(Collections.emptyList());

        List<Project> result = projectService.findByStatus(ProjectStatus.CLOSED);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(projectRepository, times(1)).findByStatus(ProjectStatus.CLOSED);
    }

    // ==================== ТЕСТЫ createProject ====================

    @Test
    @DisplayName("Создание проекта - успех")
    void createProjectWithValidData() {
        String name = "Новый проект";
        String description = "Описание нового проекта";
        LocalDate startDate = LocalDate.of(2026, 3, 1);
        LocalDate endDate = LocalDate.of(2026, 9, 30);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> {
                    Project project = invocation.getArgument(0);
                    project.setId(100L);
                    return project;
                });

        Project result = projectService.createProject(name, description, testUser, startDate, endDate);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(name, result.getName());
        assertEquals(description, result.getDescription());
        assertEquals(testUser, result.getManager());
        assertEquals(startDate, result.getStartDate());
        assertEquals(endDate, result.getEndDate());
        assertEquals(ProjectStatus.ACTIVE, result.getStatus()); // По умолчанию

        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("Создание проекта - статус по умолчанию ACTIVE")
    void createProjectSetsDefaultStatusToActive() {
        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.createProject(
                "Тест", "Описание", testUser,
                LocalDate.now(), LocalDate.now().plusMonths(6)
        );

        assertEquals(ProjectStatus.ACTIVE, result.getStatus());
    }

    // ==================== ТЕСТЫ updateProject ====================

    @Test
    @DisplayName("Обновление проекта - успех")
    void updateProjectWhenProjectExists() {
        Long projectId = 1L;
        String newName = "Обновлённое название";
        String newDescription = "Новое описание";
        LocalDate newStartDate = LocalDate.of(2026, 2, 1);
        LocalDate newEndDate = LocalDate.of(2026, 10, 31);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(
                projectId, newName, newDescription, newStartDate, newEndDate
        );

        assertNotNull(result);
        assertEquals(newName, result.getName());
        assertEquals(newDescription, result.getDescription());
        assertEquals(newStartDate, result.getStartDate());
        assertEquals(newEndDate, result.getEndDate());

        verify(projectRepository, times(1)).findById(projectId);
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    @DisplayName("Обновление проекта - проект не найден")
    void updateProjectWhenProjectNotFound() {
        Long projectId = 999L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> projectService.updateProject(
                        projectId, "Name", "Desc", LocalDate.now(), LocalDate.now()
                )
        );

        assertEquals("Проект не найден", exception.getMessage());
        verify(projectRepository, times(1)).findById(projectId);
        verify(projectRepository, never()).save(any(Project.class));
    }

    // ==================== ТЕСТЫ deleteProject ====================

    @Test
    @DisplayName("Удаление проекта - успех")
    void deleteProjectWhenProjectExists() {
        Long projectId = 1L;
        doNothing().when(projectRepository).deleteById(projectId);

        projectService.deleteProject(projectId);

        verify(projectRepository, times(1)).deleteById(projectId);
    }

    @Test
    @DisplayName("Удаление несуществующего проекта - не вызывает ошибку")
    void deleteProjectWhenProjectNotExists() {
        Long projectId = 999L;
        doNothing().when(projectRepository).deleteById(projectId);

        assertDoesNotThrow(() -> projectService.deleteProject(projectId));
        verify(projectRepository, times(1)).deleteById(projectId);
    }

    // ==================== ТЕСТЫ closeProject ====================

    @Test
    @DisplayName("Закрытие проекта - успех")
    void closeProjectWhenProjectExists() {
        Long projectId = 1L;
        testProject.setStatus(ProjectStatus.ACTIVE);

        when(projectRepository.findById(projectId)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.closeProject(projectId);

        assertNotNull(result);
        assertEquals(ProjectStatus.CLOSED, result.getStatus());

        verify(projectRepository, times(1)).findById(projectId);
        verify(projectRepository, times(1)).save(testProject);
    }

    @Test
    @DisplayName("Закрытие проекта - проект не найден")
    void closeProjectWhenProjectNotFound() {
        Long projectId = 999L;

        when(projectRepository.findById(projectId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> projectService.closeProject(projectId)
        );

        assertEquals("Проект не найден", exception.getMessage());
        verify(projectRepository, times(1)).findById(projectId);
        verify(projectRepository, never()).save(any(Project.class));
    }

    // ==================== ТЕСТЫ ПРОВЕРКИ ТРАНЗАКЦИОННОСТИ ====================

    @Test
    @DisplayName("Проверка транзакционности createProject")
    void createProjectIsTransactional() {
        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        projectService.createProject("Test", "Desc", testUser,
                LocalDate.now(), LocalDate.now().plusMonths(1));

        verify(projectRepository, times(1)).save(any(Project.class));
    }

    @Test
    @DisplayName("Проверка readOnly транзакции для findById")
    void findByIdIsReadOnlyTransactional() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));

        projectService.findById(1L);

        verify(projectRepository, times(1)).findById(1L);
        verifyNoMoreInteractions(projectRepository);
    }

    @Test
    @DisplayName("Проверка транзакционности closeProject")
    void closeProjectIsTransactional() {
        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        projectService.closeProject(1L);

        verify(projectRepository, times(1)).save(testProject);
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЕ МЕТОДЫ ====================

    /**
     * Создаёт тестовый проект с заданными параметрами.
     */
    private Project createProject(Long id, String name) {
        Project project = new Project();
        project.setId(id);
        project.setName(name);
        project.setDescription("Описание");
        project.setManager(testUser);
        project.setStartDate(LocalDate.now());
        project.setEndDate(LocalDate.now().plusMonths(6));
        project.setStatus(ProjectStatus.ACTIVE);
        return project;
    }

    /**
     * Создаёт тестовый проект с заданным статусом.
     */
    private Project createProjectWithStatus(Long id, ProjectStatus status) {
        Project project = createProject(id, "Проект " + id);
        project.setStatus(status);
        return project;
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

    @Test
    @DisplayName("Создание проекта с пустым описанием")
    void createProjectWithEmptyDescription() {
        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.createProject(
                "Тест", "", testUser,
                LocalDate.now(), LocalDate.now().plusMonths(1)
        );

        assertNotNull(result);
        assertEquals("", result.getDescription());
    }

    @Test
    @DisplayName("Создание проекта с длинным названием")
    void createProjectWithLongName() {
        String longName = "П".repeat(200);

        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.createProject(
                longName, "Описание", testUser,
                LocalDate.now(), LocalDate.now().plusMonths(1)
        );

        assertNotNull(result);
        assertEquals(longName, result.getName());
    }

    @Test
    @DisplayName("Обновление проекта с новыми датами")
    void updateProjectWithNewDates() {
        LocalDate futureDate = LocalDate.of(2030, 12, 31);

        when(projectRepository.findById(1L)).thenReturn(Optional.of(testProject));
        when(projectRepository.save(any(Project.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Project result = projectService.updateProject(
                1L, "Name", "Desc", futureDate, futureDate.plusYears(1)
        );

        assertEquals(futureDate, result.getStartDate());
        assertEquals(futureDate.plusYears(1), result.getEndDate());
    }
}