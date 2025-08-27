package com.charlesfitzgerald.kanban;

import java.util.Comparator;
import java.util.List;

import static java.util.Comparator.*;

public class TaskViews {
    public TaskViews() {}

    public static String formatTaskLine(Task t, Column col) {
        return String.format("[#%d] %s (prio %d) in %s",
                t.getId(), t.getTitle(), t.getPriority(), col.name());
    }

    public static String printDetailedTask(Task t, Column col) {
        return String.format("""
                        <<TASK DETAILS>>
                        TASK ID: #%d
                        TITLE: %s
                        DESCRIPTION: %s
                        PRIORITY: %d
                        COLUMN: %s
                        """,
                t.getId(), t.getTitle(), t.getDescription(), t.getPriority(), col.name());
    }

    public static void printSection(String header, List<Task> taskList, Column col) {
        System.out.println(header);
        printList(taskList, col);
    }

    public static void printSectionSorted(String header, List<Task> taskList, Column col, Comparator<Task> cmp) {
        System.out.println(header);
        printListSorted(taskList, col, cmp);
    }

    public static void printList(List<Task> taskList, Column col) {
            for (Task t : taskList) {
                System.out.println(formatTaskLine(t, col));
            }
    }

    public static void printListSorted(List<Task> taskList, Column col, Comparator<Task> cmp) {
        taskList.stream().sorted(cmp)
                .forEach(task -> System.out.println(formatTaskLine(task, col)));
    }

    public static void printAll(Board board) {
        printSection("[TODO]", board.getTodo(), Column.TODO);
        System.out.println();
        printSection("[DOING]", board.getDoing(), Column.DOING);
        System.out.println();
        printSection("[DONE]", board.getDone(), Column.DONE);
        System.out.println();
    }

    public static void printAllSorted(Board board, Comparator<Task> cmp) {
        printSectionSorted("[TODO]", board.getTodo(), Column.TODO, cmp);
        System.out.println();
        printSectionSorted("[DOING]", board.getDoing(), Column.DOING, cmp);
        System.out.println();
        printSectionSorted("[DONE]", board.getDone(), Column.DONE, cmp);
        System.out.println();
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
