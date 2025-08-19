package com.charlesfitzgerald.kanban;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final ArrayList<Task> todo;
    private final ArrayList<Task> doing;
    private final ArrayList<Task> done;
    private long nextId;
    private static final String SAVE_FILE = "board.json";

    // Default constructor (starts empty)
    public Board() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    // Constructor that accepts starting tasks
    public Board(List<Task> todo, List<Task> doing, List<Task> done) {
        this.todo = new ArrayList<>(todo);
        this.doing = new ArrayList<>(doing);
        this.done = new ArrayList<>(done);
        this.nextId = 0;
    }

    // enum version of listing
    public List<Task> get(Column column) {
        return switch (column) {
            case TODO -> this.todo;
            case DOING -> this.doing;
            case DONE -> this.done;
        };
    }

    // enum version to add tasks to todo by default
    public void add(Task task) {
        get(Column.TODO).add(task);
    }

    // function to find task in enum Columns by id
    // will search TODO -> DOING -> DONE
    public Task find(long id) {
        for (Column col : Column.values()) {
            for (Task t : get(col)) {
                if (t.getId() == id) {
                    return t;
                }
            }

        }
        return null;
    }

    public String getColName(long id) {
        for (Column col : Column.values()) {
            for (Task t : get(col)) {
                if(t.getId() == id) {
                    return col.name();
                }
            }
        }
        return null;
    }

    public Column getCol(long id) {
        for (Column col : Column.values()) {
            for (Task t : get(col)) {
                if(t.getId() == id) {
                    return col;
                }
            }
        }
        return null;
    }

    // function to remove task from list using enum
    public boolean remove(long id) {
        for (Column col : Column.values()) {
            List<Task> list = get(col);

            for (Task t : list) {
                if (t.getId() == id) {
                    list.remove(t);
                    return true;
                }
            }
        }

        return false;
    }

    // function to move task from one list to another using enum
    public boolean move(long id, Column to) {
        List<Task> toList = get(to);
        if (toList == null) {
            System.out.println("You shouldn't have made it to this branch");
            return false;
        }

        Task moveTask = find(id);

        if (moveTask != null) {
            remove(id);
            toList.add(moveTask);
            return true;
        } else {
            return false;
        }
    }

    public void edit(Task t, String title, String description, int priority) {
        t.setTitle(title);
        t.setDescription(description);
        t.setPriority(priority);
    }

    public void getTasks(List<Task> arrayList) {

        if (arrayList.isEmpty()) {
            System.out.println("(No tasks)");
        } else {
            for (Task task : arrayList) {
                System.out.println(task);
            }
        }
    }

    public SaveData toSaveData() {
        List<Task> todoCopy = getTodo();
        List<Task> doingCopy = getDoing();
        List<Task> doneCopy = getDone();

        return new SaveData(todoCopy, doingCopy, doneCopy);
    }

    public void loadFrom(SaveData data) {
        todo.clear();
        doing.clear();
        done.clear();

        todo.addAll(data.getTodo());
        doing.addAll(data.getDoing());
        done.addAll(data.getDone());
    }

    public boolean save() {
        SaveData data = toSaveData();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        try(FileWriter writer = new FileWriter(SAVE_FILE)) {
            gson.toJson(data, writer);
            return true;
        } catch (IOException e) {
            return false;
        }
    }

    public long getMaxId() {
        long maxId = 0;

        for (Column col : Column.values()) {
            List<Task> list = get(col);

            for (Task t : list) {
                if (t.getId() > maxId) {
                    maxId = t.getId();
                }
            }
        }

        return maxId;
    }

    public boolean load() {
        Gson gson = new Gson();
        try (FileReader reader = new FileReader(SAVE_FILE)) {
            SaveData data = gson.fromJson(reader, SaveData.class);
            loadFrom(data);
            nextId = getMaxId() + 1;
            return true;
        } catch (IOException e) {
            return false;
        }
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

    // simple getter method for seeing what the next id will be
    public long getNextId() {
        return nextId;
    }

    public void setNextId() {
        nextId += 1;
    }

    // method to assign a task an id, then increase the global nextId count
    public long nextId() {
        long nextId = getNextId();

        setNextId();

        return nextId;
    }

    public String getSaveFilePath() {
        return SAVE_FILE;
    }


}
