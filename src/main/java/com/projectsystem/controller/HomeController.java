package com.projectsystem.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Контроллер для главных страниц приложения.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@AuthenticationPrincipal UserDetails userDetails) {
        // ✅ ПРОВЕРЯЕМ аутентификацию перед редиректом
        if (userDetails == null) {
            return "redirect:/login";  // ← Неаутентифицированных на login
        }
        return "redirect:/dashboard";  // ← Аутентифицированных на dashboard
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        if (userDetails == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", userDetails.getUsername());
        return "dashboard/index";
    }
}