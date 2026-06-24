package com.projectsystem.model.enums;

/**
 * Статусы проекта.
 *
 * @author Евдокимов Д.А.
 */
public enum ProjectStatus {
    ACTIVE("Активный"),
    ON_HOLD("Приостановлен"),
    CLOSED("Завершён");

    private final String displayName;

    ProjectStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}