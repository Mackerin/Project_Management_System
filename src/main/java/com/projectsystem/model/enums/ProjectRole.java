package com.projectsystem.model.enums;

/**
 * Роли пользователя в проекте.
 *
 * @author Евдокимов Д.А.
 */
public enum ProjectRole {
    MANAGER("Руководитель"),
    EXECUTOR("Исполнитель"),
    OBSERVER("Наблюдатель");

    private final String displayName;

    ProjectRole(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}