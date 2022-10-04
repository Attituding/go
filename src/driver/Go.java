package driver;

import java.util.ArrayList;
import java.awt.Point;

public class Go {
    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;
    
    private final int[][] board;
    private final int size;
    
    private boolean lastPlayerPassed = false;
    private boolean gameEnded = false;
    private int move = 1;
    private int player = BLACK;
    
    
    // options: int size, boolean self capture/suicide
    public Go(int size /*, boolean allowSelfCapture*/) {
        board = new int[size][size];
        this.size = size;
        
        for (int row = 0; row < board.length; row++) {
            for (int column = 0; column < board[0].length; column++) {
                setValue(row, column, EMPTY);
            }
        }
    }
    
    public void pass() {
        // When both players pass consecutively, end the game
        if (lastPlayerPassed) {
            gameEnded = true;
            return;
        }
        
        lastPlayerPassed = true;
        move += 1;
        player = getOtherPlayer(player);
    }
    
    public void play(int row, int column) {
        setValue(row, column, player);
        
        // Capturing other player takes priority
        capture(row, column, getOtherPlayer(player));
        capture(row, column, player);
        
        lastPlayerPassed = false;
        move += 1;
        player = getOtherPlayer(player);
    }
    
    public boolean canPlay(int row, int column) {
        // TODO: Check that a value was never occcupied by the same player (ko rule)
        // TODO: Check for infinite looping behavior

        if (isInBounds(row, column) == false) {
            return false;
        }
        
        if (getValue(row, column) != EMPTY) {
            return false;
        }
        
        // Optional self-capture rule
        // A move is illegal if only one's stones are removed after a play
        // Basic implementation:
        // Get neighboring chains (getNeighborChains) including self
        // Check that opposite chain isn't losing liberty
        // If yes, return legal
        // If no, continue
        // Check chain that the placed stone resides in, no need to get neighbors for this
        
        return true;
    }
    
    // Remove a neighboring chain if it has no liberty
    private void capture(int row, int column, int player) {
        ArrayList<ArrayList<Point>> neighborChains = getNeighborChains(row, column, player);
        
        for (ArrayList<Point> chain : neighborChains) {
            if (isChainLiberal(chain) == false) {
                for (Point point : chain) {
                    setValue(point.y, point.x, EMPTY);
                }
            }
        }
    }
    
    /*
    Find a group of the same player in the north, east, south, and west directions
    Requirements:
        - Don't crash/infinitely loop when a circle is encountered
        - Minimize checking for duplicates for speed
    This approach recursively searches immediate neighbors and avoids searching previously searched values
    This method could be more efficient with a different approach, but this suffices and is fast enough
        - To improve speed, don't include checked indexes as neighbors
        - Track the angle that a givern getChain call has differed from the origin
    */
    private ArrayList<Point> getChain(int row, int column) {
        ArrayList<Point> chain = new ArrayList();
        
        chain.add(new Point(column, row));
        
        int targetPlayer = getValue(row, column);
        
        getChain(chain, row, column, targetPlayer);
        
        return chain;
    }
    
    private void getChain(ArrayList<Point> chain, int row, int column, int player) {
        ArrayList<Point> neighbors = getNeighbors(row, column);
        
        for (Point neighbor : neighbors) {
            if (getValue(neighbor.y, neighbor.x) == player && chain.contains(neighbor) == false) {
                chain.add(new Point(neighbor.x, neighbor.y));
                getChain(chain, neighbor.y, neighbor.x, player);
            }
        }
    }
    
    private ArrayList<Point> getNeighbors(int row, int column) {
        ArrayList<Point> neighbors = new ArrayList();
        
        if (isInBounds(row - 1, column)) { // North
            neighbors.add(new Point(column, row - 1));
        }
        
        if (isInBounds(row, column + 1)) { // East
            neighbors.add(new Point(column + 1, row));
        }
        
        if (isInBounds(row + 1, column)) { // South
            neighbors.add(new Point(column, row + 1));
        }
        
        if (isInBounds(row, column - 1)) { //West
            neighbors.add(new Point(column - 1, row));
        }
        
        return neighbors;
    }
    
    private ArrayList<ArrayList<Point>> getNeighborChains(int row, int column, int player) {
        // TODO: check that chains are not duplicated
        
        ArrayList<ArrayList<Point>> neighborChains = new ArrayList();
        ArrayList<Point> stonesOfInterest = getNeighbors(row, column);
        
        stonesOfInterest.add(new Point(column, row));
        
        for (Point stoneOfInterest : stonesOfInterest) {
            if (getValue(stoneOfInterest.y, stoneOfInterest.x) == player) {
                ArrayList<Point> chain = getChain(stoneOfInterest.y, stoneOfInterest.x);
                neighborChains.add(chain);
            }
        }
        
        return neighborChains;
    }
    
    public int getPlayer() {
        return player;
    }
    
    public boolean getGameEnded() {
        return gameEnded;
    }
    
    public int getMove() {
        return move;
    }
    
    public int getOtherPlayer(int player) {
        if (player == BLACK) {
            return WHITE;
        }
        
        return BLACK;
    }
    
    /*
    public int getScoreTerritoy(int player) {
        // Wikipedia: In territory scoring a player's score is determined by the number of empty locations that player has surrounded minus the number of stones their opponent has captured
        // loop over every empty space
        // if a space is surrounded by "player", add point to that player
        // if a space is surrounded by multiple players, no points are given
        // subtract captured stone count int from that score
    }
    */
    
    /*
    public int getScoreArea(int player) {
        // Wikipedia: In area scoring (including Chinese rules), a player's score is determined by the number of stones that player has on the board plus the empty area surrounded by that player's stones. 
        // loop over every empty space (also track the amount fo stone that "player" has)
        // if a space is surrounded by "player", add point to that player
        // if a space is surrounded by multiple players, no points are given
    }
    */
    
    public int getSize() {
        return size;
    }
    
    public int getValue(int row, int column) {
        return board[row][column];
    }
    
    private boolean isChainLiberal(ArrayList<Point> chain) {
        for (Point point : chain) {
            if (isLiberal(point.y, point.x)) {
                return true;
            }
        }
        
        return false;
    }
    
    public boolean isInBounds(int row, int column) {
        return isInBounds(row) && isInBounds(column);
    }
    
    public boolean isInBounds(int axis) {
        return axis >= 0 && axis < size;
    }
    
    private boolean isLiberal(int row, int column) {
        ArrayList<Point> neighbors = getNeighbors(row, column);
        
        for (Point neighbor : neighbors) {
            if (getValue(neighbor.y, neighbor.x) == EMPTY) {
                return true;
            }
        }
        
        return false;
    }
    
    private void setValue(int row, int column, int value) {
        board[row][column] = value;
    }
}