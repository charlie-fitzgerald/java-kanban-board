package com.charlesfitzgerald.kanban;

import java.util.Comparator;

public enum SortKey {
    ID, TITLE, PRIORITY;

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