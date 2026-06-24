package com.projectsystem.service;

import com.projectsystem.model.entity.Notification;
import com.projectsystem.model.entity.User;
import com.projectsystem.repository.NotificationRepository;
import com.projectsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Сервис для управления уведомлениями.
 *
 * @author Евдокимов Д.А.
 */
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<Notification> findByUserId(Long userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public List<Notification> findUnreadByUserId(Long userId) {
        return notificationRepository.findByUserIdAndIsReadFalse(userId);
    }

    @Transactional(readOnly = true)
    public long countUnread(Long userId) {
        return notificationRepository.countByUserIdAndIsReadFalse(userId);
    }

    @Transactional
    public Notification createNotification(Long userId, String message) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        Notification notification = new Notification();
        notification.setUser(user);
        notification.setMessage(message);
        notification.setIsRead(false);

        return notificationRepository.save(notification);
    }

    @Transactional
    public void markAsRead(Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Уведомление не найдено"));
        notification.setIsRead(true);
        notificationRepository.save(notification);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        List<Notification> notifications = notificationRepository.findByUserIdAndIsReadFalse(userId);
        for (Notification notification : notifications) {
            notification.setIsRead(true);
        }
        notificationRepository.saveAll(notifications);
    }
}