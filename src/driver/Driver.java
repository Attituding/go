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
    
    private static void print(Go game) {
        int size = game.getSize();
        int paddingLength = Integer.toString(size).length();
        
        String header = padString("0", paddingLength);
        
        for (int i = 0; i < size; i++) {
            header += padString(i + 1 + " ", paddingLength);
        }
        
        System.out.println("Player " + game.getPlayer() + " - Move " + game.getMove() );
        System.out.println(header);
        
        for (int i = 0; i < size; i++) {
            String rowString = padString(i + 1 + "", paddingLength);
            
            for (int y = 0; y < size; y++) {
                int rawValue = game.getStone(i, y);
                
                switch (rawValue) {
                    case Go.EMPTY -> rowString += padString("·", paddingLength);
                    case Go.BLACK -> rowString += padString("B", paddingLength);
                    case Go.WHITE -> rowString += padString("W", paddingLength);
                }
                
                /*
                if (rawValue == Go.EMPTY) {
                    rowString += padString("·", paddingLength);
                } else if (rawValue == Go.BLACK) {
                    rowString += padString("B", paddingLength);
                } else if (rawValue == Go.WHITE) {
                    rowString += padString("W", paddingLength);
                }
                */
            }
            
            System.out.println(rowString);
        }
    }
    
    public static void main(String[] args) {
        Go game = new Go(20);
        
        Scanner input = new Scanner(System.in);
        
        while (game.getGameEnded() == false) {
            print(game);
            
            System.out.println("Do you want to skip your turn?");
            
            String line = input.next();
            
            if (line.contains("y") || line.contains("true")) {
                System.out.println("Skipping turn!");
                game.pass();
                continue;
            }
            
            int row;
            int column;
            boolean firstTry = true;
            
            do {
                if (firstTry == false) {
                    System.out.println("Illegal move. Try again.");
                }
                
                firstTry = false;
                
                System.out.println("Input a row number:");
                row = input.nextInt() - 1;
                
                while (game.isInBounds(row) == false) {
                    System.out.println("Invalid row. Try again:");
                    row = input.nextInt() - 1;
                }
                
                System.out.println("Input a column number:");
                column = input.nextInt() - 1;
                
                while (game.isInBounds(column) == false) {
                    System.out.println("Invalid column. Try again:");
                    column = input.nextInt() - 1;
                }
            } while (game.canPlay(row, column) == false);
            
            game.play(row, column);
        }

        input.close();
        
        System.out.println("Both players skipped! Game over!");
    }
}
