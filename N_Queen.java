//  N-Queen Problem using Multi-Threading

// -1 = Free location
// 1 = Queen placed
// 0 = Blocked Position
// 2 = Tried Position

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.Stack;
import java.util.concurrent.RecursiveAction;
import java.util.stream.Collectors;

class Counter {
    static int counter = 0;
}

class BoardStatus {
    int board[][];
    int queenNumber;

    public BoardStatus(int[][] board, int queenNumber) {

        this.queenNumber = queenNumber;
        int size = board.length;

        this.board = new int[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                this.board[i][j] = board[i][j];
            }
        }
    }

    public int[][] getBoard() {
        return board;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int[] row : board) {
            for (int cell : row) {
                sb.append(cell).append(" ");
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}

class Solutions {
    List<Integer> result;
}

class Task extends RecursiveAction {

    int[][] initialBoard;
    int ThreadNumber;
    List<List<Integer>> SolutionList;
    Counter count;
    FileWriter fw;
    BufferedWriter bw;

    // boolean debug = true;
    boolean debug = false;

    public Task(int[][] board, int ThreadNumber) {
        this.initialBoard = board;
        this.ThreadNumber = ThreadNumber;
    }

    @Override
    protected void compute() {
        BoardStatus status = new BoardStatus(initialBoard, 1);
        Stack<BoardStatus> statusStack = new Stack<>();
        statusStack.push(status);// initial board with 1st queen placed
        try {
            getSolutions(statusStack);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void makeDeepCopy(int[][] newBoard, int[][] topBoard) {
        for (int i = 0; i < topBoard.length; i++) {
            for (int j = 0; j < topBoard.length; j++) {
                newBoard[i][j] = topBoard[i][j];
            }
        }
    }

    public int checkForBreakingCondition(int[][] topBoard) {
        int returnValue = 1;

        for (int j = 0; j < topBoard.length; j++) {
            if (topBoard[1][j] == -1 || topBoard[1][j] == 1) {
                returnValue = 0;
            }
        }
        return returnValue;
    }

    public void getSolutions(Stack<BoardStatus> statusStack) throws Exception {
        this.SolutionList = new ArrayList<>();
        String fileName = this.ThreadNumber + ".txt";
        this.fw = new FileWriter(fileName, true);
        this.bw = new BufferedWriter(fw);

        int[][] newBoard = null;
        int i = 0;
        while (true) {
            BoardStatus topBoardObject = null;
            try {
                topBoardObject = statusStack.peek();
            } catch (Exception e) {
                System.out.println("\nStack is empty..");
            }
            int queenNumber = topBoardObject.queenNumber; // will tell which queen (1,2,3 or 4) is placed at this board
            int[][] topBoard = topBoardObject.board; // top board at stack
            int boardLength = topBoard.length;

            if (checkForBreakingCondition(topBoard) == 1) {
                break;
            }
            newBoard = new int[boardLength][boardLength]; // making new board of same length

            makeDeepCopy(newBoard, topBoard);

            // if (this.debug) {
            // System.out.println("\nFrom Thread " + ThreadNumber);
            // displayBoard(newBoard);
            // }

            int row = queenNumber;
            int columnWhereQueenPlaced = -1;

            if (row < boardLength) {
                // putting Queen in next row
                for (int col = 0; col < boardLength; col++) {
                    if (newBoard[row][col] == -1) {
                        columnWhereQueenPlaced = col;
                        break;
                    }
                }

                if (columnWhereQueenPlaced != -1) {
                    // Queen placed
                    // if (debug) {
                    // System.out.println("\nFrom Thread " + ThreadNumber + ": Queen will be placed
                    // at : (" + row
                    // + ", " + columnWhereQueenPlaced + ")");
                    // }

                    setBlockedPositionsForNewboard(newBoard, row, columnWhereQueenPlaced);

                    // if (debug) {
                    // System.out.println("\nNew Board From Thread : " + ThreadNumber);
                    // displayBoard(newBoard);
                    // }

                    BoardStatus newTopBoard = new BoardStatus(newBoard, queenNumber + 1); // object
                    statusStack.push(newTopBoard);
                } else {
                    // Queen didn't find place to hold (backtrack)
                    // if (debug) {
                    // System.out
                    // .println("\nFrom Thread " + ThreadNumber + ": Queen can't be placed : Need
                    // Backtrack");
                    // }
                    statusStack.pop(); // removing top staus of board

                    // setting tried location
                    for (int j = 0; j < newBoard.length; j++) {
                        if (newBoard[queenNumber - 1][j] == 1) {
                            newBoard[queenNumber - 1][j] = 2;
                            // System.out.println("\nRemoving Queen from : (" + (queenNumber - 1) + ", " + j
                            // + ")");
                            break;
                        }
                    }
                    syncBlockedPositions(newBoard, queenNumber - 1);

                    // System.out.println("\nFrom Thread " + ThreadNumber + ": Synced Board : ");

                    // displayBoard(newBoard);
                    int latestRow = whereIsQueen(newBoard);
                    topBoardObject = new BoardStatus(newBoard, latestRow + 1);
                    statusStack.push(topBoardObject);

                }

                if (isThereQueenAtLastRow(newBoard)) {
                    // Solutin Found
                    Counter.counter++;
                    // System.out.print("\nSolution : " + Counter.counter);
                    List<Integer> resultList = getPositionsOfQueens(newBoard);
                    // // printList(resultList);

                    String Solution = convertIntListToString(resultList);
                    this.bw.write(Solution);
                    this.bw.newLine();
                    this.bw.flush();

                    // this.bw.close();
                    // this.fw.close();

                    // System.out.println("\nFrom Thread " + ThreadNumber + ": " + Solution);
                    // backtrack
                    statusStack.pop(); // removing top staus of board
                    // setting tried location
                    for (int j = 0; j < newBoard.length; j++) {
                        if (newBoard[queenNumber][j] == 1) {
                            newBoard[queenNumber][j] = 2;
                            // System.out.println("\nRemoving Queen from : (" + (queenNumber) + ", " + j
                            // +")");
                            break;
                        }
                    }
                    syncBlockedPositions(newBoard, queenNumber);

                    // System.out.println("\nFrom Thread " + ThreadNumber + ": Synced Board : ");

                    // displayBoard(newBoard);
                    int latestRow = whereIsQueen(newBoard);
                    topBoardObject = new BoardStatus(newBoard, latestRow + 1);
                    statusStack.push(topBoardObject);
                }
            } else {
            }
            i++;
        }
    }

    public static String convertIntListToString(List<Integer> numbers) {
        String result = numbers.stream()
                .map(Object::toString) // Convert integers to strings
                .collect(Collectors.joining(",", "[", "]")); // Join with commas and add brackets
        return result;
    }

    public boolean isThereQueenAtLastRow(int[][] board) {
        boolean returnValue = false;

        for (int j = 0; j < board.length; j++) {
            if (board[board.length - 1][j] == 1) {
                returnValue = true;
            }
        }
        return returnValue;
    }

    public void printList(List<Integer> resultList) {
        System.out.print("[");
        for (int i = 0; i < resultList.size(); i++) {
            if (i == resultList.size() - 1)
                System.out.print(resultList.get(i));
            else
                System.out.print(resultList.get(i) + ", ");
        }
        System.out.print("]");
    }

    public int whereIsQueen(int[][] newBoard) {
        int returnValue = 1;
        int size = newBoard.length;
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (newBoard[i][j] == 1) {
                    returnValue = i;
                }
            }
        }

        return returnValue;
    }

    public void displaySolutionList(List<List<Integer>> SolutionList) {
        System.out.println("From Thread " + ThreadNumber + ": Solution List:");
        for (List<Integer> list : SolutionList) {
            System.out.println(list);
        }
    }

    public List<Integer> getPositionsOfQueens(int[][] newTopBoard) {
        int size = newTopBoard.length;

        List<Integer> resultList = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                if (newTopBoard[i][j] == 1) {
                    resultList.add(j);
                }
            }
        }
        return resultList;
    }

    public void syncBlockedPositions(int[][] newBoard, int currentRow) {

        for (int i = 0; i < newBoard.length; i++) {
            for (int j = 0; j < newBoard.length; j++) {
                if (newBoard[i][j] == 1) {
                    continue;
                } else if (newBoard[i][j] == 2 && i <= currentRow) {
                    continue;
                } else {
                    newBoard[i][j] = -1;
                }
            }
        }

        for (int i = 0; i < newBoard.length; i++) {
            for (int j = 0; j < newBoard.length; j++) {
                if (newBoard[i][j] == 1) {
                    setBlockedPositionsForNewboard(newBoard, i, j);
                } else {
                }
            }
        }
    }

    public void setBlockedPositionsForNewboard(int[][] board, int row, int col) {
        board[row][col] = 1;
        int size = board.length;

        // blocking vertical positions
        for (int i = 0; i < size; i++) {
            if (i == row || board[i][col] == 2) {
                continue;
            }
            board[i][col] = 0;
        }

        // blocking horizontal positions
        for (int j = 0; j < size; j++) {
            if (j == col || board[row][j] == 2) {
                continue;
            } else {
                board[row][j] = 0;
            }
        }

        // blocking right diagonal positions
        int i = row + 1;
        int j = col + 1;
        while (i < size && j < size) {
            board[i][j] = 0;
            i++;
            j++;
        }

        // blocking left diagonal positions
        i = row + 1;
        j = col - 1;
        while (i < size && j >= 0) {
            board[i][j] = 0;
            i++;
            j--;
        }
    }

    public void displayBoard(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == -1) {
                    System.out.print("-  ");
                } else if (board[i][j] == 1) {
                    System.out.print("Q  ");
                } else {
                    System.out.print(board[i][j] + "  ");
                }
            }
            System.out.println();
        }
    }
}

public class N_Queen {

    public static void displayBoard(int[][] board) {

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                if (board[i][j] == -1) {
                    System.out.print("-  ");

                } else if (board[i][j] == 1) {
                    System.out.print("Q  ");

                } else {
                    System.out.print(board[i][j] + "  ");
                }
            }
            System.out.println();
        }
    }

    public static void initializeBoard(int[][] board) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                board[i][j] = -1;
            }
        }
    }

    public static void setFirstQueenOnBoard(int[][] board, int column) {
        board[0][column] = 1; // putting queen in 1st row
        int size = board.length;

        // blocking vertical positions
        for (int i = 1; i < size; i++) {
            board[i][column] = 0;
        }

        // blocking horizontal positions
        for (int j = 0; j < size; j++) {
            if (j == column) {
                continue;
            } else {
                board[0][j] = 0;
            }
        }

        // blocking right diagonal positions
        int i = 1;
        int j = column + 1;
        while (i < size && j < size) {
            board[i][j] = 0;
            i++;
            j++;
        }

        // blocking left diagonal positions
        i = 1;
        j = column - 1;
        while (i < size && j >= 0) {
            board[i][j] = 0;
            i++;
            j--;
        }
    }

    public static void distributeWork(int NumberOfThreads) {
        Task task[] = new Task[NumberOfThreads];
        for (int j = 0; j < NumberOfThreads; j++) {
            int board[][] = new int[NumberOfThreads][NumberOfThreads];

            initializeBoard(board); // initializing board with -1 (all free positions)

            setFirstQueenOnBoard(board, j); // putting 1st queen in 1st row at jth column

            task[j] = new Task(board, (j + 1));
            task[j].fork();
            try {
                // Thread.sleep(50);
            } catch (Exception e) {
            }
        }

        for (int j = 0; j < NumberOfThreads; j++) {
            task[j].join();
        }
    }

    public static void main(String[] args) {

        File file = new File("E:\\CSE\\java\\My projects\\N-Queen");
        String[] files = file.list();

        int i = 0;
        while (i < files.length) {

            if (files[i].charAt(files[i].length() - 1) == 't' &&
                    files[i].charAt(files[i].length() - 2) == 'x' &&
                    files[i].charAt(files[i].length() - 3) == 't') {

                new File(files[i]).delete();
            }
            i++;
        }

        Scanner sc = new Scanner(System.in);
        System.out.print("\n\nEnter Order : ");
        int n = sc.nextInt();

        if (n == 1) {
            System.out.println("\nNumber of Solution : 1");
        } else if (n == 2 || n == 3) {
            System.out.println("\nSolution not possible...");
        } else {
            // Part for n > 3
            distributeWork(n);
        }

        System.out.println("\nNo. of Solutions : " + Counter.counter);
    }
}