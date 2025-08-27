package com.charlesfitzgerald.kanban;
import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
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

    // Normalizes to "y", "n", or "q"; keeps prompting until valid.
    static String askYesNoQuit(Scanner scanner, String prompt) {
        while (true) {
            System.out.println(prompt);
            System.out.print("> ");
            String a = scanner.nextLine().trim().toLowerCase();
            switch (a) {
                case "y", "yes" -> {
                    return "y";
                }
                case "n", "no" -> {
                    return "n";
                }
                case "q", "quit" -> {
                    return "q";
                }
            }
            System.out.println("Enter y or n | q to quit");
        }
    }

    // Reads a priority in [1..3]; keeps prompting until valid.
    static int readPriority1to3(Scanner scanner) {
        while (true) {
            System.out.println("Enter priority (1-3):");
            System.out.print("> ");
            String s = scanner.nextLine().trim();
            try {
                int v = Integer.parseInt(s);
                if (v >= 1 && v <= 3) return v;
                System.out.println("Invalid priority: must be 1, 2, or 3.");
            } catch (NumberFormatException e) {
                System.out.println("Invalid input: enter a number (1-3).");
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

                    int priority = readPriority1to3(scanner);

                    Task newTask = new Task(board.nextId(), title, description, priority);

                    board.add(newTask);

                    System.out.println("New task added successfully!");
                    System.out.println("Your new task is: ");
                    System.out.println(newTask);
                    break;

                case "list":
                    System.out.println("Select list: todo, doing, done (optional sorting flags available)");
                    System.out.print("> ");
                    String userListSelection = scanner.nextLine();
                    String[] userListSelectionParts = userListSelection.split("\\s+");
                    Column listColumn = parseColumn(userListSelectionParts[0]);

                    if (listColumn == null) {
                        System.out.println("List not found");
                        break;
                    }

                    List<Task> listSelection = board.get(listColumn);
                    Comparator<Task> cmp;
                    boolean isDescending = false;

                    if (userListSelectionParts.length < 2) {
                        TaskViews.printList(listSelection, listColumn);
                        break;
                    }

                    if (userListSelectionParts.length < 3) {
                        System.out.println("Something went wrong with your query");
                        System.out.println("Usage: list <col> [--by id|title|priority] [--desc]");
                        TaskViews.printList(listSelection, listColumn);
                        break;
                    }

                    if (userListSelectionParts.length > 4) {
                        System.out.println("You typed too much stuff");
                        System.out.println("Usage: list <col> [--by id|title|priority] [--desc]");
                        break;
                    }

                    if (userListSelectionParts[1].equalsIgnoreCase("--by")) {
                        String sortType = userListSelectionParts[2].trim().toLowerCase();
                        SortKey key = SortKey.fromFlag(sortType);

                        if (key == null) {
                            System.out.println("Invalid sort type. Please enter a valid sort type");
                            break;
                        }

                        // loop through the rest of the user input for a descending flag
                        for (int i = 3; i < userListSelectionParts.length; i++) {
                            String token = userListSelectionParts[i];
                            if ("--d".equalsIgnoreCase(token) || "--desc".equalsIgnoreCase(token) || "--descending".equalsIgnoreCase(token)) {
                                isDescending = true;
                                break;
                            }
                        }

                        cmp = key.comparator(isDescending);

                        TaskViews.printListSorted(listSelection, listColumn, cmp);
                    } else {
                        System.out.println("Invalid input after list selection");
                        System.out.println("Usage: list <col> [--by id|title|priority] [--desc]");
                        TaskViews.printList(listSelection, listColumn);
                    }
                    break;
                case "find":
                    String findPrompt = "Enter the id of the task you want to find: ";
                    System.out.print("> ");
                    long findTaskId = readLongOrFail(scanner, findPrompt);

                    Task foundTask = board.find(findTaskId);
                    Column foundTaskCol = board.getCol(findTaskId);

                    if (foundTask == null) {
                        System.out.println("Task not found");
                        break;
                    } else {
                        System.out.println(TaskViews.formatTaskLine(foundTask, foundTaskCol));
                    }
                    break;
                case "move":
                    boolean exitMove = false;
                    while(true) {
                        String movePrompt = "Enter the id of task you want to move: ";
                        long moveTaskId = readLongOrFail(scanner, movePrompt);

                        Task moveTask = board.find(moveTaskId);
                        Column fromList = board.getCol(moveTaskId);

                        if (moveTask == null) {
                            System.out.println("Task not found");
                            continue;
                        }

                        // confirmation before moving task
                        System.out.println("You have selected this task to move: ");
                        System.out.println(TaskViews.formatTaskLine(moveTask, fromList));

                        System.out.println("Please confirm that this is the correct task");
                        String moveTaskConfirm = askYesNoQuit(scanner, "Confirm task? (y or n | q to quit)");
                        if (moveTaskConfirm.equals("q")) {
                            System.out.println("Move task aborted");
                            break;
                        }

                        if (moveTaskConfirm.equals("n")) {
                            System.out.println("Please select a different task");
                            continue;
                        }

                        System.out.println("Enter which list you want to move the task to: todo, doing, done");
                        String moveInput = scanner.nextLine();

                        Column toList = parseColumn(moveInput);

                        if (toList == null) {
                            System.out.println("Enter a valid list");
                            continue;
                        }

                        if (fromList == toList) {
                            System.out.print("The task is already in that list. Please select a different list.");
                            continue;
                        }

                        System.out.println("Moving task from " + board.getCol(moveTaskId) + ", to " + toList.name());

                        String userConfirmation = askYesNoQuit(scanner, "Confirm move? (y or n | q to quit)");
                        switch (userConfirmation) {
                            case "q":
                                System.out.println("Move aborted. Returning to main menu");
                                exitMove = true;
                                break;
                            case "y":
                                boolean taskMoved = board.move(moveTaskId, toList);

                                if (taskMoved) {
                                    System.out.println("Task moved successfully");
                                } else {
                                    System.out.println("Task move failed");
                                }
                                exitMove = true;
                                break;
                            case "n":
                                System.out.println("Select a different task to move");
                        }

                        if(exitMove) {
                            break;
                        }
                    }

                    break;
                case "edit": {
                    long editId = readLongOrFail(scanner, "Enter id of task to edit: ");
                    Task editTask = board.find(editId);
                    Column editTaskCol = board.getCol(editId);
                    if (editTask == null) {
                        System.out.println("Task not found");
                        break;
                    }

                    // Preload current values; overwrite only if user chooses to edit
                    String newTitle = editTask.getTitle();
                    String newDescription = editTask.getDescription();
                    int newPriority = editTask.getPriority();

                    // ---- Title ----
                    System.out.println("Current title: " + editTask.getTitle());
                    String tAns = askYesNoQuit(scanner, "Edit title? (y/n | q to quit)");
                    if (tAns.equals("q")) { System.out.println("Edit ABORTED"); break; }
                    if (tAns.equals("y")) {
                        System.out.println("Enter new title:");
                        System.out.print("> ");
                        newTitle = scanner.nextLine();
                    }

                    // ---- Description ----
                    System.out.println("Current description: " + editTask.getDescription());
                    String dAns = askYesNoQuit(scanner, "Edit description? (y/n | q to quit)");
                    if (dAns.equals("q")) { System.out.println("Edit ABORTED"); break; }
                    if (dAns.equals("y")) {
                        System.out.println("Enter new description:");
                        System.out.print("> ");
                        newDescription = scanner.nextLine();
                    }

                    // ---- Priority ----
                    System.out.println("Current priority: " + editTask.getPriority());
                    String pAns = askYesNoQuit(scanner, "Edit priority? (y/n | q to quit)");
                    if (pAns.equals("q")) { System.out.println("Edit ABORTED"); break; }
                    if (pAns.equals("y")) {
                        newPriority = readPriority1to3(scanner);
                    }

                    // No-op detection
                    boolean changed =
                            !newTitle.equals(editTask.getTitle()) ||
                            !newDescription.equals(editTask.getDescription()) ||
                            newPriority != editTask.getPriority();

                    if (!changed) {
                        System.out.println("Task not edited");
                        break;
                    }

                    board.edit(editTask, newTitle, newDescription, newPriority);

                    System.out.println("Task edited successfully");
                    System.out.println("Edited task: " + TaskViews.formatTaskLine(editTask, editTaskCol));
                    boolean editSave = board.save();
                    if (editSave) {
                        System.out.printf("Board saved successfully to %s%n", board.getSaveFilePath());
                    } else {
                        System.out.printf("Failed to save board to %s%n", board.getSaveFilePath());
                    }
                    break;
                }

                case "del":
                    boolean exitDel = false;
                    do {
                        String delPrompt = "Enter id of task to delete: ";
                        long delId = readLongOrFail(scanner, delPrompt);
                        Task delTask = board.find(delId);
                        Column delTaskCol = board.getCol(delId);

                        List<Task> delTaskList = board.get(delTaskCol);
                        int taskCount = delTaskList.size();

                        if (delTask == null) {
                            System.out.println("Task not found. Select a valid task");
                            continue;
                        }

                        // User confirms that they selected the correct task
                        System.out.println("You have selected this task to delete: ");
                        System.out.println(TaskViews.formatTaskLine(delTask, delTaskCol));

                        String delConfirmResponse = askYesNoQuit(scanner, "Are you sure you want to delete this task? (y or n | q to quit)");

                        switch (delConfirmResponse) {
                            case "q":
                                exitDel = true;
                                System.out.println("Delete aborted. Returning to main menu");
                                break;
                            case "y":
                                boolean result = board.remove(delId);

                                if (result) {
                                    exitDel = true;
                                    System.out.println("Task removed successfully");
                                    System.out.println(delTaskCol.name() + " currently has " + taskCount + " tasks.");
                                } else {
                                    System.out.println("Task not found");
                                }
                                break;
                            case "n":
                                System.out.println("Select a different task to delete");
                        }

                    } while (!exitDel);

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

                    scanner.close();
                    return;
                default:
                    System.out.println("Pick a valid option");
            }
        }

    }
}