package com.projectsystem.service;

import com.projectsystem.config.TwoFaConfig;
import com.projectsystem.model.entity.User;
import com.projectsystem.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Random;

/**
 * Сервис для эмуляции двухфакторной аутентификации.
 * Код выводится в консоль сервера (эмуляция SMS).
 *
 * @author Евдокимов Д.А.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class TwoFaService {

    private final TwoFaConfig twoFaConfig;
    private final UserRepository userRepository;

    /**
     * Генерирует случайный код подтверждения и сохраняет его для пользователя.
     * Код выводится в лог сервера (эмуляция отправки SMS).
     */
    @Transactional
    public String generateCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        String code = generateRandomCode();
        LocalDateTime expiration = LocalDateTime.now()
                .plusMinutes(twoFaConfig.getExpirationMinutes());

        user.setTwoFaCode(code);
        user.setTwoFaCodeExpiration(expiration);
        userRepository.save(user);

        // ЭМУЛЯЦИЯ SMS - вывод кода в консоль
        log.info("===========================================");
        log.info("  [SMS SERVICE] To user '{}' code: {}",
                user.getUsername(), code);
        log.info("  Code expires at: {}", expiration);
        log.info("===========================================");

        return code;
    }

    /**
     * Проверяет введённый код подтверждения.
     */
    @Transactional
    public boolean verifyCode(Long userId, String inputCode) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));

        if (user.getTwoFaCode() == null) {
            return false;
        }

        if (user.getTwoFaCodeExpiration() == null ||
                LocalDateTime.now().isAfter(user.getTwoFaCodeExpiration())) {
            // Код истёк
            user.setTwoFaCode(null);
            user.setTwoFaCodeExpiration(null);
            userRepository.save(user);
            return false;
        }

        boolean isValid = user.getTwoFaCode().equals(inputCode);

        if (isValid) {
            // Очищаем код после успешной проверки
            user.setTwoFaCode(null);
            user.setTwoFaCodeExpiration(null);
            userRepository.save(user);
        }

        return isValid;
    }

    /**
     * Генерирует случайный 6-значный код.
     */
    private String generateRandomCode() {
        Random random = new Random();
        int code = 100000 + random.nextInt(900000);
        return String.valueOf(code);
    }
}