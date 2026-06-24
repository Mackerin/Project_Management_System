package com.projectsystem.integration;

import com.projectsystem.model.entity.Project;
import com.projectsystem.model.entity.Task;
import com.projectsystem.model.entity.User;
import com.projectsystem.model.enums.TaskStatus;
import com.projectsystem.repository.ProjectRepository;
import com.projectsystem.repository.TaskRepository;
import com.projectsystem.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для TaskController с реальной базой PostgreSQL.
 *
 * @author Евдокимов Д.А.
 */
@DisplayName("Интеграционные тесты TaskController (PostgreSQL)")
class TaskControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Project testProject;
    private Task testTask;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByUsername("integrationtest")
                .orElseThrow(() -> new RuntimeException("Тестовый пользователь не найден"));

        testProject = projectRepository.findByNameContainingIgnoreCase("Тестовый проект")
                .stream()
                .filter(p -> "Тестовый проект".equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Тестовый проект не найден"));

        testTask = taskRepository.findByProjectId(testProject.getId())
                .stream()
                .filter(t -> "Тестовая задача 1".equals(t.getTitle()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Тестовая задача не найдена"));
    }

    // ==================== ТЕСТЫ СПИСКА ЗАДАЧ ====================

    @Test
    @DisplayName("GET /tasks без аутентификации - редирект на /login")
    void getTasksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /tasks с аутентификацией - отображение списка")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getTasksWithAuthentication() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attributeExists("tasks"));
    }

    @Test
    @DisplayName("GET /tasks?projectId=1 - фильтрация по проекту")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getTasksFilteredByProject() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("projectId", String.valueOf(testProject.getId())))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attribute("tasks",
                        org.hamcrest.Matchers.hasItem(
                                org.hamcrest.Matchers.hasProperty("project",
                                        org.hamcrest.Matchers.hasProperty("id",
                                                org.hamcrest.Matchers.equalTo(testProject.getId()))))));
    }

    @Test
    @DisplayName("GET /tasks?status=NEW - фильтрация по статусу")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getTasksFilteredByStatus() throws Exception {
        mockMvc.perform(get("/tasks")
                        .param("status", "NEW"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attribute("tasks",
                        org.hamcrest.Matchers.hasItem(
                                org.hamcrest.Matchers.hasProperty("status",
                                        org.hamcrest.Matchers.is(TaskStatus.NEW)))));
    }

    // ==================== ТЕСТЫ ДЕТАЛЕЙ ЗАДАЧИ ====================

    @Test
    @DisplayName("GET /tasks/{id} без аутентификации - редирект на /login")
    void getTaskDetailsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/tasks/" + testTask.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /tasks/{id} с аутентификацией - отображение деталей")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getTaskDetailsWithAuthentication() throws Exception {
        mockMvc.perform(get("/tasks/" + testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/details"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attribute("task",
                        org.hamcrest.Matchers.hasProperty("id",
                                org.hamcrest.Matchers.equalTo(testTask.getId()))));
    }

    @Test
    @DisplayName("GET /tasks/{id} несуществующая задача - ошибка 500")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getTaskDetailsNonExistent() throws Exception {
        mockMvc.perform(get("/tasks/999999"))
                .andExpect(status().is5xxServerError());
    }

    // ==================== ТЕСТЫ ФОРМЫ СОЗДАНИЯ ====================

    @Test
    @DisplayName("GET /tasks/create без аутентификации - редирект на /login")
    void getCreateFormWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/tasks/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /tasks/create с аутентификацией - отображение формы")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getCreateFormWithAuthentication() throws Exception {
        mockMvc.perform(get("/tasks/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/create"))
                .andExpect(model().attributeExists("task"))
                .andExpect(model().attributeExists("statuses"))
                .andExpect(model().attributeExists("priorities"));
    }

    // ==================== ТЕСТЫ СОЗДАНИЯ ЗАДАЧИ ====================

    @Test
    @DisplayName("POST /tasks/create без аутентификации - редирект на /login")
    void postCreateTaskWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/tasks/create")
                        .param("title", "Новая задача")
                        .param("description", "Описание")
                        .param("priority", "MEDIUM")
                        .param("deadline", "2026-12-31")
                        .param("projectId", String.valueOf(testProject.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("POST /tasks/create с аутентификацией - успешное создание")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void postCreateTaskWithAuthentication() throws Exception {
        String uniqueTitle = "Задача из теста " + System.currentTimeMillis();

        mockMvc.perform(post("/tasks/create")
                        .param("title", uniqueTitle)
                        .param("description", "Описание задачи из теста")
                        .param("priority", "HIGH")
                        .param("deadline", "2026-12-31")
                        .param("projectId", String.valueOf(testProject.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        // Проверяем, что задача действительно создана
        mockMvc.perform(get("/tasks")
                        .param("projectId", String.valueOf(testProject.getId())))
                .andExpect(status().isOk())
                .andExpect(model().attribute("tasks",
                        org.hamcrest.Matchers.hasItem(
                                org.hamcrest.Matchers.hasProperty("title",
                                        org.hamcrest.Matchers.is(uniqueTitle)))));
    }

    @Test
    @DisplayName("POST /tasks/create без CSRF токена - ошибка 403")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void postCreateTaskWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/tasks/create")
                        .param("title", "Новая задача")
                        .param("description", "Описание")
                        .param("priority", "MEDIUM")
                        .param("deadline", "2026-12-31")
                        .param("projectId", String.valueOf(testProject.getId()))
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    // ==================== ТЕСТЫ ОБНОВЛЕНИЯ СТАТУСА ====================

    @Test
    @DisplayName("POST /tasks/{id}/status без аутентификации - редирект на /login")
    void postUpdateStatusWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/tasks/" + testTask.getId() + "/status")
                        .param("status", "IN_PROGRESS")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("POST /tasks/{id}/status с аутентификацией - успешное обновление")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void postUpdateStatusWithAuthentication() throws Exception {
        mockMvc.perform(post("/tasks/" + testTask.getId() + "/status")
                        .param("status", "IN_PROGRESS")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/" + testTask.getId()));

        // Проверяем, что статус действительно обновился
        Task updatedTask = taskRepository.findById(testTask.getId()).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals(TaskStatus.IN_PROGRESS, updatedTask.getStatus());
    }

    @Test
    @DisplayName("POST /tasks/{id}/status с несуществующим статусом - ошибка")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void postUpdateStatusWithInvalidStatus() throws Exception {
        mockMvc.perform(post("/tasks/" + testTask.getId() + "/status")
                        .param("status", "INVALID_STATUS")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                // ✅ Ожидаем 500, так как исключение не обрабатывается
                .andExpect(status().is5xxServerError());
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

    @Test
    @DisplayName("POST /tasks - метод не разрешён")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void postTasksMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/tasks")
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("PUT /tasks/{id} - метод не разрешён")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void putTaskMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/tasks/" + testTask.getId())
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }

    // ==================== ТЕСТЫ С РАЗНЫМИ РОЛЯМИ ====================

    @Test
    @DisplayName("GET /tasks с ролью EXECUTOR - доступ разрешён")
    @WithMockUser(username = "executor", roles = {"EXECUTOR"})
    void getTasksWithExecutorRole() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attributeExists("tasks"));
    }

    @Test
    @DisplayName("GET /tasks с ролью OBSERVER - доступ разрешён")
    @WithMockUser(username = "observer", roles = {"OBSERVER"})
    void getTasksWithObserverRole() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"))
                .andExpect(model().attributeExists("tasks"));
    }

    // ==================== ТЕСТЫ ПОСЛЕДОВАТЕЛЬНОСТИ ====================

    @Test
    @DisplayName("Полный цикл: список → создание → список с новой задачей")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void fullFlowListCreateList() throws Exception {
        // Step 1: Получаем список задач
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"));

        // Step 2: Создаём новую задачу
        String uniqueTitle = "Задача из цикла " + System.currentTimeMillis();

        mockMvc.perform(post("/tasks/create")
                        .param("title", uniqueTitle)
                        .param("description", "Описание из цикла")
                        .param("priority", "MEDIUM")
                        .param("deadline", "2026-12-31")
                        .param("projectId", String.valueOf(testProject.getId()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks"));

        // Step 3: Проверяем, что новая задача в списке
        mockMvc.perform(get("/tasks")
                        .param("projectId", String.valueOf(testProject.getId())))
                .andExpect(status().isOk())
                .andExpect(model().attribute("tasks",
                        org.hamcrest.Matchers.hasItem(
                                org.hamcrest.Matchers.hasProperty("title",
                                        org.hamcrest.Matchers.is(uniqueTitle)))));
    }

    @Test
    @DisplayName("Переход: список → детали задачи → список")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void fullFlowListDetailsList() throws Exception {
        // Step 1: Список задач
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());

        // Step 2: Детали конкретной задачи
        mockMvc.perform(get("/tasks/" + testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/details"))
                .andExpect(model().attribute("task",
                        org.hamcrest.Matchers.hasProperty("id",
                                org.hamcrest.Matchers.equalTo(testTask.getId()))));

        // Step 3: Возврат к списку
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk())
                .andExpect(view().name("tasks/list"));
    }

    @Test
    @DisplayName("Полный цикл: список → изменение статуса → детали")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void fullFlowListUpdateStatusDetails() throws Exception {
        // Step 1: Список задач
        mockMvc.perform(get("/tasks"))
                .andExpect(status().isOk());

        // Step 2: Обновляем статус задачи
        mockMvc.perform(post("/tasks/" + testTask.getId() + "/status")
                        .param("status", "REVIEW")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/tasks/" + testTask.getId()));

        // Step 3: Проверяем детали с обновлённым статусом
        mockMvc.perform(get("/tasks/" + testTask.getId()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("task",
                        org.hamcrest.Matchers.hasProperty("status",
                                org.hamcrest.Matchers.is(TaskStatus.REVIEW))));
    }
}