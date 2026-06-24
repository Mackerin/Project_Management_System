package com.projectsystem.integration;

import com.projectsystem.model.entity.User;
import com.projectsystem.repository.UserRepository;
import com.projectsystem.service.TwoFaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Интеграционные тесты для AuthController с реальной базой PostgreSQL.
 * Использует тестовые данные из миграции 007 (контекст "test").
 *
 * @author Евдокимов Д.А.
 */
@DisplayName("Интеграционные тесты AuthController (PostgreSQL)")
class AuthControllerIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TwoFaService twoFaService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByUsername("integrationtest")
                .orElseThrow(() -> new RuntimeException("Тестовый пользователь не найден. Убедитесь, что миграция 007 выполнена."));
    }

    // ==================== ТЕСТЫ СТРАНИЦЫ ВХОДА ====================

    @Test
    @DisplayName("GET /login - отображение страницы входа")
    void getLoginPage() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"))
                .andExpect(model().attributeDoesNotExist("error"));
    }

    @Test
    @DisplayName("GET /login?error=true - отображение ошибки")
    void getLoginPageWithError() throws Exception {
        mockMvc.perform(get("/login").param("error", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    // ==================== ТЕСТЫ АВТОРИЗАЦИИ ====================

    @Test
    @DisplayName("POST /login - успешный вход с валидными данными")
    void postLoginWithValidCredentials() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "integrationtest")
                        .param("password", "testpass123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/two-factor"));
    }

    @Test
    @DisplayName("POST /login - неверный пароль")
    void postLoginWithInvalidPassword() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "integrationtest")
                        .param("password", "wrongpassword")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("POST /login - несуществующий пользователь")
    void postLoginWithNonExistentUser() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "nonexistent")
                        .param("password", "testpass123")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    @Test
    @DisplayName("POST /login - пустые поля")
    void postLoginWithEmptyFields() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "")
                        .param("password", "")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error=true"));
    }

    // ==================== ТЕСТЫ ДВУХФАКТОРНОЙ АУТЕНТИФИКАЦИИ ====================

    @Test
    @DisplayName("GET /two-factor - отображение страницы 2FA")
    void getTwoFactorPage() throws Exception {
        mockMvc.perform(get("/two-factor"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/two-factor"));
    }

    @Test
    @DisplayName("POST /two-factor - успешная проверка кода")
    void postTwoFactorWithValidCode() throws Exception {
        String code = twoFaService.generateCode(testUser.getId());

        mockMvc.perform(post("/two-factor")
                        .param("code", code)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .sessionAttr("pendingUserId", testUser.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("POST /two-factor - неверный код")
    void postTwoFactorWithInvalidCode() throws Exception {
        twoFaService.generateCode(testUser.getId());

        mockMvc.perform(post("/two-factor")
                        .param("code", "000000")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .sessionAttr("pendingUserId", testUser.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/two-factor?error=true"));
    }

    @Test
    @DisplayName("POST /two-factor - без pendingUserId в сессии")
    void postTwoFactorWithoutPendingUserId() throws Exception {
        mockMvc.perform(post("/two-factor")
                        .param("code", "123456")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("POST /two-factor - код истёк")
    void postTwoFactorWithExpiredCode() throws Exception {
        twoFaService.generateCode(testUser.getId());

        testUser.setTwoFaCodeExpiration(java.time.LocalDateTime.now().minusMinutes(10));
        userRepository.save(testUser);

        String code = testUser.getTwoFaCode();

        mockMvc.perform(post("/two-factor")
                        .param("code", code)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .sessionAttr("pendingUserId", testUser.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/two-factor?error=true"));
    }

    // ==================== ТЕСТЫ ЗАЩИТЫ МАРШРУТОВ ====================

    @Test
    @DisplayName("GET /dashboard без аутентификации - редирект на /login")
    void getDashboardWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("GET /projects без аутентификации - редирект на /login")
    void getProjectsWithoutAuthentication() throws Exception {
        mockMvc.perform(get("/projects"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    // ==================== ТЕСТЫ CSRF ====================

    @Test
    @DisplayName("POST /login без CSRF токена - ошибка 403")
    void postLoginWithoutCsrfToken() throws Exception {
        mockMvc.perform(post("/login")
                        .param("username", "integrationtest")
                        .param("password", "testpass123")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED))
                .andExpect(status().isForbidden());
    }

    // ==================== ТЕСТЫ ПОСЛЕДОВАТЕЛЬНОСТИ ====================

    @Test
    @DisplayName("Полный цикл: логин → 2FA → дашборд")
    void fullAuthFlow() throws Exception {
        ResultActions loginResult = mockMvc.perform(post("/login")
                .param("username", "integrationtest")
                .param("password", "testpass123")
                .with(csrf())
                .contentType(MediaType.APPLICATION_FORM_URLENCODED));

        loginResult
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/two-factor"));

        User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
        String code = updatedUser.getTwoFaCode();

        mockMvc.perform(post("/two-factor")
                        .param("code", code)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .sessionAttr("pendingUserId", testUser.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    @DisplayName("Повторное использование кода 2FA - должно быть отклонено")
    void twoFactorCode() throws Exception {
        String code = twoFaService.generateCode(testUser.getId());

        mockMvc.perform(post("/two-factor")
                        .param("code", code)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .sessionAttr("pendingUserId", testUser.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));

        mockMvc.perform(post("/two-factor")
                        .param("code", code)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .sessionAttr("pendingUserId", testUser.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/two-factor?error=true"));
    }
}