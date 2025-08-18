package com.charlesfitzgerald.kanban;

public class SaveDataSmokeTest {
    public static void main(String[] args) {
        Board board = new Board();

        // Seed a couple tasks (Board.add puts them in TODO)
        board.add(new Task(1, "Test1", "desc", 1));
        board.add(new Task(2, "Test2", "desc", 2));

        System.out.println("Before snapshot (TODO size): " + board.get(Column.TODO).size()); // expect 2

        // Take snapshot
        SaveData snapshot = board.toSaveData();

        // Clear via removals
        board.remove(1);
        board.remove(2);
        System.out.println("After clearing (TODO size): " + board.get(Column.TODO).size()); // expect 0

        // Reload from snapshot
        board.loadFrom(snapshot);
        System.out.println("After reload (TODO size): " + board.get(Column.TODO).size()); // expect 2
    }
}
