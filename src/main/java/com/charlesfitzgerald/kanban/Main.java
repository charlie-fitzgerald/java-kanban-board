package com.charlesfitzgerald.kanban;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static String VERSION = "0.2.0";

    @org.jetbrains.annotations.Nullable
    public static Column parseColumn(String input) {
        input = input.trim().toLowerCase();

        return switch (input) {
            case "todo" -> Column.TODO;
            case "doing" -> Column.DOING;
            case "done" -> Column.DONE;
            default -> null;
        };

    }

    public static int readIntOrFail(@NotNull Scanner scanner, String prompt) {
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

    public static long readLongOrFail(@NotNull Scanner scanner, String prompt) {
        while(true) {
            System.out.println(prompt);
            System.out.print("> ");
            String input = scanner.nextLine().trim();
            try {
                return Long.parseLong(input);
            } catch (NumberFormatException e) {
                System.out.println("Enter a valid number");
            }
        }
    }

    public static void main(String[] args) {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);

        boolean loadOnStartUp = board.load();

        if (loadOnStartUp) {
            System.out.printf("Board successfully loaded from %s%n", board.getSaveFilePath());
        }

        while(true) {
            System.out.println("Available commands: add | list | find | move | edit | del | save | load | version | help | quit");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "add":
                    System.out.println("Add a task to a list");
                    System.out.println("You'll be prompted to enter the following info: <title>, <description>, <priority>");

                    System.out.println("Enter title:");
                    System.out.print("> ");
                    String title = scanner.nextLine();

                    System.out.println("Enter description:");
                    System.out.print("> ");
                    String description = scanner.nextLine();

                    int priority; // will be set by the loop
                    while (true) {
                        System.out.println("Enter priority of task as a value between 1-3:");
                        System.out.print("> ");
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

                    Task newTask = new Task(board.nextId(), title, description, priority);

                    board.add(newTask);

                    System.out.println("New task added successfully!");
                    break;

                case "list":
                    System.out.println("Select list: todo, doing, done");
                    System.out.print("> ");
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
                    System.out.print("> ");
                    long findTaskId = readLongOrFail(scanner, findPrompt);

                    Task foundTask = board.find(findTaskId);
                    String foundTaskCol = board.getColName(findTaskId);

                    if (foundTask == null) {
                        System.out.println("Task not found");
                        break;
                    } else {
                        System.out.println("Task found in " + foundTaskCol);
                        System.out.println(foundTask);
                    }
                    break;
                case "move":
                    String movePrompt = "Enter the id of task you want to move: ";
                    System.out.print("> ");

                    long moveTaskId = readLongOrFail(scanner, movePrompt);

                    Task moveTask = board.find(moveTaskId);

                    if (moveTask == null) {
                        System.out.println("Task not found");
                        break;
                    }

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
                case "edit":
                    String editPrompt = "Enter id of task to edit: ";
                    long editId = readLongOrFail(scanner, editPrompt);
                    Task editTask = board.find(editId);

                    if (editTask == null) {
                        System.out.println("Task not found");
                        break;
                    }

                    String newTitle;
                    String newDescription;
                    int newPriority;
                    boolean editTitle = true;
                    boolean editDescription = true;
                    boolean editPriority = true;

                    while (true){
                        System.out.println("Current title: " + editTask.getTitle());
                        System.out.println("Edit title? (y or n)");
                        System.out.print("> ");
                        String editTitleQuery = scanner.nextLine().trim().toLowerCase();

                        if (editTitleQuery.equals("y")) {
                            System.out.println("Enter new title: ");
                            System.out.print("> ");
                            newTitle = scanner.nextLine();
                            break;
                        } else if (editTitleQuery.equals("n")) {
                            System.out.println("Title not changed");
                            newTitle = editTask.getTitle();
                            editTitle = false;
                            break;
                        } else {
                            System.out.println("Enter y or n");
                        }
                    }
                    while (true){
                        System.out.println("Current description: " + editTask.getDescription());
                        System.out.println("Edit Description? (y or n)");
                        String editDescriptionQuery = scanner.nextLine().trim().toLowerCase();

                        if (editDescriptionQuery.equals("y")) {
                            System.out.println("Enter new description: ");
                            System.out.print("> ");
                            newDescription = scanner.nextLine();
                            break;
                        } else if (editDescriptionQuery.equals("n")) {
                            System.out.println("Description not changed");
                            newDescription = editTask.getDescription();
                            editDescription = false;
                            break;
                        } else {
                            System.out.println("Enter y or n");
                        }
                    }
                    while (true){
                        System.out.println("Current priority: " + editTask.getPriority());
                        System.out.println("Edit Priority? (y or n)");
                        String editPriorityQuery = scanner.nextLine().trim().toLowerCase();

                        if (editPriorityQuery.equals("y")) {
                            System.out.println("Enter new priority");
                            System.out.print("> ");
                            newPriority = Integer.parseInt(scanner.nextLine());
                            break;
                        } else if (editPriorityQuery.equals("n")) {
                            newPriority = editTask.getPriority();
                            editPriority= false;
                            break;
                        } else {
                            System.out.println("Enter y or n");
                        }
                    }

                    if (!editTitle && !editDescription && !editPriority) {
                        System.out.println("Task not edited");
                        break;
                    }

                    board.edit(editTask, newTitle, newDescription, newPriority);
                    System.out.println("Task edited successfully");
                    System.out.println("Edited task: " + editTask);

                    break;
                case "del":
                    String delPrompt = "Enter id of task to delete: ";
                    System.out.print("> ");
                    long delId = readLongOrFail(scanner, delPrompt);

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
                    System.out.println("  add     - Add a new task to a list");
                    System.out.println("  list    - List tasks in a specific list. Can use --sorted and --by-priority flags");
                    System.out.println("  find    - Find task by id");
                    System.out.println("  move    - Move a task between lists");
                    System.out.println("  edit    - Edit a task by entering its id");
                    System.out.println("  del     - Delete a task by entering its id");
                    System.out.println("  save    - Save the current data");
                    System.out.println("  load    - Load data from a save file");
                    System.out.println("  version - View the current version of the software");
                    System.out.println("  quit    - Exit the program");
                    break;
                case "version":
                    System.out.println("Kanban CLI - Version " + VERSION);
                    break;
                case "quit":
                    boolean quitSave = board.save();

                    if (quitSave) {
                        System.out.printf("Board successfully saved to %s%n", board.getSaveFilePath());
                    } else {
                        System.out.printf("Failed to save board to %s%n", board.getSaveFilePath());
                    }
                    System.out.println("Thanks for using this program!");

                    return;
                default:
                    System.out.println("Pick a valid option");
            }
        }

    }
}