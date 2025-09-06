package com.charlesfitzgerald.kanban;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final ArrayList<Task> todo;
    private final ArrayList<Task> doing;
    private final ArrayList<Task> done;
    private long nextId;
    private String boardName;
    private static final String DEFAULT_SAVE_DIR = "boards";

    private String saveDir = DEFAULT_SAVE_DIR;

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
        this.boardName = "board";
    }

    // Constructor that accepts starting tasks and name
    public Board(String boardName, List<Task> todo, List<Task> doing, List<Task> done) {
        this.todo = new ArrayList<>(todo);
        this.doing = new ArrayList<>(doing);
        this.done = new ArrayList<>(done);
        this.nextId = 0;
        this.boardName = boardName;
    }

    // enum version of listing
    public List<Task> get(Column column) {
        return switch (column) {
            case TODO -> this.todo;
            case DOING -> this.doing;
            case DONE -> this.done;
        };
    }

    // enum version to add tasks to list
    public void add(Task task, Column addToCol) {
        get(addToCol).add(task);
    }

    // function to find task in enum Columns by id
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
            for (var it = list.iterator(); it.hasNext(); ) {
                Task t = it.next();
                if (t.getId() == id) {
                    it.remove();
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

    public SaveData toSaveData() {
        List<Task> todoCopy = getTodo();
        List<Task> doingCopy = getDoing();
        List<Task> doneCopy = getDone();
        String boardName = getBoardName();

        return new SaveData(todoCopy, doingCopy, doneCopy, boardName);
    }

    public void loadFrom(SaveData data) {
        todo.clear(); doing.clear(); done.clear();
        todo.addAll(data.getTodo());
        doing.addAll(data.getDoing());
        done.addAll(data.getDone());

        String loadedName = data.getBoardName();
        if (loadedName != null && !loadedName.isBlank()) {
            this.boardName = loadedName;
        }
    }

    public boolean save() {
        SaveData data = toSaveData();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();

        String path = getSaveFilePath();
        Path p = java.nio.file.Paths.get(path);

        try {
            // Ensure parent directory exists (no-op if already exists)
            Path parent = p.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Writer writer = Files.newBufferedWriter(p, java.nio.charset.StandardCharsets.UTF_8)) {
                gson.toJson(data, writer);
            }
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
        Path path = Paths.get(getSaveFilePath());
        try (Reader reader = Files.newBufferedReader(path, java.nio.charset.StandardCharsets.UTF_8)) {
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

    // return the current save directory
    public String getSaveDir() {
        return saveDir;
    }

    // set a new save directory for this board
    public void setSaveDir(String saveDir) {
        this.saveDir = saveDir;
    }


    // return a filename to save from the board's name
    public String getSaveFilename() {
        if (getBoardName().isEmpty()) {
            return "board.json";
        }

        return this.boardName + ".json";
    }

    // return a filepath to save a board to using the board's current directory and filename
    public String getSaveFilePath() {
        return Paths.get(getSaveDir(), getSaveFilename()).toString();
    }

    public String getBoardName() {
        return boardName;
    }

    // set the current board name with sanitized input
    public boolean setBoardName(String boardName) {
        String s = (boardName == null) ? "" : boardName.trim();
        if (s.isEmpty()) return false; // ignore empty
        if (s.contains("/") || s.contains("\\") || s.contains("..")) return false; // ignore invalid
        this.boardName = s;
        return true;
    }



}
