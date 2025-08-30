package com.charlesfitzgerald.kanban;

import org.jetbrains.annotations.NotNull;

import java.util.Comparator;
import java.util.List;
import java.util.Scanner;

public class CommandHelpers {
    private static final String VERSION = "0.8.0";

    private static void printHelpCommands() {
        System.out.println("Commands:");
        System.out.println("  add          - Add a new task to a list");
        System.out.println("  list         - List tasks in a specific list.");
        System.out.println("  find         - Find task by id");
        System.out.println("  move         - Move a task between lists");
        System.out.println("  edit         - Edit a task by entering its id");
        System.out.println("  del          - Delete a task by entering its id");
        System.out.println("  rename-board - Rename the current board");
        System.out.println("  save         - Save the current data");
        System.out.println("  load         - Load data from a save file");
        System.out.println("  version      - View the current version of the software");
        System.out.println("  quit         - Exit the program");
    }

    public static void printMenu() {
        System.out.println("Available commands: add | list | find | move | edit | del | rename-board | save | load | version | help | quit");
        System.out.print("> ");
    }

    // move to Column as a fromString function later
    @org.jetbrains.annotations.Nullable
    private static Column parseColumn(String input) {
        input = input.trim().toLowerCase();

        return switch (input) {
            case "todo" -> Column.TODO;
            case "doing" -> Column.DOING;
            case "done" -> Column.DONE;
            default -> null;
        };

    }

    private static long readLongOrFail(@NotNull Scanner scanner, String prompt) {
        while (true) {
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

    private static long tryParseLong(String inputString) {
        try {
            return Long.parseLong(inputString);
        } catch (NumberFormatException e) {
            System.out.println("Enter a valid number");
        }
        return -1;
    }

    // Reads a priority in [1..3]; keeps prompting until valid.
    private static int readPriority1to3(Scanner scanner) {
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

    // Normalizes to "y", "n", or "q"; keeps prompting until valid.
    private static String askYesNoQuit(Scanner scanner, String prompt) {
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

    public static void handleAdd(@NotNull Board board, @NotNull Scanner scanner) {
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

        System.out.println("Add task to which list: TODO | DOING | DONE");
        System.out.print("> ");
        String newTaskListSelection = scanner.nextLine().trim().toLowerCase();

        Column addToCol = parseColumn(newTaskListSelection);

        if (addToCol == null) {
            System.out.println("List not found. Defaulting to TODO");
            addToCol = Column.TODO;
            board.add(newTask, addToCol);
        } else {
            board.add(newTask, addToCol);
        }

        System.out.println("New task with ID " + newTask.getId() + " added to " + addToCol.name() + " successfully!");
        System.out.println("Your new task is: ");
        System.out.println(TaskViews.formatTaskLine(newTask, addToCol));
        boolean addSave = board.save();
        if (addSave) {
            System.out.printf("Board saved successfully to %s%n", board.getSaveFilePath());
        } else {
            System.out.printf("Failed to save board to %s%n", board.getSaveFilePath());
        }
    }

    public static void handleList(@NotNull Board board, @NotNull Scanner scanner) {
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
            return;
        }

        if (userListSelectionParts[0].trim().equalsIgnoreCase("all")) {

            if (userListSelectionParts.length < 2) {
                TaskViews.printAll(board);
                return;
            }

            if (userListSelectionParts.length < 3) {
                System.out.println("Something went wrong with your query");
                System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
                TaskViews.printAll(board);
                return;
            }

            if (userListSelectionParts.length > 4) {
                System.out.println("You typed too much stuff");
                System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
                return;
            }

            if (userListSelectionParts[1].equalsIgnoreCase("--by")) {
                String sortType = userListSelectionParts[2].trim().toLowerCase();
                SortKey key = SortKey.fromFlag(sortType);

                if (key == null) {
                    System.out.println("Invalid sort type. Please enter a valid sort type");
                    return;
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

            return;
        }


        Column listColumn = parseColumn(userListSelectionParts[0]);

        if (listColumn == null) {
            System.out.println("List not found");
            return;
        }

        List<Task> listSelection = board.get(listColumn);

        if (userListSelectionParts.length < 2) {
            TaskViews.printList(listSelection, listColumn);
            return;
        }

        if (userListSelectionParts.length < 3) {
            System.out.println("Something went wrong with your query");
            System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
            return;
        }

        if (userListSelectionParts.length > 4) {
            System.out.println("You typed too much stuff");
            System.out.println("Usage: list [<col> | <all>] [--by id|title|priority] [--desc]");
            return;
        }

        if (userListSelectionParts[1].equalsIgnoreCase("--by")) {
            String sortType = userListSelectionParts[2].trim().toLowerCase();
            SortKey key = SortKey.fromFlag(sortType);

            if (key == null) {
                System.out.println("Invalid sort type. Please enter a valid sort type");
                return;
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
    }

    public static void handleFind(@NotNull Board board, @NotNull Scanner scanner) {
        System.out.println("Enter: <id> [--detailed|--v] (q to quit)");
        System.out.print("> ");
        String findInput = scanner.nextLine().trim().toLowerCase();

        if (findInput.equals("q")) {
            System.out.println("Returning to main menu");
            return;
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
            return;
        }

        Task foundTask = board.find(findTaskId);
        Column foundTaskCol = board.getCol(findTaskId);

        if (foundTask == null) {
            System.out.println("Task not found");
        } else {
            String findOutput = isDetailed ? TaskViews.printDetailedTask(foundTask, foundTaskCol) :
                    TaskViews.formatTaskLine(foundTask, foundTaskCol);
            System.out.println(findOutput);
        }
    }

    public static void handleMove(@NotNull Board board, @NotNull Scanner scanner) {
        boolean exitMove = false;
        while (true) {
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
                return;
            }

            if (moveTaskConfirm.equals("n")) {
                System.out.println("Please select a different task");
                continue;
            }

            System.out.println("Enter which list you want to move the task to: TODO, DOING, DONE");
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
                        boolean moveSave = board.save();
                        if (moveSave) {
                            System.out.printf("Board saved successfully to %s%n", board.getSaveFilePath());
                        } else {
                            System.out.printf("Failed to save board to %s%n", board.getSaveFilePath());
                        }
                    } else {
                        System.out.println("Task move failed");
                    }
                    exitMove = true;
                    break;
                case "n":
                    System.out.println("Select a different task to move");
            }

            if (exitMove) {
                return;
            }
        }
    }

    public static void handleEdit(@NotNull Board board, @NotNull Scanner scanner) {
        long editId = readLongOrFail(scanner, "Enter id of task to edit: ");
        Task editTask = board.find(editId);
        Column editTaskCol = board.getCol(editId);
        if (editTask == null) {
            System.out.println("Task not found");
            return;
        }

        // Preload current values; overwrite only if user chooses to edit
        String newTitle = editTask.getTitle();
        String newDescription = editTask.getDescription();
        int newPriority = editTask.getPriority();

        // ---- Title ----
        System.out.println("Current title: " + editTask.getTitle());
        String tAns = askYesNoQuit(scanner, "Edit title? (y/n | q to quit)");
        if (tAns.equals("q")) {
            System.out.println("Edit ABORTED");
            return;
        }
        if (tAns.equals("y")) {
            System.out.println("Enter new title:");
            System.out.print("> ");
            newTitle = scanner.nextLine();
        }

        // ---- Description ----
        System.out.println("Current description: " + editTask.getDescription());
        String dAns = askYesNoQuit(scanner, "Edit description? (y/n | q to quit)");
        if (dAns.equals("q")) {
            System.out.println("Edit ABORTED");
            return;
        }
        if (dAns.equals("y")) {
            System.out.println("Enter new description:");
            System.out.print("> ");
            newDescription = scanner.nextLine();
        }

        // ---- Priority ----
        System.out.println("Current priority: " + editTask.getPriority());
        String pAns = askYesNoQuit(scanner, "Edit priority? (y/n | q to quit)");
        if (pAns.equals("q")) {
            System.out.println("Edit ABORTED");
            return;
        }
        if (pAns.equals("y")) {
            newPriority = readPriority1to3(scanner);
        }

        // No-op detection
        boolean changed =
                !newTitle.equals(editTask.getTitle()) ||
                        !newDescription.equals(editTask.getDescription()) ||
                        newPriority != editTask.getPriority();

        if (!changed) {
            System.out.println("No changes detected. Task not edited");
            return;
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
    }

    public static void handleDel(@NotNull Board board, @NotNull Scanner scanner) {
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
    }

    public static void handleRenameBoard(@NotNull Board board, @NotNull Scanner scanner) {
        boolean inRenameBoardFlow = true;
        while (inRenameBoardFlow) {
            System.out.println("Current board name: " + board.getBoardName());
            System.out.println("Please enter a new board name ('q' to quit to main menu. 'q' is an invalid board name): ");
            System.out.print("> ");
            String newBoardNameInput = scanner.nextLine().trim();

            if (newBoardNameInput.equalsIgnoreCase("q")) {
                System.out.println("Returning to main menu");
                inRenameBoardFlow = false;
            }

            if (newBoardNameInput.isEmpty()) {
                System.out.println("No new name detected. Please enter a new name.");
                continue;
            }

            if (newBoardNameInput.length() >= 20) {
                System.out.println("New name is too long. Enter a new name that is less than 20 characters");
                continue;
            }

            if (newBoardNameInput.equals(board.getBoardName())) {
                System.out.println("Name unchanged");
                inRenameBoardFlow = false;
            }

            if (inRenameBoardFlow) {
                board.setBoardName(newBoardNameInput);
                System.out.println("Board renamed to: " + board.getBoardName());
                board.save();
                System.out.printf("Board '" + board.getBoardName() + "' successfully saved to %s%n", board.getSaveFilePath());
                inRenameBoardFlow = false;
            }
        }
    }

    public static void handleSave(@NotNull Board board) {
        boolean save = board.save();
        if (save) {
            System.out.printf("Board '" + board.getBoardName() + "' successfully saved to %s%n", board.getSaveFilePath());
        } else {
            System.out.printf("Failed to save board to %s%n", board.getSaveFilePath());
        }
    }

    public static void handleLoad(@NotNull Board board) {
        boolean load = board.load();

        if (load) {
            System.out.printf("Board '" + board.getBoardName() + "' data successfully loaded from %s%n", board.getSaveFilePath());
        } else {
            System.out.printf("Loading data from %s%n failed", board.getSaveFilePath());
        }

    }

    public static void handleHelp(@NotNull Scanner scanner) {
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
                    System.out.println("Prompts you to enter:");
                    System.out.println("  Title       - Short description of the task");
                    System.out.println("  Description - Longer details of the task");
                    System.out.println("  Priority    - Number between 1 (low) and 3 (high)");
                    System.out.println("  List        - Target column (todo | doing | done).");
                    System.out.println("                If input is invalid, defaults to TODO.");
                    System.out.println();
                    System.out.println("Example flow:");
                    System.out.println("  > add");
                    System.out.println("  Enter title: Write report");
                    System.out.println("  Enter description: Draft the quarterly report");
                    System.out.println("  Enter priority (1-3): 2");
                    System.out.println("  Add task to which list: doing");
                    System.out.println("  -> Task created in DOING with ID #7");
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
                case "rename-board":
                    System.out.println("Usage: rename-board");
                    System.out.println();
                    System.out.println("Description:");
                    System.out.println("  Interactively rename the current board. The new name will be saved");
                    System.out.println("  and used on the next load.");
                    System.out.println();
                    System.out.println("Rules:");
                    System.out.println("  - Type 'q' to cancel and return to the main menu");
                    System.out.println("  - Name cannot be empty");
                    System.out.println("  - Name must be less than 20 characters");
                    System.out.println("  - Re-entering the current name will be treated as 'unchanged'");
                    System.out.println();
                    System.out.println("Examples:");
                    System.out.println("  rename-board");
                    System.out.println("    Current board name: Default Board");
                    System.out.println("    > Computer Blue");
                    System.out.println("    Board renamed to: Computer Blue");
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
    }

    public static void handleVersion() {
        System.out.println("Kanban CLI - Version " + VERSION);
    }

    public static void handleQuit(@NotNull Board board) {
        boolean quitSave = board.save();

        if (quitSave) {
            System.out.printf("Board '"+board.getBoardName()+"' successfully saved to %s%n", board.getSaveFilePath());
        } else {
            System.out.printf("Failed to save board to %s%n", board.getSaveFilePath());
        }
        System.out.println("Thanks for using this program!");
    }

}

