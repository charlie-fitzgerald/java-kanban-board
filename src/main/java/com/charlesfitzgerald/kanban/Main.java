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

    static long tryParseLong(String inputString) {
        try {
            return Long.parseLong(inputString);
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid number");
        }
        return -1;
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

    static void printHelpCommands() {
        System.out.println("Commands:");
        System.out.println("  add     - Add a new task to a list");
        System.out.println("  list    - List tasks in a specific list.");
        System.out.println("  find    - Find task by id");
        System.out.println("  move    - Move a task between lists");
        System.out.println("  edit    - Edit a task by entering its id");
        System.out.println("  del     - Delete a task by entering its id");
        System.out.println("  save    - Save the current data");
        System.out.println("  load    - Load data from a save file");
        System.out.println("  version - View the current version of the software");
        System.out.println("  quit    - Exit the program");
    }

    public static void main(String[] args) {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);

        boolean loadOnStartUp = board.load();

        if (loadOnStartUp) {
            System.out.printf("Board '" + board.getBoardName() + "' successfully loaded from %s%n", board.getSaveFilePath());
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
                    System.out.println(TaskViews.formatTaskLine(newTask, board.getCol(newTask.getId())));
                    break;

                case "list":
                    System.out.println("Select list: todo, doing, done, or type 'all' to print all lists");
                    System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
                    System.out.print("> ");
                    String userListSelection = scanner.nextLine();
                    String[] userListSelectionParts = userListSelection.split("\\s+");
                    Comparator<Task> cmp;
                    boolean isDescending = false;

                    // Handling printing all lists at once
                    if (userListSelectionParts[0].isEmpty()) {
                        System.out.println("Please provide input for this command");
                        break;
                    }

                    if (userListSelectionParts[0].trim().equalsIgnoreCase("all")) {

                        if (userListSelectionParts.length < 2) {
                            TaskViews.printAll(board);
                            break;
                        }

                        if (userListSelectionParts.length < 3) {
                            System.out.println("Something went wrong with your query");
                            System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
                            TaskViews.printAll(board);
                            break;
                        }

                        if (userListSelectionParts.length > 4) {
                            System.out.println("You typed too much stuff");
                            System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
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
                                } else {
                                    System.out.println("Ignoring unknown flag " + token);
                                }
                            }

                            cmp = key.comparator(isDescending);

                            TaskViews.printAllSorted(board, cmp);
                        } else {
                            System.out.println("Invalid input after list selection");
                            System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
                            TaskViews.printAll(board);
                        }

                        break;
                    }


                    Column listColumn = parseColumn(userListSelectionParts[0]);

                    if (listColumn == null) {
                        System.out.println("List not found");
                        break;
                    }

                    List<Task> listSelection = board.get(listColumn);

                    if (userListSelectionParts.length < 2) {
                        TaskViews.printList(listSelection, listColumn);
                        break;
                    }

                    if (userListSelectionParts.length < 3) {
                        System.out.println("Something went wrong with your query");
                        System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
                        break;
                    }

                    if (userListSelectionParts.length > 4) {
                        System.out.println("You typed too much stuff");
                        System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
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
                        System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
                        TaskViews.printList(listSelection, listColumn);
                    }
                    break;
                case "find":
                    System.out.println("Enter: <id> [--detailed|--v] (q to quit)");
                    System.out.print("> ");
                    String findInput = scanner.nextLine().trim().toLowerCase();

                    if (findInput.equals("q")) {
                        System.out.println("Returning to main menu");
                        break;
                    }

                    String[] findInputParts = findInput.split("\\s+");
                    boolean isDetailed = false;
                    for (int i = 1; i < findInputParts.length; i++) {
                        String f = findInputParts[i];
                        if (f.equals("--detailed") || f.equals("--v")) {
                            isDetailed = true;
                        } else {
                            System.out.println("Ignoring unknown flag " + f);
                        }
                    }

                    long findTaskId = tryParseLong(findInputParts[0]);

                    if (findTaskId == -1) {
                        System.out.println("Invalid id");
                        break;
                    }

                    Task foundTask = board.find(findTaskId);
                    Column foundTaskCol = board.getCol(findTaskId);

                    if (foundTask == null) {
                        System.out.println("Task not found");
                        break;
                    } else {
                       String findOutput = isDetailed ? TaskViews.printDetailedTask(foundTask, foundTaskCol) :
                                                        TaskViews.formatTaskLine(foundTask, foundTaskCol);
                       System.out.println(findOutput);
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

                        if (delTask == null) {
                            System.out.println("Task not found. Select a valid task");
                            continue;
                        }

                        // User confirms that they selected the correct task
                        System.out.println("You have selected this task to delete: ");
                        System.out.println(TaskViews.formatTaskLine(delTask, delTaskCol));

                        // Normalizes user input to y/n/q. Continues prompting until valid input detected
                        String delConfirmResponse = askYesNoQuit(scanner, "Are you sure you want to delete this task? (y or n | q to quit)");

                        switch (delConfirmResponse) {
                            case "q":
                                exitDel = true;
                                System.out.println("Delete aborted. Returning to main menu");
                                break;
                            case "y":
                                boolean result = board.remove(delId);

                                if (result) {
                                    int taskCount = delTaskList.size();
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
                        System.out.printf("Board '" + board.getBoardName() + "' successfully saved to %s%n", board.getSaveFilePath());
                    } else {
                        System.out.printf("Failed to save board to %s%n", board.getSaveFilePath());
                    }
                    break;
                case "load":
                    boolean load = board.load();

                    if (load) {
                        System.out.printf("Board '" + board.getBoardName() + "' data successfully loaded from %s%n", board.getSaveFilePath());
                    } else {
                        System.out.printf("Loading data from %s%n failed", board.getSaveFilePath());
                    }

                    break;
                case "help":
                    printHelpCommands();

                    boolean inHelpFlow = true;
                    while (inHelpFlow) {
                        System.out.println("""
                        Type a command from the list to get more details on that specific command.
                        Type 'commands' to print the list of commands again.
                        Type 'q' to quit to main menu ('quit' will return help for the 'quit' command)
                        """);
                        System.out.print("> ");
                        String helpInput = scanner.nextLine().toLowerCase().trim();

                        switch (helpInput) {
                            case "q":
                                System.out.println("Returning to main menu");
                                inHelpFlow = false;
                                break;
                            case "commands":
                                printHelpCommands();
                                break;
                            case "add":
                                System.out.println("Usage: add");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  Add a new task to the board. You will be prompted to enter a title,");
                                System.out.println("  description, and priority (1–3). By default, the task is added to TODO.");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  add");
                                System.out.println();
                                break;
                            case "list":
                                System.out.println("Usage: list <todo|doing|done|all> [--by id|title|priority] [--desc]");
                                System.out.println();
                                System.out.println("Options:");
                                System.out.println("  --by <key>   Sort by id/title/priority (aliases supported)");
                                System.out.println("  --desc, --d  Sort in descending order");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  list todo");
                                System.out.println("  list doing --by title");
                                System.out.println("  list done --by p --d");
                                System.out.println("  list all");
                                System.out.println("  list all --by priority --desc");
                                System.out.println();
                                break;
                            case "find":
                                System.out.println("Usage: find <id> [--detailed|--v]");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  After entering 'find' at the main menu, you will be prompted to enter");
                                System.out.println("  the task ID, with an optional flag to show detailed information.");
                                System.out.println();
                                System.out.println("Options:");
                                System.out.println("  --detailed, --v   Show full task details (title, description, priority, column)");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  3");
                                System.out.println("  12 --detailed");
                                System.out.println("  7 --v");
                                System.out.println();
                                break;
                            case "move":
                                System.out.println("Usage: move");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  Move an existing task from one list to another. You will be prompted");
                                System.out.println("  for the task ID, then asked to confirm the task and select a target list.");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  move");
                                System.out.println();
                                break;
                            case "edit":
                                System.out.println("Usage: edit");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  Edit an existing task by its ID. You will be prompted to confirm each");
                                System.out.println("  field (title, description, priority), and only the ones you change");
                                System.out.println("  will be updated.");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  edit");
                                System.out.println();
                                break;
                            case "del":
                                System.out.println("Usage: del");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  Delete an existing task by its ID. You will be asked to confirm before");
                                System.out.println("  the task is removed.");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  del");
                                System.out.println();
                                break;
                            case "save":
                                System.out.println("Usage: save");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  Save the current board data to disk. By default, data is saved to 'board.json'.");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  save");
                                System.out.println();
                                break;
                            case "load":
                                System.out.println("Usage: load");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  Load board data from the save file ('board.json'). This replaces the");
                                System.out.println("  current board state.");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  load");
                                System.out.println();
                                break;
                            case "version":
                                System.out.println("Usage: version");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  Show the current version of the Kanban CLI.");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  version");
                                System.out.println();
                                break;
                            case "quit":
                                System.out.println("Usage: quit");
                                System.out.println();
                                System.out.println("Description:");
                                System.out.println("  Save (auto-save) and exit the program.");
                                System.out.println();
                                System.out.println("Examples:");
                                System.out.println("  quit");
                                System.out.println();
                                break;
                            default:
                                printHelpCommands();
                                System.out.println("That command not found. Choose a command from the command list, or q to quit to the main menu.");
                                break;
                        }

                    }
                    break;
                case "version":
                    System.out.println("Kanban CLI - Version " + VERSION);
                    break;
                case "quit":
                    boolean quitSave = board.save();

                    if (quitSave) {
                        System.out.printf("Board '"+board.getBoardName()+"' successfully saved to %s%n", board.getSaveFilePath());
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