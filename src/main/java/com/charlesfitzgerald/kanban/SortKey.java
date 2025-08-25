package com.charlesfitzgerald.kanban;

import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public enum SortKey {
    ID(Comparator.comparingLong(Task::getId), "i", "id"),
    TITLE(Comparator.comparing(Task::getTitle, String.CASE_INSENSITIVE_ORDER).thenComparingLong(Task::getId), "t", "title"),
    PRIORITY(Comparator.comparingInt(Task::getPriority).thenComparingLong(Task::getId), "p", "prio", "priority");

    private final Comparator<Task> cmp;
    private final Set<String> aliases;

    // constructor with comparator, and vararg to accept any number of strings
    SortKey(Comparator<Task> cmp, String... aliases) {
        this.cmp = cmp;
        this.aliases = new HashSet<>();
        for(String a : aliases) {
            this.aliases.add(a.toLowerCase());
        }
    }

    public static SortKey fromFlag(String flag) {
        if (flag == null) return null;
        String lower = flag.toLowerCase();

        for (SortKey k : values()) {
            if (k.aliases.contains(lower)) {
                return k;
            }
        }
        return null;
    }

    public Comparator<Task> comparator() {
        return cmp;
    }

    public Comparator<Task> comparator(boolean descending) {
        return descending ? cmp.reversed() : cmp;
    }
}