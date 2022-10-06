package driver;

import java.util.Scanner;

public class Driver {

    private static String padString(String string, int paddingLength) {
        String paddedString = string;

        for (int i = string.length(); i < paddingLength + 1; i++) {
            paddedString += " ";
        }

        return paddedString;
    }

    private static int parseInt(String integer) {
        try {
            return Integer.parseInt(integer);
        } catch (NumberFormatException e) {
        }

        return -1;
    }

    private static void print(Go game) {
        int size = game.getSize();
        int paddingLength = Integer.toString(size).length();

        String header = padString("0", paddingLength);

        for (int i = 0; i < size; i++) {
            header += padString(i + 1 + " ", paddingLength);
        }

        System.out.println("Player " + game.getPlayer() + " - Move " + game.getMove());
        System.out.println(header);

        for (int i = 0; i < size; i++) {
            String rowString = padString(i + 1 + "", paddingLength);

            for (int y = 0; y < size; y++) {
                int rawValue = game.getValue(i, y);

                if (rawValue == Go.EMPTY) {
                    rowString += padString("·", paddingLength);
                } else if (rawValue == Go.BLACK) {
                    rowString += padString("B", paddingLength);
                } else if (rawValue == Go.WHITE) {
                    rowString += padString("W", paddingLength);
                }
            }

            System.out.println(rowString);
        }
    }

    public static void main(String[] args) {
        System.out.println(
                "   ____         \n"
                + "  / ___|   ___  \n"
                + " | |  _   / _ \\ \n"
                + " | |_| | | (_) |\n"
                + "  \\____|  \\___/ \n"
                + "                "
        );

        Go game = new Go(19, false);

        Scanner input = new Scanner(System.in);

        while (game.getGameEnded() == false) {
            print(game);

            System.out.println("Do you want to skip your turn? (n/y/true/false)");

            String line = input.next();

            if (line.contains("y") || line.equals("true")) {
                System.out.println("Skipping turn!");
                game.pass();
                continue;
            }

            int row = -1;
            int column = -1;
            boolean firstTry = true;

            do {
                if (firstTry == false) {
                    System.out.println("Illegal move. Try again.");
                }

                firstTry = false;

                System.out.println("Input a row number:");
                row = parseInt(input.next()) - 1;

                while (game.isInBounds(row) == false) {
                    System.out.println("Invalid row. Try again:");
                    row = parseInt(input.next()) - 1;
                }

                System.out.println("Input a column number:");
                column = parseInt(input.next()) - 1;

                while (game.isInBounds(column) == false) {
                    System.out.println("Invalid column. Try again:");
                    column = parseInt(input.next()) - 1;
                }
            } while (game.canPlay(row, column) == false);

            game.play(row, column);
        }

        input.close();

        int blackScoreArea = game.getScoreArea(Go.BLACK);
        int whiteScoreArea = game.getScoreArea(Go.WHITE);

        int blackScoreTerritory = game.getScoreTerritoy(Go.BLACK);
        int whiteScoreTerritory = game.getScoreTerritoy(Go.WHITE);

        System.out.println("Both players skipped! Game over!");
        System.out.println("Area Scoring: Black got " + blackScoreArea + " points and White got " + whiteScoreArea + " points!");
        System.out.println("Territory Scoring: Black got " + blackScoreTerritory + " points and White got " + whiteScoreTerritory + " points!");
    }
}
