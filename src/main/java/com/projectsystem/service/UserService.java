package com.projectsystem.service;

import com.projectsystem.model.entity.User;
import com.projectsystem.model.entity.Role;
import com.projectsystem.repository.UserRepository;
import com.projectsystem.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Сервис для управления пользователями.
 *
 * @author Евдокимов Д.А.
 */
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(String username, String password, String email, List<String> roleNames) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Пользователь с таким логином уже существует");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email уже зарегистрирован");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setIsEnabled(true);

        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Роль не найдена: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);

        return userRepository.save(user);
    }

    @Transactional
    public void updateTwoFaCode(Long userId, String code) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setTwoFaCode(code);
        userRepository.save(user);
    }

    @Transactional
    public void clearTwoFaCode(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Пользователь не найден"));
        user.setTwoFaCode(null);
        userRepository.save(user);
    }
}