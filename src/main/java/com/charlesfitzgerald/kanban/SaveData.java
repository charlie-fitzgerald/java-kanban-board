package com.charlesfitzgerald.kanban;

import java.util.ArrayList;
import java.util.List;

public class SaveData {
    private final ArrayList<Task> todo;
    private final ArrayList<Task> doing;
    private final ArrayList<Task> done;
    private final String boardName;

    // Default constructor (starts empty)
    public SaveData(String boardName) {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), boardName);
    }

    // Constructor that accepts starting tasks
    public SaveData(List<Task> todo, List<Task> doing, List<Task> done, String boardName) {
        this.todo = new ArrayList<>(todo);
        this.doing = new ArrayList<>(doing);
        this.done = new ArrayList<>(done);
        this.boardName = boardName;
    }



    public List<Task> getTodo() {
        return todo;
    }

    public List<Task> getDoing() {
        return doing;
    }

    public List<Task> getDone() {
        return done;
    }

    public String getBoardName() { return boardName;}


}
