package com.projectsystem.model.enums;

/**
 * Статусы задачи в системе управления проектами.
 *
 * @author Евдокимов Д.А.
 */
public enum TaskStatus {
    NEW("Новая"),
    IN_PROGRESS("В работе"),
    REVIEW("На проверке"),
    DONE("Завершена");

    private final String displayName;

    TaskStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}