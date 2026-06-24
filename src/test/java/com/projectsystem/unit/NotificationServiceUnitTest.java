package com.projectsystem.unit;

import com.projectsystem.model.entity.Notification;
import com.projectsystem.model.entity.User;
import com.projectsystem.repository.NotificationRepository;
import com.projectsystem.repository.UserRepository;
import com.projectsystem.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Модульные тесты для NotificationService.
 *
 * @author Евдокимов Д.А.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Тесты NotificationService")
class NotificationServiceUnitTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private NotificationService notificationService;

    private User testUser;
    private Notification testNotification;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setEmail("test@example.com");
        testUser.setIsEnabled(true);

        testNotification = new Notification();
        testNotification.setId(1L);
        testNotification.setUser(testUser);
        testNotification.setMessage("Тестовое уведомление");
        testNotification.setIsRead(false);
        testNotification.setCreatedAt(LocalDateTime.now());
    }

    // ==================== ТЕСТЫ findByUserId ====================

    @Test
    @DisplayName("Поиск уведомлений по ID пользователя - успех")
    void findByUserIdWhenNotificationsExist() {
        List<Notification> notifications = Arrays.asList(
                testNotification,
                createNotification(2L, "Второе уведомление"),
                createNotification(3L, "Третье уведомление")
        );
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(notifications);

        List<Notification> result = notificationService.findByUserId(1L);

        assertNotNull(result);
        assertEquals(3, result.size());
        assertEquals("Тестовое уведомление", result.get(0).getMessage());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
    }

    @Test
    @DisplayName("Поиск уведомлений по ID пользователя - пустой список")
    void findByUserIdWhenNoNotifications() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.findByUserId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
    }

    // ==================== ТЕСТЫ findUnreadByUserId ====================

    @Test
    @DisplayName("Поиск непрочитанных уведомлений - успех")
    void findUnreadByUserIdWhenUnreadExist() {
        Notification unread1 = createNotification(1L, "Непрочитанное 1");
        unread1.setIsRead(false);

        Notification unread2 = createNotification(2L, "Непрочитанное 2");
        unread2.setIsRead(false);

        List<Notification> unreadNotifications = Arrays.asList(unread1, unread2);
        when(notificationRepository.findByUserIdAndIsReadFalse(1L))
                .thenReturn(unreadNotifications);

        List<Notification> result = notificationService.findUnreadByUserId(1L);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertFalse(result.get(0).getIsRead());
        assertFalse(result.get(1).getIsRead());
        verify(notificationRepository, times(1)).findByUserIdAndIsReadFalse(1L);
    }

    @Test
    @DisplayName("Поиск непрочитанных уведомлений - все прочитаны")
    void findUnreadByUserIdWhenAllRead() {
        when(notificationRepository.findByUserIdAndIsReadFalse(1L))
                .thenReturn(Collections.emptyList());

        List<Notification> result = notificationService.findUnreadByUserId(1L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(notificationRepository, times(1)).findByUserIdAndIsReadFalse(1L);
    }

    // ==================== ТЕСТЫ countUnread ====================

    @Test
    @DisplayName("Подсчёт непрочитанных уведомлений - успех")
    void countUnreadWhenUnreadExist() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(5L);

        long result = notificationService.countUnread(1L);

        assertEquals(5L, result);
        verify(notificationRepository, times(1)).countByUserIdAndIsReadFalse(1L);
    }

    @Test
    @DisplayName("Подсчёт непрочитанных уведомлений - ноль")
    void countUnreadWhenNoUnread() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(0L);

        long result = notificationService.countUnread(1L);

        assertEquals(0L, result);
        verify(notificationRepository, times(1)).countByUserIdAndIsReadFalse(1L);
    }

    // ==================== ТЕСТЫ createNotification ====================

    @Test
    @DisplayName("Создание уведомления - успех")
    void createNotificationWithValidData() {
        Long userId = 1L;
        String message = "Новое уведомление";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> {
                    Notification notification = invocation.getArgument(0);
                    notification.setId(100L);
                    return notification;
                });

        Notification result = notificationService.createNotification(userId, message);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(message, result.getMessage());
        assertFalse(result.getIsRead());
        assertNotNull(result.getUser());
        assertEquals(userId, result.getUser().getId());

        verify(userRepository, times(1)).findById(userId);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Создание уведомления - пользователь не найден")
    void createNotificationWhenUserNotFound() {
        Long userId = 999L;
        String message = "Новое уведомление";

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService.createNotification(userId, message)
        );

        assertEquals("Пользователь не найден", exception.getMessage());
        verify(userRepository, times(1)).findById(userId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Создание уведомления - проверка что isRead=false")
    void createNotificationSetsIsReadToFalse() {
        Long userId = 1L;
        String message = "Тестовое сообщение";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.createNotification(userId, message);

        assertNotNull(result);
        assertFalse(result.getIsRead(), "Новое уведомление должно быть непрочитанным");
    }

    // ==================== ТЕСТЫ markAsRead ====================

    @Test
    @DisplayName("Отметка уведомления как прочитанное - успех")
    void markAsReadWhenNotificationExistsMarksAsRead() {
        Long notificationId = 1L;
        testNotification.setIsRead(false);

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.of(testNotification));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);

        notificationService.markAsRead(notificationId);

        assertTrue(testNotification.getIsRead());
        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, times(1)).save(testNotification);
    }

    @Test
    @DisplayName("Отметка уведомления как прочитанное - уведомление не найдено")
    void markAsReadWhenNotificationNotFound() {
        Long notificationId = 999L;

        when(notificationRepository.findById(notificationId))
                .thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> notificationService.markAsRead(notificationId)
        );

        assertEquals("Уведомление не найдено", exception.getMessage());
        verify(notificationRepository, times(1)).findById(notificationId);
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    // ==================== ТЕСТЫ markAllAsRead ====================

    @Test
    @DisplayName("Отметка всех уведомлений как прочитанные - успех")
    void markAllAsReadWhenUnreadExist() {
        Long userId = 1L;

        Notification unread1 = createNotification(1L, "Непрочитанное 1");
        unread1.setIsRead(false);

        Notification unread2 = createNotification(2L, "Непрочитанное 2");
        unread2.setIsRead(false);

        List<Notification> unreadNotifications = Arrays.asList(unread1, unread2);
        when(notificationRepository.findByUserIdAndIsReadFalse(userId))
                .thenReturn(unreadNotifications);
        when(notificationRepository.saveAll(anyList()))
                .thenReturn(unreadNotifications);

        notificationService.markAllAsRead(userId);

        assertTrue(unread1.getIsRead());
        assertTrue(unread2.getIsRead());
        verify(notificationRepository, times(1)).findByUserIdAndIsReadFalse(userId);
        verify(notificationRepository, times(1)).saveAll(unreadNotifications);
    }

    @Test
    @DisplayName("Отметка всех уведомлений как прочитанные - нет непрочитанных")
    void markAllAsReadWhenNoUnread() {
        Long userId = 1L;

        when(notificationRepository.findByUserIdAndIsReadFalse(userId))
                .thenReturn(Collections.emptyList());

        notificationService.markAllAsRead(userId);

        verify(notificationRepository, times(1)).findByUserIdAndIsReadFalse(userId);
        verify(notificationRepository, times(1)).saveAll(Collections.emptyList());
    }

    // ==================== ТЕСТЫ ПРОВЕРКИ ТРАНЗАКЦИОННОСТИ ====================

    @Test
    @DisplayName("Проверка транзакционности createNotification")
    void createNotificationIsTransactional() {
        Long userId = 1L;
        String message = "Тест";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);

        notificationService.createNotification(userId, message);

        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Проверка readOnly транзакции для findByUserId")
    void findByUserIdIsReadOnlyTransactional() {
        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(1L))
                .thenReturn(Collections.singletonList(testNotification));

        notificationService.findByUserId(1L);

        verify(notificationRepository, times(1)).findByUserIdOrderByCreatedAtDesc(1L);
        verifyNoMoreInteractions(notificationRepository);
    }

    @Test
    @DisplayName("Проверка транзакционности markAllAsRead")
    void markAllAsReadIsTransactional() {
        Long userId = 1L;
        List<Notification> notifications = Collections.singletonList(testNotification);

        when(notificationRepository.findByUserIdAndIsReadFalse(userId))
                .thenReturn(notifications);
        when(notificationRepository.saveAll(anyList()))
                .thenReturn(notifications);

        notificationService.markAllAsRead(userId);

        verify(notificationRepository, times(1)).saveAll(notifications);
    }

    // ==================== ВСПОМОГАТЕЛЬНЫЙ МЕТОД ====================

    /**
     * Создаёт тестовое уведомление с заданными параметрами.
     */
    private Notification createNotification(Long id, String message) {
        Notification notification = new Notification();
        notification.setId(id);
        notification.setUser(testUser);
        notification.setMessage(message);
        notification.setIsRead(false);
        notification.setCreatedAt(LocalDateTime.now());
        return notification;
    }

    // ==================== ТЕСТЫ ГРАНИЧНЫХ ЗНАЧЕНИЙ ====================

    @Test
    @DisplayName("Создание уведомления с пустым сообщением")
    void createNotificationWithEmptyMessage() {
        Long userId = 1L;
        String message = "";

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));
        when(notificationRepository.save(any(Notification.class)))
                .thenReturn(testNotification);

        Notification result = notificationService.createNotification(userId, message);

        assertNotNull(result);
        verify(notificationRepository, times(1)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Создание уведомления с длинным сообщением")
    void createNotificationWithLongMessage() {
        Long userId = 1L;
        String longMessage = "О".repeat(1000);

        when(userRepository.findById(userId)).thenReturn(Optional.of(testUser));

        when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Notification result = notificationService.createNotification(userId, longMessage);

        assertNotNull(result);
        assertEquals(longMessage, result.getMessage());
    }

    @Test
    @DisplayName("Подсчёт непрочитанных с большим числом")
    void countUnreadWithLargeCount() {
        when(notificationRepository.countByUserIdAndIsReadFalse(1L)).thenReturn(999L);

        long result = notificationService.countUnread(1L);

        assertEquals(999L, result);
    }
}