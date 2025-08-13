package org.example;

import java.util.ArrayList;
import java.util.Scanner;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);
        int taskId = 0;

        while(true) {
            System.out.println("Available commands: add | list | move | del | help | quit");
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                case "add":
                    System.out.println("Add a task to a list");
                    System.out.println("Type the list you want to add it to, followed by: <id>, <title>, <description>, <priority>");

                    System.out.println("Select list: todo, doing, done");
                    ArrayList<Task> addList = board.getList(scanner.nextLine().toLowerCase());

                    if (addList == null) {
                        break;
                    }

                    System.out.println("Enter title:");
                    String title = scanner.nextLine();

                    System.out.println("Enter description:");
                    String description = scanner.nextLine();

                    System.out.println("Enter priority (0,1,2):");
                    int priority = Integer.parseInt(scanner.nextLine());

                    Task newTask = new Task(taskId++, title, description, priority);

                    board.addTask(addList, newTask);

                    System.out.println("New task added successfully!");
                    break;

                case "list":
                    System.out.println("Select list: todo, doing, done");
                    ArrayList<Task> listSelection = board.getList(scanner.nextLine().toLowerCase());

                    if (listSelection == null) {
                        break;
                    }

                    board.getTasks(listSelection);
                    break;
                case "move":
                    System.out.println("Move from which list: todo, doing, done");
                    ArrayList<Task> fromList = board.getList(scanner.nextLine().toLowerCase());

                    if (fromList == null) {
                        break;
                    }

                    System.out.println("Move to which list: todo, doing, done");
                    ArrayList<Task> toList = board.getList(scanner.nextLine().toLowerCase());

                    if (toList == null) {
                        break;
                    }

                    System.out.println("Id of task to move");
                    int moveTaskId = Integer.parseInt(scanner.nextLine());

                    board.moveTaskById(fromList, toList, moveTaskId);
                    break;
                case "del":
                    System.out.println("Pick a list to delete a task from: todo, doing, done");
                    ArrayList<Task> delList = board.getList(scanner.nextLine().toLowerCase());

                    if (delList == null) {
                        break;
                    }

                    System.out.println("Enter id of task to delete: ");
                    int delId = Integer.parseInt(scanner.nextLine());

                    board.removeTaskById(delList, delId);
                    break;
                case "help":
                    System.out.println("Commands:");
                    System.out.println("  add  - Add a new task to a list");
                    System.out.println("  list - List tasks in a specific list");
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