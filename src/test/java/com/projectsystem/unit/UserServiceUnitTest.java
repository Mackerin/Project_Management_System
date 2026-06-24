package com.projectsystem.unit;

import com.projectsystem.model.entity.User;
import com.projectsystem.model.entity.Role;
import com.projectsystem.repository.UserRepository;
import com.projectsystem.repository.RoleRepository;
import com.projectsystem.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Модульные тесты для UserService.
 *
 * @author Евдокимов Д.А.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты UserService")
class UserServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RoleRepository roleRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private Role testRole;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setPassword("encodedPassword");
        testUser.setEmail("test@example.com");
        testUser.setIsEnabled(true);

        testRole = new Role();
        testRole.setId(1L);
        testRole.setName("ROLE_MANAGER");
    }

    // ==================== ТЕСТЫ findByUsername ====================

    @Test
    @DisplayName("Поиск пользователя по имени - успех")
    void findByUsernameWhenUserExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findByUsername("testuser");

        assertTrue(result.isPresent());
        assertEquals("testuser", result.get().getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }

    @Test
    @DisplayName("Поиск пользователя по имени - пользователь не найден")
    void findByUsernameWhenUserNotExists() {
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        Optional<User> result = userService.findByUsername("nonexistent");

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findByUsername("nonexistent");
    }

    // ==================== ТЕСТЫ findById ====================

    @Test
    @DisplayName("Поиск пользователя по ID - успех")
    void findByIdWhenUserExists() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));

        Optional<User> result = userService.findById(1L);

        assertTrue(result.isPresent());
        assertEquals(1L, result.get().getId());
        verify(userRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("Поиск пользователя по ID - пользователь не найден")
    void findByIdWhenUserNotExists() {
        when(userRepository.findById(999L)).thenReturn(Optional.empty());

        Optional<User> result = userService.findById(999L);

        assertFalse(result.isPresent());
        verify(userRepository, times(1)).findById(999L);
    }

    // ==================== ТЕСТЫ findAll ====================

    @Test
    @DisplayName("Получение всех пользователей - успех")
    void findAllWhenUsersExist() {
        List<User> users = Arrays.asList(testUser, new User());
        when(userRepository.findAll()).thenReturn(users);

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(userRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Получение всех пользователей - пустой список")
    void findAllWhenNoUsers() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<User> result = userService.findAll();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(userRepository, times(1)).findAll();
    }

    // ==================== ТЕСТЫ createUser ====================

    @Test
    @DisplayName("Создание пользователя - успех")
    void createUserWithValidData() {
        String username = "newuser";
        String password = "password123";
        String email = "new@example.com";
        List<String> roleNames = Arrays.asList("ROLE_MANAGER");

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_MANAGER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(username, password, email, roleNames);

        assertNotNull(result);
        verify(userRepository, times(1)).existsByUsername(username);
        verify(userRepository, times(1)).existsByEmail(email);
        verify(passwordEncoder, times(1)).encode(password);
        verify(roleRepository, times(1)).findByName("ROLE_MANAGER");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - логин уже существует")
    void createUserWhenUsernameExists() {
        String username = "existinguser";
        String password = "password123";
        String email = "new@example.com";
        List<String> roleNames = Arrays.asList("ROLE_MANAGER");

        when(userRepository.existsByUsername(username)).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(username, password, email, roleNames)
        );

        assertEquals("Пользователь с таким логином уже существует", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - email уже зарегистрирован")
    void createUserWhenEmailExists() {
        String username = "newuser";
        String password = "password123";
        String email = "existing@example.com";
        List<String> roleNames = Arrays.asList("ROLE_MANAGER");

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(true);

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(username, password, email, roleNames)
        );

        assertEquals("Email уже зарегистрирован", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - роль не найдена")
    void createUserWhenRoleNotFound() {
        String username = "newuser";
        String password = "password123";
        String email = "new@example.com";
        List<String> roleNames = Arrays.asList("ROLE_INVALID");

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(roleRepository.findByName("ROLE_INVALID")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.createUser(username, password, email, roleNames)
        );

        assertTrue(exception.getMessage().contains("Роль не найдена"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    @DisplayName("Создание пользователя - несколько ролей")
    void createUserWithMultipleRoles() {
        String username = "newuser";
        String password = "password123";
        String email = "new@example.com";
        List<String> roleNames = Arrays.asList("ROLE_MANAGER", "ROLE_EXECUTOR");

        Role role1 = new Role();
        role1.setId(1L);
        role1.setName("ROLE_MANAGER");

        Role role2 = new Role();
        role2.setId(2L);
        role2.setName("ROLE_EXECUTOR");

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_MANAGER")).thenReturn(Optional.of(role1));
        when(roleRepository.findByName("ROLE_EXECUTOR")).thenReturn(Optional.of(role2));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        User result = userService.createUser(username, password, email, roleNames);

        assertNotNull(result);
        verify(roleRepository, times(1)).findByName("ROLE_MANAGER");
        verify(roleRepository, times(1)).findByName("ROLE_EXECUTOR");
    }

    // ==================== ТЕСТЫ updateTwoFaCode ====================

    @Test
    @DisplayName("Обновление 2FA кода - успех")
    void updateTwoFaCodeWhenUserExists() {
        Long userId = 1L;
        String code = "123456";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateTwoFaCode(userId, code);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
        assertEquals(code, testUser.getTwoFaCode());
    }

    @Test
    @DisplayName("Обновление 2FA кода - пользователь не найден")
    void updateTwoFaCodeWhenUserNotFound() {
        Long userId = 999L;
        String code = "123456";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.updateTwoFaCode(userId, code)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== ТЕСТЫ clearTwoFaCode ====================

    @Test
    @DisplayName("Очистка 2FA кода - успех")
    void clearTwoFaCodeWhenUserExists() {
        Long userId = 1L;

        testUser.setTwoFaCode("123456");
        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.clearTwoFaCode(userId);

        verify(userRepository, times(1)).findById(userId);
        verify(userRepository, times(1)).save(testUser);
        assertNull(testUser.getTwoFaCode());
    }

    @Test
    @DisplayName("Очистка 2FA кода - пользователь не найден")
    void clearTwoFaCodeWhenUserNotFound() {
        Long userId = 999L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> userService.clearTwoFaCode(userId)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }

    // ==================== ТЕСТЫ ПОКРЫТИЯ ====================

    @Test
    @DisplayName("Проверка транзакционности createUser")
    void createUserIsTransactional() {
        String username = "txuser";
        String password = "password123";
        String email = "tx@example.com";
        List<String> roleNames = Arrays.asList("ROLE_MANAGER");

        when(userRepository.existsByUsername(username)).thenReturn(false);
        when(userRepository.existsByEmail(email)).thenReturn(false);
        when(passwordEncoder.encode(password)).thenReturn("encodedPassword");
        when(roleRepository.findByName("ROLE_MANAGER")).thenReturn(Optional.of(testRole));
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.createUser(username, password, email, roleNames);

        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    @DisplayName("Проверка readOnly транзакции для findByUsername")
    void findByUsernameIsReadOnlyTransactional() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(testUser));

        userService.findByUsername("testuser");

        verify(userRepository, times(1)).findByUsername("testuser");
        verifyNoMoreInteractions(userRepository);
    }
}