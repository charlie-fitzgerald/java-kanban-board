package org.example;
import java.util.List;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static Column parseColumn(String input) {
        input = input.trim().toLowerCase();

        return switch (input) {
            case "todo" -> Column.TODO;
            case "doing" -> Column.DOING;
            case "done" -> Column.DONE;
            default -> null;
        };

    }

    public static void main(String[] args) {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);
        int taskId = 0;

        while(true) {
            System.out.println("Available commands: add | list | find | move | del | help | quit");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "add":
                    System.out.println("Add a task to a list");
                    System.out.println("You'll be prompted to enter the following info: <title>, <description>, <priority>");

                    System.out.println("Enter title:");
                    String title = scanner.nextLine();

                    System.out.println("Enter description:");
                    String description = scanner.nextLine();

                    int priority; // will be set by the loop
                    while (true) {
                        System.out.println("Enter priority of task as a value between 1–3:");
                        String line = scanner.nextLine().trim();
                        try {
                            int parsed = Integer.parseInt(line);
                            if (parsed < 1 || parsed > 3) {
                                System.out.println("Invalid priority: must be 1, 2, or 3.");
                                continue;
                            }
                            priority = parsed; // valid
                            break;
                        } catch (NumberFormatException e) {
                            System.out.println("Invalid input: enter a number (1–3).");
                        }
                    }

                    Task newTask = new Task(taskId++, title, description, priority);

                    board.add(newTask);

                    System.out.println("New task added successfully!");
                    break;

                case "list":
                    System.out.println("Select list: todo, doing, done");
                    String userListSelection = scanner.nextLine();
                    Column listColumn = parseColumn(userListSelection);

                    if (listColumn == null) {
                        System.out.println("List not found");
                        break;
                    }

                    List<Task> listSelection = board.get(listColumn);
                    board.getTasks(listSelection);
                    break;
                case "find":
                    System.out.println("Enter the id of the task you want to find: ");
                    String idInput = scanner.nextLine();
                    int findTaskId;

                    try {
                        findTaskId = Integer.parseInt(idInput);
                    } catch(NumberFormatException e) {
                        System.out.println("Invalid input. Please enter a number.");
                        break;
                    }

                    Task foundTask = board.find(findTaskId);

                    if (foundTask == null) {
                        System.out.println("Task not found");
                        break;
                    } else {
                        System.out.println("Here's your task: ");
                        System.out.println(foundTask);
                    }
                    break;
                case "move":
                    System.out.println("Move from which list: todo, doing, done");
                    List<Task> fromList = board.getList(scanner.nextLine().toLowerCase());

                    if (fromList == null) {
                        break;
                    }

                    System.out.println("Move to which list: todo, doing, done");
                    List<Task> toList = board.getList(scanner.nextLine().toLowerCase());

                    if (toList == null) {
                        break;
                    }

                    System.out.println("Id of task to move");
                    int moveTaskId = Integer.parseInt(scanner.nextLine());

                    boolean taskMoved = board.moveTaskById(fromList, toList, moveTaskId);

                    if (taskMoved) {
                        System.out.println("Task moved successfully");
                    } else {
                        System.out.println("Task not found");
                    }
                    break;
                case "del":
                    System.out.println("Pick a list to delete a task from: todo, doing, done");
                    List<Task> delList = board.getList(scanner.nextLine().toLowerCase());

                    if (delList == null) {
                        break;
                    }

                    System.out.println("Enter id of task to delete: ");
                    int delId = Integer.parseInt(scanner.nextLine());

                    boolean result = board.removeTaskById(delList, delId);

                    if (result) {
                        System.out.println("Task removed successfully");
                    } else {
                        System.out.println("Task not found");
                    }
                    break;
                case "help":
                    System.out.println("Commands:");
                    System.out.println("  add  - Add a new task to a list");
                    System.out.println("  list - List tasks in a specific list");
                    System.out.println("  find - Find task by id");
                    System.out.println("  move - Move a task between lists");
                    System.out.println("  del  - Delete a task from a list");
                    System.out.println("  quit - Exit the program");
                    break;
                case "quit":
                    System.out.println("Thanks for using this program!");
                    return;
                default:
                    System.out.println("Pick a valid option");
            }
        }

    }
}