package com.projectsystem.integration;

import com.projectsystem.model.entity.Project;
import com.projectsystem.model.entity.User;
import com.projectsystem.repository.ProjectRepository;
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
 * Интеграционные тесты для ProjectController с реальной базой PostgreSQL.
 * Использует тестовые данные из миграции 007 (контекст "test").
 *
 * @author Евдокимов Д.А.
 */
@DisplayName("Интеграционные тесты ProjectController (PostgreSQL)")
class ProjectControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ProjectRepository projectRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;
    private Project testProject;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByUsername("integrationtest")
                .orElseThrow(() -> new RuntimeException("Тестовый пользователь не найден"));

        testProject = projectRepository.findByNameContainingIgnoreCase("Тестовый проект")
                .stream()
                .filter(p -> "Тестовый проект".equals(p.getName()))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Тестовый проект не найден"));
    }

    // ==================== ТЕСТЫ СПИСКА ПРОЕКТОВ ====================

    @Test
    @DisplayName("GET /projects без аутентификации - редирект на /login")
    void getProjectsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /projects с аутентификацией - отображение списка")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getProjectsWithAuthentication() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/list"))
                .andExpect(model().attributeExists("projects"))
                .andExpect(model().attributeExists("username"))
                .andExpect(model().attribute("username", "integrationtest"));
    }

    @Test
    @DisplayName("GET /projects с аутентификацией - проекты в модели")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getProjectsWithAuthenticationProjectsInModel() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projects",
                        org.hamcrest.Matchers.hasItem(
                                org.hamcrest.Matchers.hasProperty("name",
                                        org.hamcrest.Matchers.is("Тестовый проект"))
                        )
                ));
    }

    // ==================== ТЕСТЫ ДЕТАЛЕЙ ПРОЕКТА ====================

    @Test
    @DisplayName("GET /projects/{id} без аутентификации - редирект на /login")
    void getProjectDetailsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/projects/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /projects/{id} с аутентификацией - отображение деталей")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getProjectDetailsWithAuthentication() throws Exception {
        mockMvc.perform(get("/projects/" + testProject.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/details"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attribute("project",
                        org.hamcrest.Matchers.hasProperty("name",
                                org.hamcrest.Matchers.is("Тестовый проект"))));
    }

    @Test
    @DisplayName("GET /projects/{id} несуществующий проект - исключение")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getProjectDetailsNonExistent() throws Exception {
        mockMvc.perform(get("/projects/999999"))
                .andExpect(status().is5xxServerError());
    }

    // ==================== ТЕСТЫ ФОРМЫ СОЗДАНИЯ ====================

    @Test
    @DisplayName("GET /projects/create без аутентификации - редирект на /login")
    void getCreateFormWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/projects/create"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /projects/create с аутентификацией - отображение формы")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getCreateFormWithAuthentication() throws Exception {
        mockMvc.perform(get("/projects/create"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/create"))
                .andExpect(model().attributeExists("project"))
                .andExpect(model().attribute("project",
                        org.hamcrest.Matchers.hasProperty("name",
                                org.hamcrest.Matchers.nullValue())));
    }

    // ==================== ТЕСТЫ СОЗДАНИЯ ПРОЕКТА ====================

    @Test
    @DisplayName("POST /projects/create без аутентификации - редирект на /login")
    void postCreateProjectWithoutAuthentication() throws Exception {
        mockMvc.perform(post("/projects/create")
                        .param("name", "Новый проект")
                        .param("description", "Описание")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("POST /projects/create с аутентификацией - успешное создание")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void postCreateProjectWithAuthentication() throws Exception {
        mockMvc.perform(post("/projects/create")
                        .param("name", "Новый тестовый проект")
                        .param("description", "Описание нового проекта")
                        .param("startDate", "2026-03-01")
                        .param("endDate", "2026-09-30")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        mockMvc.perform(get("/projects")
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projects",
                        org.hamcrest.Matchers.hasItem(
                                org.hamcrest.Matchers.hasProperty("name",
                                        org.hamcrest.Matchers.is("Новый тестовый проект"))
                        )));
    }

    @Test
    @DisplayName("POST /projects/create без CSRF токена - ошибка 403")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void postCreateProjectWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/projects/create")
                        .param("name", "Новый проект")
                        .param("description", "Описание")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

    @Test
    @DisplayName("POST /projects - метод не разрешён")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void postProjectsMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/projects")
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("PUT /projects/{id} - метод не разрешён")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void putProjectMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/projects/" + testProject.getId())
                        .with(csrf()))
                .andExpect(status().isMethodNotAllowed());
    }

    @Test
    @DisplayName("DELETE /projects/{id} - метод не разрешён")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void deleteProjectMethodNotAllowed() throws Exception {
        mockMvc.perform(post("/projects/" + testProject.getId())
                        .with(csrf())
                        .param("_method", "delete"))
                .andExpect(status().isMethodNotAllowed());
    }

    // ==================== ТЕСТЫ С РАЗНЫМИ РОЛЯМИ ====================

    @Test
    @DisplayName("GET /projects с ролью EXECUTOR - доступ разрешён")
    @WithMockUser(username = "executor", roles = {"EXECUTOR"})
    void getProjectsWithExecutorRole() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/list"))
                .andExpect(model().attributeExists("projects"));
    }

    @Test
    @DisplayName("GET /projects с ролью OBSERVER - доступ разрешён")
    @WithMockUser(username = "observer", roles = {"OBSERVER"})
    void getProjectsWithObserverRole() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/list"))
                .andExpect(model().attributeExists("projects"));
    }

    @Test
    @DisplayName("POST /projects/create с ролью EXECUTOR - создание разрешено")
    @WithMockUser(username = "executor", roles = {"EXECUTOR"})
    void postCreateProjectWithExecutorRole() throws Exception {
        mockMvc.perform(post("/projects/create")
                        .param("name", "Проект исполнителя")
                        .param("description", "Описание")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));
    }

    // ==================== ТЕСТЫ ПОСЛЕДОВАТЕЛЬНОСТИ ====================

    @Test
    @DisplayName("Полный цикл: список → создание → список с новым проектом")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void fullFlowListCreateList() throws Exception {
        // Step 1: Получаем список проектов
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/list"));

        // Step 2: Создаём новый проект с УНИКАЛЬНЫМ именем
        String uniqueName = "Проект из цикла " + System.currentTimeMillis();

        mockMvc.perform(post("/projects/create")
                        .param("name", uniqueName)
                        .param("description", "Описание из цикла")
                        .param("startDate", "2026-06-01")
                        .param("endDate", "2026-12-31")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/projects"));

        // Step 3: Проверяем, что новый проект в списке
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(model().attribute("projects",
                        org.hamcrest.Matchers.hasItem(
                                org.hamcrest.Matchers.hasProperty("name",
                                        org.hamcrest.Matchers.is(uniqueName))
                        )));
    }

    @Test
    @DisplayName("Переход: список → детали проекта → список")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void fullFlowListDetailsList() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk());

        mockMvc.perform(get("/projects/" + testProject.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/details"))
                .andExpect(model().attribute("project",
                        org.hamcrest.Matchers.hasProperty("id",
                                org.hamcrest.Matchers.equalTo(testProject.getId()))));

        mockMvc.perform(get("/projects"))
                .andExpect(status().isOk())
                .andExpect(view().name("projects/list"));
    }
}