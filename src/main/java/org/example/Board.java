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

    public void addTask(List<Task> list, Task task) {
        list.add(task);
    }

    public ArrayList<Task> getTodo() {
        return todo;
    }

    public ArrayList<Task> getDoing() {
        return doing;
    }

    public ArrayList<Task> getDone() {
        return done;
    }

    public void getTasks(ArrayList<Task> arrayList) {

        if (arrayList.isEmpty()) {
            System.out.println("(No tasks)");
        } else {
            for (Task task : arrayList) {
                System.out.println(task);
            }
        }
    }

    public ArrayList<Task> getList(String name) {
        if ("todo".equalsIgnoreCase(name)) return getTodo();
        if ("doing".equalsIgnoreCase(name)) return getDoing();
        if ("done".equalsIgnoreCase(name)) return getDone();
        System.out.println("Select a valid list");
        return null;
    }

    public Task getTaskById(ArrayList<Task> arrayList, int id) {
        for (Task task : arrayList) {
            if (task.getId() == id) {
                return task;
            }
        }
        System.out.println("Task not found");
        return null;
    }

    public void removeTaskById(ArrayList<Task> arrayList, int id) {
        arrayList.removeIf(task -> task.getId() == id);
    }

    // move a task from list1 to list2
    public void moveTaskById(ArrayList<Task> list1, ArrayList<Task> list2, int id) {
        Task task = this.getTaskById(list1, id);

        if (task != null) {
            this.removeTaskById(list1, id);
            this.addTask(list2, task);
            System.out.println("Task moved successfully");
        } else {
            System.out.println("Task not found");
        }
    }
}
