/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */

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
    public Go(int size) {
        board = new int[size][size];
        this.size = size;
        
        for (int row[] : board) {
            for (int value : row) {
                value = EMPTY;
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
        setStone(row, column, player);
        
        // Capturing other player takes priority
        capture(row, column, player);
        capture(row, column, getOtherPlayer(player));
        
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
        
        if (getStone(row, column) != EMPTY) {
            return false;
        }
        
        /*
        // Optional suicide rule?
        // Additional: a move is illegal if 3 or more of your stones will be removed
        // Setting and removing point to test liberty as  a chain, if one exists
        int currentValue = getStone(row, column);
        setStone(row, column, player);
        ArrayList<Point> chain = getChain(row, column);
        boolean pointisLiberal = isChainLiberal(chain);
        setStone(row, column, EMPTY);
        */
        
        return true;
    }
    
    // Remove a neighboring chain if it has no liberty
    private void capture(int row, int column, int player) {
        ArrayList<Point> neighbors = getNeighbors(row, column);
        
        for (Point neighbor : neighbors) {
            if (getStone(neighbor.y, neighbor.x) == player) {
                ArrayList<Point> chain = getChain(neighbor.y, neighbor.x);
            
                if (isChainLiberal(chain) == false) {
                    for (Point point : chain) {
                        setStone(point.y, point.x, EMPTY);
                    }
                }
            }
        }
    }
    
    /*
    Find a group of the same player in the north, east, south, and west directions
    Requirements:
        - Don't crash/infinitely loop when a circle is encountered
        - Minimize checking for duplicates for speed
    This approach recursively searches immediate neightbors and avoids searching previously searched values
    This method could be more efficient with a different approach, but this sufficies and is fast enough
        - To improve speed, don't include checked indexes as neighbors
    */
    private ArrayList<Point> getChain(int row, int column) {
        ArrayList<Point> chain = new ArrayList();
        
        chain.add(new Point(column, row));
        
        int targetPlayer = getStone(row, column);
        
        getChain(chain, row, column, targetPlayer);
        
        return chain;
    }
    
    private void getChain(ArrayList<Point> chain, int row, int column, int player) {
        ArrayList<Point> neighbors = getNeighbors(row, column);
        
        for (Point neighbor : neighbors) {
            if (getStone(neighbor.y, neighbor.x) == player && chain.contains(neighbor) == false) {
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
    public int getScore(int player) {
        // count territory completely surrounded by a player
        // loop over all empty slots while retaining a list of checked indexes?
    }
    */
    
    public int getSize() {
        return size;
    }
    
    public int getStone(int row, int column) {
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
            if (getStone(neighbor.y, neighbor.x) == EMPTY) {
                return true;
            }
        }
        
        return false;
    }
    
    private void setStone(int row, int column, int value) {
        board[row][column] = value;
    }
}