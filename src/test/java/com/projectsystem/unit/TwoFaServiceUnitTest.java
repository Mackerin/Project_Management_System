package com.projectsystem.unit;

import com.projectsystem.config.TwoFaConfig;
import com.projectsystem.model.entity.User;
import com.projectsystem.repository.UserRepository;
import com.projectsystem.service.TwoFaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Модульные тесты для TwoFaService.
 *
 * @author Евдокимов Д.А.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты TwoFaService")
class TwoFaServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private TwoFaConfig twoFaConfig;

    @InjectMocks
    private TwoFaService twoFaService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setIsEnabled(true);
    }

    // ==================== ТЕСТЫ generateCode ====================

    @Test
    @DisplayName("Генерация 2FA кода - успех")
    void generateCodeWhenUserExists() {
        Long userId = 1L;
        int expirationMinutes = 5;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(twoFaConfig.getExpirationMinutes()).thenReturn(expirationMinutes);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String code = twoFaService.generateCode(userId);

        assertNotNull(code);
        assertEquals(6, code.length());
        assertTrue(code.matches("\\d{6}"), "Код должен содержать 6 цифр");
        assertNotNull(testUser.getTwoFaCode());
        assertEquals(code, testUser.getTwoFaCode());
        assertNotNull(testUser.getTwoFaCodeExpiration());

        verify(userRepository, times(1)).findById(userId);
        verify(twoFaConfig, times(1)).getExpirationMinutes();
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Генерация 2FA кода - пользователь не найден")
    void generateCodeWhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> twoFaService.generateCode(userId)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Генерация 2FA кода - срок действия установлен корректно")
    void generateCodeSetsCorrectExpirationTime() {
        Long userId = 1L;
        int expirationMinutes = 10;
        LocalDateTime beforeCall = LocalDateTime.now();

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(twoFaConfig.getExpirationMinutes()).thenReturn(expirationMinutes);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        twoFaService.generateCode(userId);
        LocalDateTime afterCall = LocalDateTime.now();

        assertNotNull(testUser.getTwoFaCodeExpiration());
        LocalDateTime expiration = testUser.getTwoFaCodeExpiration();

        LocalDateTime expectedMin = beforeCall.plusMinutes(expirationMinutes).minusSeconds(5);
        LocalDateTime expectedMax = afterCall.plusMinutes(expirationMinutes).plusSeconds(5);

        assertTrue(expiration.isAfter(expectedMin) || expiration.isEqual(expectedMin),
                "Время истечения не должно быть раньше ожидаемого");
        assertTrue(expiration.isBefore(expectedMax) || expiration.isEqual(expectedMax),
                "Время истечения не должно быть позже ожидаемого");
    }

    @Test
    @DisplayName("Генерация 2FA кода - разные вызовы дают разные коды")
    void generateCodeMultipleCalls() {
        Long userId = 1L;

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(twoFaConfig.getExpirationMinutes()).thenReturn(5);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String code1 = twoFaService.generateCode(userId);
        String code2 = twoFaService.generateCode(userId);

        assertNotNull(code1);
        assertNotNull(code2);
        assertNotEquals(code1, code2, "Разные вызовы должны генерировать разные коды");
    }

    // ==================== ТЕСТЫ verifyCode ====================

    @Test
    @DisplayName("Проверка 2FA кода - верный код")
    void verifyCodeWithValidCode() {
        Long userId = 1L;
        String validCode = "123456";
        testUser.setTwoFaCode(validCode);
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = twoFaService.verifyCode(userId, validCode);

        assertTrue(result);
        assertNull(testUser.getTwoFaCode(), "Код должен быть очищен после успешной проверки");
        assertNull(testUser.getTwoFaCodeExpiration());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Проверка 2FA кода - неверный код")
    void verifyCodeWithInvalidCode() {
        Long userId = 1L;
        String validCode = "123456";
        String invalidCode = "654321";

        testUser.setTwoFaCode(validCode);
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        boolean result = twoFaService.verifyCode(userId, invalidCode);

        assertFalse(result);
        assertEquals(validCode, testUser.getTwoFaCode(), "Код не должен изменяться при неверной проверке");

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Проверка 2FA кода - код не установлен")
    void verifyCodeWhenCodeNotSet() {
        Long userId = 1L;
        testUser.setTwoFaCode(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        boolean result = twoFaService.verifyCode(userId, "123456");

        assertFalse(result);
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Проверка 2FA кода - код истёк")
    void verifyCodeWhenCodeExpired() {
        Long userId = 1L;
        String code = "123456";

        testUser.setTwoFaCode(code);
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().minusMinutes(1));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = twoFaService.verifyCode(userId, code);

        assertFalse(result);
        assertNull(testUser.getTwoFaCode(), "Истёкший код должен быть очищен");
        assertNull(testUser.getTwoFaCodeExpiration());

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Проверка 2FA кода - время истечения null")
    void verifyCodeWhenExpirationIsNull() {
        Long userId = 1L;
        String code = "123456";

        testUser.setTwoFaCode(code);
        testUser.setTwoFaCodeExpiration(null);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = twoFaService.verifyCode(userId, code);

        assertFalse(result);
        assertNull(testUser.getTwoFaCode());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Проверка 2FA кода - пользователь не найден")
    void verifyCodeWhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> twoFaService.verifyCode(userId, "123456")
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Проверка 2FA кода - регистр не важен")
    void verifyCodeCaseSensitivity() {
        Long userId = 1L;
        String code = "123456";

        testUser.setTwoFaCode(code);
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = twoFaService.verifyCode(userId, code);

        assertTrue(result);
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

    @Test
    @DisplayName("Генерация кода - проверка формата (ровно 6 цифр)")
    void generateCode() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(twoFaConfig.getExpirationMinutes()).thenReturn(5);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        for (int i = 0; i < 10; i++) {
            String code = twoFaService.generateCode(1L);
            assertTrue(code.matches("^\\d{6}$"),
                    "Код должен быть ровно 6 цифр, получен: " + code);
        }
    }

    @Test
    @DisplayName("Проверка кода - пустая строка")
    void verifyCodeWithEmptyString() {
        Long userId = 1L;
        testUser.setTwoFaCode("123456");
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        boolean result = twoFaService.verifyCode(userId, "");

        assertFalse(result);
    }

    @Test
    @DisplayName("Проверка кода - null вместо кода")
    void verifyCodeWithNullCode() {
        Long userId = 1L;
        testUser.setTwoFaCode("123456");
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        boolean result = twoFaService.verifyCode(userId, null);

        assertFalse(result);
    }

    @Test
    @DisplayName("Проверка кода - код с лишними символами")
    void verifyCode_WithExtraCharacters_ReturnsFalse() {
        Long userId = 1L;
        testUser.setTwoFaCode("123456");
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        boolean result = twoFaService.verifyCode(userId, "1234567");

        assertFalse(result);
    }

    // ==================== ТЕСТЫ ПРОВЕРКИ ТРАНЗАКЦИОННОСТИ ====================

    @Test
    @DisplayName("Проверка транзакционности generateCode")
    void generateCodeIsTransactional() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(twoFaConfig.getExpirationMinutes()).thenReturn(5);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        twoFaService.generateCode(1L);

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Проверка транзакционности verifyCode при успешной проверке")
    void verifyCodeIsTransactional() {
        testUser.setTwoFaCode("123456");
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        twoFaService.verifyCode(1L, "123456");

        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("Проверка транзакционности verifyCode при неудачной проверке")
    void verifyCodeFailedVerification() {
        testUser.setTwoFaCode("123456");
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        twoFaService.verifyCode(1L, "000000");

        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== ТЕСТЫ БИЗНЕС-ЛОГИКИ ====================

    @Test
    @DisplayName("После успешной проверки код удаляется из пользователя")
    void verifyCodeSuccessfulVerification() {
        Long userId = 1L;
        String code = "999999";

        testUser.setTwoFaCode(code);
        testUser.setTwoFaCodeExpiration(LocalDateTime.now().plusMinutes(5));

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        boolean firstCheck = twoFaService.verifyCode(userId, code);
        boolean secondCheck = twoFaService.verifyCode(userId, code);

        assertTrue(firstCheck, "Первая проверка должна быть успешной");
        assertFalse(secondCheck, "Повторная проверка с тем же кодом должна быть неудачной");
        assertNull(testUser.getTwoFaCode(), "Код должен быть удалён после первой успешной проверки");
    }

    @Test
    @DisplayName("Генерация нового кода перезаписывает старый")
    void generateCodeNewCodeOverwritesOldCode() {
        Long userId = 1L;
        String oldCode = "111111";

        testUser.setTwoFaCode(oldCode);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(twoFaConfig.getExpirationMinutes()).thenReturn(5);
        when(userRepository.save(any(User.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        String newCode = twoFaService.generateCode(userId);

        assertNotEquals(oldCode, newCode);
        assertEquals(newCode, testUser.getTwoFaCode());
        assertEquals(newCode, testUser.getTwoFaCode(), "Новый код должен заменить старый");
    }
}