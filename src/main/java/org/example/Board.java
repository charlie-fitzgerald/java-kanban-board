package org.example;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final ArrayList<Task> todo;
    private final ArrayList<Task> doing;
    private final ArrayList<Task> done;

    // Default constructor (starts empty)
    public Board() {
        this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
    }

    // Constructor that accepts starting tasks
    public Board(List<Task> todo, List<Task> doing, List<Task> done) {
        this.todo = new ArrayList<>(todo);
        this.doing = new ArrayList<>(doing);
        this.done = new ArrayList<>(done);
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
    public Task find(int id) {
        for (Column col : Column.values()) {
            for (Task t : get(col)) {
                if (t.getId() == id) {
                    return t;
                }
            }

        }
        return null;
    }

    // function to remove task from list using enum
    public boolean remove(int id) {
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
    public boolean move(int id, Column to) {
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

    // CLI Version for String input to add tasks to list
    public void addTask(List<Task> list, Task task) {
        list.add(task);
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

    public void getTasks(List<Task> arrayList) {

        if (arrayList.isEmpty()) {
            System.out.println("(No tasks)");
        } else {
            for (Task task : arrayList) {
                System.out.println(task);
            }
        }
    }

    public List<Task> getList(String name) {
        if ("todo".equalsIgnoreCase(name)) return getTodo();
        if ("doing".equalsIgnoreCase(name)) return getDoing();
        if ("done".equalsIgnoreCase(name)) return getDone();
        System.out.println("Select a valid list");
        return null;
    }

    public Task getTaskById(List<Task> list, int id) {
        for (Task task : list) {
            if (task.getId() == id) {
                return task;
            }
        }

        return null;
    }

    public boolean removeTaskById(List<Task> list, int id) {
        for (Task task : list) {
            if (task.getId() == id) {
                list.remove(task);
                return true;
            }
        }
        return false;
    }

    // move a task from list1 to list2
    public boolean moveTaskById(List<Task> list1, List<Task> list2, int id) {
        Task task = this.getTaskById(list1, id);

        if (task != null) {
            this.removeTaskById(list1, id);
            this.addTask(list2, task);
            return true;
        } else {
            return false;
        }
    }
}
