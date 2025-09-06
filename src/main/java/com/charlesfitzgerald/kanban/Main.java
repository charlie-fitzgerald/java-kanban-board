package com.charlesfitzgerald.kanban;

import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        Board board = new Board();
        Scanner scanner = new Scanner(System.in);

        boolean loadOnStartUp = board.load();

        if (loadOnStartUp) {
            System.out.printf("Board '" + board.getBoardName() + "' successfully loaded from %s%n", board.getSaveFilePath());
        } else {
            System.out.println("No board found. Starting a new board");
            System.out.println();
        }

        while(true) {
            CommandHelpers.printMenu();
            String input = scanner.nextLine().trim().toLowerCase();

            switch (input) {
                // no input returns user to main menu
                case "" -> {
                }
                case "add" -> CommandHelpers.handleAdd(board, scanner);
                case "list" -> CommandHelpers.handleList(board, scanner);
                case "find" -> CommandHelpers.handleFind(board, scanner);
                case "move" -> CommandHelpers.handleMove(board, scanner);
                case "edit" -> CommandHelpers.handleEdit(board, scanner);
                case "del" -> CommandHelpers.handleDel(board, scanner);
                case "save-as" -> CommandHelpers.handleSaveAs(board, scanner);
                case "save" -> CommandHelpers.handleSave(board);
                case "load" -> CommandHelpers.handleLoad(board);
                case "help" -> CommandHelpers.handleHelp(scanner);
                case "version" -> CommandHelpers.handleVersion();
                case "quit" -> {
                    CommandHelpers.handleQuit(board);
                    scanner.close();
                    return;
                }
                default -> System.out.println("Command not found. Choose a valid command.");
            }
        }

    }
}