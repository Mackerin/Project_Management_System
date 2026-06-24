package com.projectsystem.model.enums;

/**
 * Приоритеты задачи.
 *
 * @author Евдокимов Д.А.
 */
public enum TaskPriority {
    LOW("Низкий"),
    MEDIUM("Средний"),
    HIGH("Высокий");

    private final String displayName;

    TaskPriority(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}