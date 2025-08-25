package com.charlesfitzgerald.kanban;

import java.util.Comparator;

public enum SortKey {
    ID,
    TITLE,
    PRIORITY;

    public SortKey fromFlag(String userInput) {
        userInput = userInput.trim().toLowerCase();

        return switch (userInput) {
            case "i", "id" -> ID;
            case "p", "prio", "priority" -> PRIORITY;
            case "t", "title" -> TITLE;
            default -> null;
        };
    }

    public Comparator<Task> comparator() {
        return switch (this) {
            case ID -> Comparator.comparingLong(Task::getId);
            case TITLE -> Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER)
                                    .thenComparingLong(Task::getId);
            case PRIORITY -> Comparator.comparingInt(Task::getPriority)
                                       .thenComparingLong(Task::getId);
        };
    }
}