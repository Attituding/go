package driver;

import java.util.ArrayList;
import java.awt.Point;

public class Go
{

    public static final int EMPTY = 0;
    public static final int BLACK = 1;
    public static final int WHITE = 2;

    private final int[][] board;
    private final int size;
    private final boolean allowSelfCapture;

    private boolean lastPlayerPassed = false;
    private boolean bothPlayersPassed = false;
    private int currentPlayer = BLACK;
    private int move = 1;
    private int stonesCapturedByBlack = 0;
    private int stonesCapturedByWhite = 0;

    public Go(int size, boolean allowSelfCapture)
    {
        this.board = new int[size][size];
        this.size = size;
        this.allowSelfCapture = allowSelfCapture;

        for (int row = 0; row < board.length; row++)
        {
            for (int column = 0; column < board[0].length; column++)
            {
                setValue(row, column, EMPTY);
            }
        }
    }

    public void pass()
    {
        // When both players pass consecutively, end the game
        if (lastPlayerPassed)
        {
            bothPlayersPassed = true;
            return;
        }

        lastPlayerPassed = true;
        move += 1;
        currentPlayer = getOtherPlayer(currentPlayer);
    }

    public void play(int row, int column)
    {
        setValue(row, column, currentPlayer);

        // Capturing opponent takes priority
        capture(row, column, getOtherPlayer(currentPlayer));
        capture(row, column, currentPlayer);

        lastPlayerPassed = false;
        move += 1;
        currentPlayer = getOtherPlayer(currentPlayer);
    }

    // Remove a neighboring chain if it has no liberty
    private void capture(int row, int column, int player)
    {
        ArrayList<ArrayList<Point>> neighborChains = getNeighborChains(row, column, player);

        int captured = 0;

        for (ArrayList<Point> chain : neighborChains)
        {
            if (isChainLiberal(chain) == false)
            {
                for (Point point : chain)
                {
                    setValue(point.y, point.x, EMPTY);
                    captured++;
                }
            }
        }

        if (player == WHITE && currentPlayer == BLACK)
        {
            stonesCapturedByBlack += captured;
        } else if (player == BLACK && currentPlayer == WHITE)
        {
            stonesCapturedByWhite += captured;
        }
    }

    public boolean canPlay(int row, int column)
    {
        if (isInBounds(row, column) == false)
        {
            return false;
        }

        if (getValue(row, column) != EMPTY)
        {
            return false;
        }

        if (allowSelfCapture == false && preconditionSelfCapture(row, column))
        {
            return false;
        }

        return true;
    }

    // A move is (optionally) illegal if only the current player's stone(s) are removed after a play
    private boolean preconditionSelfCapture(int row, int column)
    {
        // Temporarily set value to simulate a play
        setValue(row, column, currentPlayer);

        int otherPlayer = getOtherPlayer(currentPlayer);
        ArrayList<ArrayList<Point>> neighborChains = getNeighborChains(row, column, otherPlayer);

        for (ArrayList<Point> chain : neighborChains)
        {
            if (isChainLiberal(chain) == false)
            {
                setValue(row, column, EMPTY);
                return false;
            }
        }

        ArrayList<Point> currentChain = getChain(row, column);
        boolean chainLiberal = isChainLiberal(currentChain) == false;

        setValue(row, column, EMPTY);
        return chainLiberal;
    }

    /*
    Finds clusters of the same colour
    This approach recursively searches immediate neighbors while avoiding known points
    Pretty unoptimized, but works for now
     */
    private ArrayList<Point> getChain(int row, int column)
    {
        ArrayList<Point> chain = new ArrayList();
        int targetPlayer = getValue(row, column);

        chain.add(new Point(column, row));

        getChain(chain, row, column, targetPlayer);

        return chain;
    }

    private void getChain(ArrayList<Point> chain, int row, int column, int player)
    {
        ArrayList<Point> neighbors = getNeighbors(row, column);

        for (Point neighbor : neighbors)
        {
            if (getValue(neighbor.y, neighbor.x) == player && chain.contains(neighbor) == false)
            {
                chain.add(new Point(neighbor.x, neighbor.y));
                getChain(chain, neighbor.y, neighbor.x, player);
            }
        }
    }

    // Could add overload for specific neighbors for efficiency
    private ArrayList<Point> getNeighbors(int row, int column)
    {
        ArrayList<Point> neighbors = new ArrayList();

        if (isInBounds(row - 1, column)) // North
        {
            neighbors.add(new Point(column, row - 1));
        }

        if (isInBounds(row, column + 1)) // East
        {
            neighbors.add(new Point(column + 1, row));
        }

        if (isInBounds(row + 1, column)) // South
        {
            neighbors.add(new Point(column, row + 1));
        }

        if (isInBounds(row, column - 1)) //West
        {
            neighbors.add(new Point(column - 1, row));
        }

        return neighbors;
    }

    // Adding filtering for duplicate chains would be good
    private ArrayList<ArrayList<Point>> getNeighborChains(int row, int column, int player)
    {
        ArrayList<ArrayList<Point>> neighborChains = new ArrayList();
        ArrayList<Point> stonesOfInterest = getNeighbors(row, column);

        if (player == currentPlayer)
        {
            stonesOfInterest.add(new Point(column, row));
        }

        for (Point stoneOfInterest : stonesOfInterest)
        {
            if (getValue(stoneOfInterest.y, stoneOfInterest.x) == player)
            {
                ArrayList<Point> chain = getChain(stoneOfInterest.y, stoneOfInterest.x);
                neighborChains.add(chain);
            }
        }

        return neighborChains;
    }

    public int getCurrentPlayer()
    {
        return currentPlayer;
    }

    public int getMove()
    {
        return move;
    }

    public int getOtherPlayer(int player)
    {
        if (player == BLACK)
        {
            return WHITE;
        }

        return BLACK;
    }

    public int getSize()
    {
        return size;
    }

    // Area scoring counts a player's territoty plus the number of stones they have on the board
    public int getScoreArea(int player)
    {
        int stones = 0;

        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                int value = getValue(y, x);

                if (value == player)
                {
                    stones++;
                }
            }
        }

        return getTerritory(player) + stones;
    }

    // Territory scoring counts a player's territory minus the stones captured by the opponent
    public int getScoreTerritory(int player)
    {
        int stonesCaptured = player == BLACK
                ? stonesCapturedByWhite
                : stonesCapturedByBlack;

        return getTerritory(player) - stonesCaptured;
    }

    public int getTerritory(int player)
    {
        ArrayList<Point> checkedPoints = new ArrayList();
        int territory = 0;

        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                int value = getValue(y, x);

                if (value != EMPTY)
                {
                    continue;
                }

                if (checkedPoints.contains(new Point(x, y)))
                {
                    continue;
                }

                ArrayList<Point> chain = getChain(y, x);
                checkedPoints.addAll(chain);

                // If a space is surrounded by "player", it belongs to "player"
                // If a space is surrounded by both players, it belongs to no one
                if (isChainPlayerTerritoy(chain, player))
                {
                    territory += chain.size();
                }
            }
        }

        return territory;
    }

    public int getValue(int row, int column)
    {
        return board[row][column];
    }

    private boolean isChainLiberal(ArrayList<Point> chain)
    {
        for (Point point : chain)
        {
            if (isLiberal(point.y, point.x))
            {
                return true;
            }
        }

        return false;
    }

    // Territory is defined as empty spaces that are adjacdent with only the players stones
    private boolean isChainPlayerTerritoy(ArrayList<Point> chain, int player)
    {
        for (Point point : chain)
        {
            ArrayList<Point> neighbors = getNeighbors(point.y, point.x);

            for (Point neighbor : neighbors)
            {
                int value = getValue(neighbor.y, neighbor.x);
                if (value != player && value != EMPTY)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isGameOver()
    {
        if (bothPlayersPassed)
        {
            return true;
        }

        for (int y = 0; y < size; y++)
        {
            for (int x = 0; x < size; x++)
            {
                if (canPlay(y, x) == true)
                {
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isInBounds(int row, int column)
    {
        return isInBounds(row) && isInBounds(column);
    }

    public boolean isInBounds(int axis)
    {
        return axis >= 0 && axis < size;
    }

    // Returns true when a point has one or more empty space on any side
    private boolean isLiberal(int row, int column)
    {
        ArrayList<Point> neighbors = getNeighbors(row, column);

        for (Point neighbor : neighbors)
        {
            if (getValue(neighbor.y, neighbor.x) == EMPTY)
            {
                return true;
            }
        }

        return false;
    }

    private void setValue(int row, int column, int value)
    {
        board[row][column] = value;
    }
}
