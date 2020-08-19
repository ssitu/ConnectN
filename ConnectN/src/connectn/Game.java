package connectn;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Scanner;

public class Game {

    private final Scanner sc = new Scanner(System.in);
    private final float PLAYER1MARK = 1;
    private final float PLAYER2MARK = -1;
    private final int WIDTH;
    private final int HEIGHT;
    private final int TURNSTODRAW;
    private final int STATUSPLAYER1WIN = 1;
    private final int STATUSPLAYER2WIN = 2;
    private final int STATUSDRAW = 3;
    private final int ONGOING = 0;
    private final int[] recentloc = new int[2];
    private final boolean PRINTING = true;
    private final String STRINGWINPLAYER1 = "Player 1 wins!";
    private final String STRINGWINPLAYER2 = "Player 2 wins!";
    private final String STRINGDRAW = "It's a draw!";
    private float[][] grid;
    private Runnable player1input = () -> defaultInput();
    private Runnable player2input = () -> defaultInput();
    private boolean player1turn;
    private int action = -1;
    private int gamestatus;
    private int turns;
    private final int CONNECTNUM;
    private ArrayList<Runnable> updates = new ArrayList<>();
    private int updatesSize = 0;
    private final String PLAYER1STRING = "x";
    private final String PLAYER2STRING = "o";
    private final String PLAYER1STRINGWIN = "X";//"\033[0;31m" + PLAYER1STRING + "\033[0m"
    private final String PLAYER2STRINGWIN = "O";//"\033[0;31m" + PLAYER2STRING + "\033[0m"
    private final int MAXCONNECTIONS = 2;
    private final Socket[] sockets = new Socket[MAXCONNECTIONS];
    private final BufferedReader[] inputStreams = new BufferedReader[MAXCONNECTIONS];
    private final PrintStream[] outputStreams = new PrintStream[MAXCONNECTIONS];

    public Game(int width, int height, int connectNumber) {
        this.WIDTH = width;
        this.HEIGHT = height;
        this.CONNECTNUM = connectNumber;
        this.TURNSTODRAW = width * height;
    }

    public void update() {
        for (int i = 0; i < updatesSize; i++) {
            try {
                updates.get(i).run();
            } catch (NullPointerException e) {
            }
        }
        if (PRINTING) {
            printgrid();
            if (player1turn) {
                System.out.println("Turn for Player 1 (" + PLAYER1STRING + ").");
            } else {
                System.out.println("Turn for Player 2 (" + PLAYER2STRING + ").");
            }
        }
        while (action == -1) {
            if (player1turn) {
                player1input.run();
            } else {
                player2input.run();
            }
        }
        if (isGameOngoing()) {
            takeaction();
            turns++;
            player1turn = !player1turn;
            action = -1;
            checkgameover(recentloc);
        } else {
            reset();
        }
    }

    public void play() {
        reset();
        while (true) {
            update();
        }
    }

    private void reset() {
        grid = new float[WIDTH][HEIGHT];
        player1turn = true;
        gamestatus = ONGOING;
        recentloc[0] = -1;
        turns = 0;
    }

    private void printgrid() {
        for (int i = 0; i < HEIGHT; i++) {
            System.out.print("|");
            for (int j = 0; j < WIDTH; j++) {
                float current = grid[j][i];
                if (current == PLAYER1MARK) {
                    System.out.print(PLAYER1STRING);
                } else if (current == PLAYER2MARK) {
                    System.out.print(PLAYER2STRING);
                } else {
                    System.out.print(" ");
                }
                int digitsMinusOne = (int) (Math.log10(j + 1));
                for (int k = 0; k < digitsMinusOne; k++) {
                    System.out.print(" ");
                }
                System.out.print("|");
            }
            System.out.println("");
        }
        System.out.print(" ");
        for (int i = 1; i <= WIDTH; i++) {
            System.out.print(i + " ");
        }
        System.out.println("");
    }

    private String gridToString() {
        String string = "";
        for (int i = 0; i < HEIGHT; i++) {
            string += "|";
            for (int j = 0; j < WIDTH; j++) {
                float current = grid[j][i];
                if (current == PLAYER1MARK) {
                    string += PLAYER1STRING;
                } else if (current == PLAYER2MARK) {
                    string += PLAYER2STRING;
                } else {
                    string += " ";
                }
                int digitsMinusOne = (int) (Math.log10(j + 1));
                for (int k = 0; k < digitsMinusOne; k++) {
                    string += " ";
                }
                string += "|";
            }
            string += "\n";
        }
        string += " ";
        for (int i = 1; i <= WIDTH; i++) {
            string += i + " ";
        }
        return string;
    }

    private void takeaction() {
        float[] column = grid[action];
        for (int i = HEIGHT - 1; i >= 0; i--) {
            if (column[i] == 0) {
                if (player1turn) {
                    column[i] = PLAYER1MARK;
                } else {
                    column[i] = PLAYER2MARK;
                }
                recentloc[0] = action;
                recentloc[1] = i;
                break;
            }
        }
    }

    private void checkgameover(int[] recentloc) {
        int x = recentloc[0];
        if (x != -1) {
            int y = recentloc[1];
            //Column
            int count = 1 + countDirection(x, y, -1, 0, CONNECTNUM - 1);
            if (count == CONNECTNUM) {
                gamestatuswin(true);
                return;
            }
            count += countDirection(x, y, 1, 0, CONNECTNUM - count);
            if (count == CONNECTNUM) {
                gamestatuswin(true);
                return;
            }
            //Row
            count = 1 + countDirection(x, y, 0, -1, CONNECTNUM - 1);
            if (count == CONNECTNUM) {
                gamestatuswin(true);
                return;
            }
            count += countDirection(x, y, 0, 1, CONNECTNUM - count);
            if (count == CONNECTNUM) {
                gamestatuswin(true);
                return;
            }
            //Back Slash
            count = 1 + countDirection(x, y, -1, -1, CONNECTNUM - 1);
            if (count == CONNECTNUM) {
                gamestatuswin(true);
                return;
            }
            count += countDirection(x, y, 1, 1, CONNECTNUM - count);
            if (count == CONNECTNUM) {
                gamestatuswin(true);
                return;
            }
            //Forward Slash
            count = 1 + countDirection(x, y, -1, 1, CONNECTNUM - 1);
            if (count == CONNECTNUM) {
                gamestatuswin(true);
                return;
            }
            count += countDirection(x, y, 1, -1, CONNECTNUM - count);
            if (count == CONNECTNUM) {
                gamestatuswin(true);
                return;
            }
            gamestatuswin(false);
        }
    }

    private void gamestatuswin(boolean win) {
        if (!win) {
            if (turns == TURNSTODRAW) {
                gamestatus = STATUSDRAW;
                if (PRINTING) {
                    printgrid();
                    System.out.println(STRINGDRAW);
                }
                for (int i = 0; i < MAXCONNECTIONS; i++) {
                    try {
                        outputStreams[i].println(gridToString());
                        outputStreams[i].println(STRINGDRAW);
                    } catch (Exception e) {
                    }
                }
                reset();
            }
        } else {
            if (!player1turn) {//This check runs on the next player's turn
                gamestatus = STATUSPLAYER1WIN;
                if (PRINTING) {
                    String winningBoard = getWinningBoard();
                    System.out.println(winningBoard);
                    System.out.println(STRINGWINPLAYER1);
                    for (int i = 0; i < MAXCONNECTIONS; i++) {
                        try {
                            outputStreams[i].println(winningBoard);
                            outputStreams[i].println(STRINGWINPLAYER1);
                        } catch (Exception e) {
                        }
                    }
                }
            } else {
                gamestatus = STATUSPLAYER2WIN;
                if (PRINTING) {
                    String winningBoard = getWinningBoard();
                    System.out.println(winningBoard);
                    System.out.println(STRINGWINPLAYER2);
                    for (int i = 0; i < MAXCONNECTIONS; i++) {
                        try {
                            outputStreams[i].println(winningBoard);
                            outputStreams[i].println(STRINGWINPLAYER2);
                        } catch (Exception e) {
                        }
                    }
                }
            }
            reset();
        }
    }

    private int countDirection(int startx, int starty, int componentx, int componenty, int limit) {//limit and count excludes start
        float type = grid[startx][starty];
        int count = 0;
        int movingx = startx;
        int movingy = starty;
        try {
            for (int i = 0; i < limit; i++) {
                movingx += componentx;
                movingy += componenty;
                if (grid[movingx][movingy] == type) {
                    count++;
                } else {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return count;
    }

    private Object[] countPositions(int startx, int starty, int componentx, int componenty, int limit) {//limit and count excludes start
        ArrayList<int[]> positions = new ArrayList<int[]>();
        float type = grid[startx][starty];
        int count = 0;
        int movingx = startx;
        int movingy = starty;
        try {
            for (int i = 0; i < limit; i++) {
                movingx += componentx;
                movingy += componenty;
                if (grid[movingx][movingy] == type) {
                    count++;
                    positions.add(new int[]{movingx, movingy});
                } else {
                    break;
                }
            }
        } catch (ArrayIndexOutOfBoundsException e) {
        }
        return new Object[]{count, positions};
    }

    private ArrayList<int[]> getWinningPositions() {
        int x = recentloc[0];
        int y = recentloc[1];
        //Column
        Object[] pair = countPositions(x, y, -1, 0, CONNECTNUM - 1);
        int count = 1 + (int) pair[0];
        ArrayList<int[]> positions = (ArrayList<int[]>) pair[1];
        if (count == CONNECTNUM) {
            positions.add(recentloc);
            return positions;
        }
        pair = countPositions(x, y, 1, 0, CONNECTNUM - count);
        count += (int) pair[0];
        positions.addAll((ArrayList<int[]>) pair[1]);
        if (count == CONNECTNUM) {
            positions.add(recentloc);
            return positions;
        }
        //Row
        pair = countPositions(x, y, 0, -1, CONNECTNUM - 1);
        count = 1 + (int) pair[0];
        positions = (ArrayList<int[]>) pair[1];
        if (count == CONNECTNUM) {
            positions.add(recentloc);
            return positions;
        }
        pair = countPositions(x, y, 0, 1, CONNECTNUM - count);
        count += (int) pair[0];
        positions.addAll((ArrayList<int[]>) pair[1]);
        if (count == CONNECTNUM) {
            positions.add(recentloc);
            return positions;
        }
        //Back Slash
        pair = countPositions(x, y, -1, -1, CONNECTNUM - 1);
        count = 1 + (int) pair[0];
        positions = (ArrayList<int[]>) pair[1];
        if (count == CONNECTNUM) {
            positions.add(recentloc);
            return positions;
        }
        pair = countPositions(x, y, 1, 1, CONNECTNUM - count);
        count += (int) pair[0];
        positions.addAll((ArrayList<int[]>) pair[1]);
        if (count == CONNECTNUM) {
            positions.add(recentloc);
            return positions;
        }
        //Forward Slash
        pair = countPositions(x, y, -1, 1, CONNECTNUM - 1);
        count = 1 + (int) pair[0];
        positions = (ArrayList<int[]>) pair[1];
        if (count == CONNECTNUM) {
            positions.add(recentloc);
            return positions;
        }
        pair = countPositions(x, y, 1, -1, CONNECTNUM - count);
        count += (int) pair[0];
        positions.addAll((ArrayList<int[]>) pair[1]);
        if (count == CONNECTNUM) {
            positions.add(recentloc);
            return positions;
        }
        return null;
    }

    private String getWinningBoard() {
        String[][] stringArr = new String[WIDTH][HEIGHT];
        for (int j = 0; j < WIDTH; j++) {
            for (int i = 0; i < HEIGHT; i++) {
                float current = grid[j][i];
                if (current == PLAYER1MARK) {
                    stringArr[j][i] = PLAYER1STRING;
                } else if (current == PLAYER2MARK) {
                    stringArr[j][i] = PLAYER2STRING;
                } else {
                    stringArr[j][i] = " ";
                }
            }
        }
        ArrayList<int[]> winningLine = getWinningPositions();
        if (winningLine.size() != CONNECTNUM) {
            throw new IllegalStateException("getWinningPositions() unexpected return");
        }
        for (int i = 0; i < CONNECTNUM; i++) {
            int[] pos = winningLine.get(i);
            String mark = stringArr[pos[0]][pos[1]];
            if (mark == PLAYER1STRING) {
                stringArr[pos[0]][pos[1]] = PLAYER1STRINGWIN;
            } else if (mark == PLAYER2STRING) {
                stringArr[pos[0]][pos[1]] = PLAYER2STRINGWIN;
            }
        }
        String string = "";
        for (int i = 0; i < HEIGHT; i++) {
            string += "|";
            for (int j = 0; j < WIDTH; j++) {
                string += stringArr[j][i];
                int digitsMinusOne = (int) (Math.log10(j + 1));
                for (int k = 0; k < digitsMinusOne; k++) {
                    string += " ";
                }
                string += "|";
            }
            string += "\n";
        }
        string += " ";
        for (int i = 1; i <= WIDTH; i++) {
            string += i + " ";
        }
        return string;
    }

    private void defaultInput() {
        if (isGameOngoing()) {
            System.out.println("Input a column number (1 - " + WIDTH + ").");
            while (true) {
                while (true) {
                    try {
                        action = sc.nextInt();
                        if (action >= 1 && action <= WIDTH) {
                            break;
                        } else {
                            System.out.println("Not a valid column");
                        }
                    } catch (java.util.InputMismatchException ime) {
                        System.out.println("Not an integer");
                        sc.nextLine();
                    }
                }
                action--;
                if (!isColumnFull(action)) {
                    break;
                } else {
                    System.out.println("Column is full");
                }
            }
        }
    }

    public void waitForNewPeer(int port, int index) throws IOException {
        ServerSocket ss = new ServerSocket(port);
        System.out.println("Waiting for connection...");
        Socket s = ss.accept();
        System.out.println("Connected with " + s.getInetAddress());
        int i = index % sockets.length;
        sockets[i] = s;
        BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintStream out = new PrintStream(s.getOutputStream());
        inputStreams[i] = in;
        outputStreams[i] = out;
        out.println("Welcome to Connect " + CONNECTNUM + " on a " + WIDTH + "x" + HEIGHT + " grid!\n"
                + "You are Player " + (i + 1) + ".");
        Runnable update = () -> {
            try {
                out.println(gridToString());
                if (player1turn) {
                    out.println("Turn for Player 1 (" + PLAYER1STRING + ").");
                } else {
                    out.println("Turn for Player 2 (" + PLAYER2STRING + ").");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        };
        int updateIndex = setNewUpdate(update);
        Runnable input = () -> {
            try {
                if (isGameOngoing()) {
                    out.println("Input a column number (1 - " + WIDTH + ").");
                    while (true) {
                        while (true) {
                            try {
                                action = Integer.parseInt(in.readLine());
                                if (action >= 1 && action <= WIDTH) {
                                    break;
                                } else {
                                    out.println("Not a valid column");
                                }
                            } catch (NumberFormatException e) {
                                out.println("Not an integer");
                            }
                        }
                        action--;
                        if (!isColumnFull(action)) {
                            break;
                        } else {
                            out.println("Column is full");
                        }
                    }
                }
            } catch (java.net.SocketException se) {
                System.out.println(se);
                try {
                    s.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (i == 0) {
                    setInputPlayer1(() -> defaultInput());
                } else {
                    setInputPlayer2(() -> defaultInput());
                }
                updates.set(updateIndex, null);
                play();
            } catch (Exception e) {
                e.printStackTrace();

            }
        };
        if (i == 0) {
            setInputPlayer1(input);
        } else {
            setInputPlayer2(input);
        }
    }

    public void setInputPlayer1(Runnable input) {
        player1input = input;
    }

    public void setInputPlayer2(Runnable input) {
        player2input = input;
    }

    public void setInput(int action) {
        this.action = action % WIDTH;
    }

    public int setNewUpdate(Runnable update) {
        updates.add(update);
        updatesSize++;
        return updates.size() - 1;
    }

    public boolean isColumnFull(int column) {
        return grid[column][0] != 0;
    }

    public boolean isPlayer1Win() {
        return gamestatus == STATUSPLAYER1WIN;
    }

    public boolean isPlayer2Win() {
        return gamestatus == STATUSPLAYER2WIN;
    }

    public boolean isDraw() {
        return gamestatus == STATUSDRAW;
    }

    public boolean isGameOngoing() {
        return gamestatus == ONGOING;
    }
}
