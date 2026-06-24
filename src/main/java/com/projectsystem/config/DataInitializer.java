package com.projectsystem.config;

import com.projectsystem.model.entity.User;
import com.projectsystem.model.entity.Role;
import com.projectsystem.repository.UserRepository;
import com.projectsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.Set;

/**
 * Инициализирует начальные данные при запуске приложения.
 * Создает пользователя admin, если он не существует.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            log.info("🔧 Создание пользователя admin...");

            User admin = new User();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setEmail("admin@example.com");
            admin.setIsEnabled(true);

            Role managerRole = roleRepository.findByName("ROLE_MANAGER")
                    .orElseThrow(() -> new RuntimeException("Роль ROLE_MANAGER не найдена"));

            Set<Role> roles = new HashSet<>();
            roles.add(managerRole);
            admin.setRoles(roles);

            userRepository.save(admin);
            log.info("✅ Пользователь admin создан!");
            log.info("   Логин: admin");
            log.info("   Пароль: admin");
        } else {
            log.info("ℹ️ Пользователь admin уже существует");
        }
    }
}