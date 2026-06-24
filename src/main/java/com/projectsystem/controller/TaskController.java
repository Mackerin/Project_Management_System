package com.projectsystem.controller;

import com.projectsystem.model.entity.Task;
import com.projectsystem.model.enums.TaskStatus;
import com.projectsystem.model.enums.TaskPriority;
import com.projectsystem.service.TaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Контроллер для управления задачами.
 *
 * @author Евдокимов Д.А.
 */
@Controller
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;

    @GetMapping
    public String listTasks(Model model,
                            @RequestParam(required = false) Long projectId,
                            @RequestParam(required = false) String status) {
        List<Task> tasks;
        if (projectId != null) {
            tasks = taskService.findByProjectId(projectId);
        } else if (status != null) {
            tasks = taskService.findByStatus(TaskStatus.valueOf(status));
        } else {
            tasks = taskService.findAll();
        }
        model.addAttribute("tasks", tasks);
        return "tasks/list";
    }

    @GetMapping("/{id}")
    public String taskDetails(@PathVariable Long id, Model model) {
        Task task = taskService.findById(id)
                .orElseThrow(() -> new RuntimeException("Задача не найдена"));
        model.addAttribute("task", task);
        return "tasks/details";
    }

    @GetMapping("/create")
    public String createForm(Model model) {
        model.addAttribute("task", new Task());
        model.addAttribute("statuses", TaskStatus.values());
        model.addAttribute("priorities", TaskPriority.values());
        return "tasks/create";
    }

    @PostMapping("/create")
    public String createTask(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam TaskPriority priority,
                             @RequestParam LocalDate deadline,
                             @RequestParam Long projectId) {
        // Упрощённая версия - в реальном приложении нужно передавать creator и executor
        taskService.createTask(title, description, priority, deadline, projectId, null, null);
        return "redirect:/tasks";
    }

    @PostMapping("/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam TaskStatus status) {
        // В реальном приложении нужно передавать currentUser
        taskService.updateStatus(id, status, null);
        return "redirect:/tasks/" + id;
    }
}