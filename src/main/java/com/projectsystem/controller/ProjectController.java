package com.projectsystem.controller;

import com.projectsystem.model.entity.Project;
import com.projectsystem.security.CustomUserDetails;
import com.projectsystem.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

/**
 * Контроллер для управления проектами.
 *
 * @author Евдокимов Д.А.
 */
@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;

    @GetMapping
    public String listProjects(Model model, Principal principal) {
        List<Project> projects = projectService.findAll();
        model.addAttribute("projects", projects);

        // ✅ Principal работает с @WithMockUser
        if (principal != null) {
            model.addAttribute("username", principal.getName());
        }

        return "projects/list";
    }

    @GetMapping("/{id}")
    public String projectDetails(@PathVariable Long id, Model model) {
        Project project = projectService.findById(id)
                .orElseThrow(() -> new RuntimeException("Проект не найден"));
        model.addAttribute("project", project);
        return "projects/details";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("project", new Project());
        return "projects/create";
    }

    @PostMapping("/create")
    public String createProject(@RequestParam String name,
                                @RequestParam String description,
                                @RequestParam LocalDate startDate,
                                @RequestParam LocalDate endDate,
                                @AuthenticationPrincipal CustomUserDetails userDetails,
                                Model model) {
        // В реальном приложении нужно получить объект User из БД
        // Здесь упрощённая версия
        projectService.createProject(name, description, null, startDate, endDate);
        return "redirect:/projects";
    }
}