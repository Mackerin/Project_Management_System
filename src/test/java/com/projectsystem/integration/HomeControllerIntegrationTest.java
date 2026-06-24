package com.projectsystem.integration;

import com.projectsystem.model.entity.User;
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
 * Интеграционные тесты для HomeController с реальной базой PostgreSQL.
 *
 * @author Евдокимов Д.А.
 */
@DisplayName("Интеграционные тесты HomeController (PostgreSQL)")
class HomeControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByUsername("integrationtest")
                .orElseThrow(() -> new RuntimeException("Тестовый пользователь не найден"));
    }

    // ==================== ТЕСТЫ КОРНЕВОГО МАРШРУТА / ====================

    @Test
    @DisplayName("GET / без аутентификации - редирект на /login")
    void getRootWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET / с аутентификацией - редирект на /dashboard")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getRootWithAuthentication() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    // ==================== ТЕСТЫ ДАШБОРДА ====================

    @Test
    @DisplayName("GET /dashboard без аутентификации - редирект на /login")
    void getDashboardWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /dashboard с аутентификацией - отображение страницы")
    @WithMockUser(username = "integrationtest", roles = {"MANAGER"})
    void getDashboardWithAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attributeExists("username"))
                .andExpect(model().attribute("username", "integrationtest"));
    }

    @Test
    @DisplayName("GET /dashboard с другим пользователем - корректное имя в модели")
    @WithMockUser(username = "customuser", roles = {"EXECUTOR"})
    void getDashboardWithDifferentUser() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attribute("username", "customuser"));
    }

    // ==================== ТЕСТЫ ЗАЩИЩЁННЫХ МАРШРУТОВ ====================

    @Test
    @DisplayName("GET /projects без аутентификации - редирект на /login")
    void getProjectsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /tasks без аутентификации - редирект на /login")
    void getTasksWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/tasks"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /notifications без аутентификации - редирект на /login")
    void getNotificationsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/notifications"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ==================== ТЕСТЫ ПОСЛЕДОВАТЕЛЬНОСТИ ====================

    @Test
    @DisplayName("Полный цикл: корень → логин → дашборд")
    void fullFlowRootToLoginToDashboard() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        mockMvc.perform(post("/login")
                        .param("username", "integrationtest")
                        .param("password", "testpass123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/two-factor"));
    }

    @Test
    @DisplayName("Прямой доступ к /dashboard без логина - редирект на /login")
    void directAccessToDashboardWithoutLogin() throws Exception {
        mockMvc.perform(get("/dashboard")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

    @Test
    @DisplayName("POST / без CSRF токена - ошибка 403")
    void postRootWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("POST /dashboard без CSRF токена - ошибка 403")
    void postDashboardWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/dashboard"))
                .andExpect(status().isForbidden());
    }

    // ==================== ТЕСТЫ С РАЗНЫМИ РОЛЯМИ ====================

    @Test
    @DisplayName("GET /dashboard с ролью MANAGER - доступ разрешён")
    @WithMockUser(username = "manager", roles = {"MANAGER"})
    void getDashboardWithManagerRole() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attribute("username", "manager"));
    }

    @Test
    @DisplayName("GET /dashboard с ролью EXECUTOR - доступ разрешён")
    @WithMockUser(username = "executor", roles = {"EXECUTOR"})
    void getDashboardWithExecutorRole() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attribute("username", "executor"));
    }

    @Test
    @DisplayName("GET /dashboard с ролью OBSERVER - доступ разрешён")
    @WithMockUser(username = "observer", roles = {"OBSERVER"})
    void getDashboardWithObserverRole() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard/index"))
                .andExpect(model().attribute("username", "observer"));
    }
}