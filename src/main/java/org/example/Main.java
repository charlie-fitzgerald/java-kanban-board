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

    public static int readIntOrFail(Scanner scanner, String prompt) {
        while (true) {
        System.out.println(prompt);
        String input = scanner.nextLine().trim();
            try {
                return Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number");
            }
        }
    }

    public static void main(String[] args) {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);
        int taskId = 0;



        while(true) {
            System.out.println("Available commands: add | list | find | move | del | save | load | help | quit");
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
                        System.out.println("Enter priority of task as a value between 1-3:");
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
                    String findPrompt = "Enter the id of the task you want to find: ";
                    int findTaskId = readIntOrFail(scanner, findPrompt);

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
                    String movePrompt = "Enter the id of task you want to move: ";

                    int moveTaskId = readIntOrFail(scanner, movePrompt);

                    System.out.println("Enter which list you want to move the task to: todo, doing, done");
                    String moveInput = scanner.nextLine();

                    Column toList = parseColumn(moveInput);

                    if (toList == null) {
                        System.out.println("Enter a valid list");
                        break;
                    }

                    boolean taskMoved = board.move(moveTaskId, toList);

                    if (taskMoved) {
                        System.out.println("Task moved successfully");
                    } else {
                        System.out.println("Task not found");
                    }
                    break;
                case "del":
                    String delPrompt = "Enter id of task to delete: ";
                    int delId = readIntOrFail(scanner, delPrompt);

                    boolean result = board.remove(delId);

                    if (result) {
                        System.out.println("Task removed successfully");
                    } else {
                        System.out.println("Task not found");
                    }
                    break;
                case "save":
                    boolean save = board.save();
                    if (save) {
                        System.out.printf("Board successfully saved to %s%n", board.getSaveFilePath());
                    } else {
                        System.out.printf("Failed to save board to %s%n", board.getSaveFilePath());
                    }
                    break;
                case "load":
                    boolean load = board.load();

                    if (load) {
                        System.out.printf("Data successfully loaded from %s%n", board.getSaveFilePath());
                    } else {
                        System.out.printf("Loading data from %s%n failed", board.getSaveFilePath());
                    }

                    break;
                case "help":
                    System.out.println("Commands:");
                    System.out.println("  add  - Add a new task to a list");
                    System.out.println("  list - List tasks in a specific list");
                    System.out.println("  find - Find task by id");
                    System.out.println("  move - Move a task between lists");
                    System.out.println("  del  - Delete a task by entering its id");
                    System.out.println("  save  - Save the current data");
                    System.out.println("  load  - Load data from a save file");
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