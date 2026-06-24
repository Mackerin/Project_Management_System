package com.projectsystem.controller;

import com.projectsystem.model.dto.TwoFaRequest;
import com.projectsystem.security.CustomUserDetails;
import com.projectsystem.service.TwoFaService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Контроллер для аутентификации и 2FA.
 */
@Controller
@RequiredArgsConstructor
@Slf4j
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final TwoFaService twoFaService;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @PostMapping("/login")
    public String login(String username, String password, HttpSession session,
                        RedirectAttributes redirectAttributes) {
        try {
            // Первая стадия аутентификации - проверка логина/пароля
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password));

            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Генерируем код 2FA и сохраняем ID пользователя в сессии
            twoFaService.generateCode(userDetails.getId());
            session.setAttribute("pendingUserId", userDetails.getId());
            session.setAttribute("pendingAuthentication", authentication);

            log.info("User {} passed first auth stage, proceeding to 2FA", username);

            return "redirect:/two-factor";

        } catch (Exception e) {
            log.error("Login failed: {}", e.getMessage());
            redirectAttributes.addFlashAttribute("error", "Неверный логин или пароль");
            return "redirect:/login?error=true";
        }
    }

    @GetMapping("/two-factor")
    public String twoFactorPage() {
        return "auth/two-factor";
    }

    @PostMapping("/two-factor")
    public String twoFactorSubmit(@ModelAttribute TwoFaRequest request,
                                  HttpSession session,
                                  jakarta.servlet.http.HttpServletRequest httpRequest,
                                  jakarta.servlet.http.HttpServletResponse httpResponse,
                                  RedirectAttributes redirectAttributes) {
        Long userId = (Long) session.getAttribute("pendingUserId");

        if (userId == null) {
            return "redirect:/login";
        }

        boolean isValid = twoFaService.verifyCode(userId, request.getCode());

        if (isValid) {
            // ✅ Получаем Authentication из сессии
            Authentication authentication = (Authentication) session.getAttribute("pendingAuthentication");

            if (authentication != null) {
                // ✅ Создаём SecurityContext и сохраняем в HTTP-сессию
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication);
                SecurityContextHolder.setContext(securityContext);

                // ✅ ВАЖНО: Сохраняем SecurityContext в HTTP-сессию через SecurityContextRepository
                securityContextRepository.saveContext(securityContext, httpRequest, httpResponse);
            }

            // Очистка временных данных сессии
            session.removeAttribute("pendingUserId");
            session.removeAttribute("pendingAuthentication");

            log.info("User completed 2FA successfully");
            return "redirect:/dashboard";
        } else {
            redirectAttributes.addFlashAttribute("error", "Неверный код подтверждения");
            return "redirect:/two-factor?error=true";
        }
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        SecurityContextHolder.clearContext();
        return "redirect:/login?logout=true";
    }
}