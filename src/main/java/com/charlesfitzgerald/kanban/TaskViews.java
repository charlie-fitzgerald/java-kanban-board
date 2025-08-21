package com.charlesfitzgerald.kanban;

import java.util.Comparator;
import java.util.List;

import static com.charlesfitzgerald.kanban.Main.formatTaskLine;
import static java.util.Comparator.*;

public class TaskViews {
    public TaskViews() {}

    public static void printList(List<Task> taskList, Column col) {
            for (Task t : taskList) {
                System.out.println(formatTaskLine(t, col));
            }
    }

    public static void printListSorted(List<Task> taskList, Column col, Comparator<Task> cmp) {
        taskList.stream().sorted(cmp)
                .forEach(task -> System.out.println(formatTaskLine(task, col)));
    }

    public static Comparator<Task> byId() {
        return comparingLong(Task::getId);
    }

    public static Comparator<Task> byTitle() {
        return comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER).thenComparingLong(Task::getId);
    }

    public static Comparator<Task> byPriority() {
        return comparingInt(Task::getPriority).thenComparingLong(Task::getId);
    }
}
