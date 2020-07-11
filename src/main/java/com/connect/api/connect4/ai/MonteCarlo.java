package com.connect.api.connect4.ai;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MonteCarlo {

    public static MonteCarloResult monteCarlo(List<List<Integer>> board, int turn, int n) {

        Random random = new Random();

        List<Integer> moves = getMoves(board);

        System.out.println(moves);

        List<Integer> winCounts = new ArrayList<>();
        for (int i = 0; i < moves.size(); i++) {
            winCounts.add(0);
        }

        for (int m = 0; m < moves.size(); m++) {
            int move = moves.get(m);

            for (int i = 0; i < n; i++) {

                Integer lastMove = move;

                List<Integer> boardCopy = flatten(board);
                boardCopy.set(move, turn);
                int turnCopy = 3 - turn;

                List<Integer> movesCopy = copyMoves(moves);
                boolean removed = movesCopy.remove(lastMove);
                if (lastMove < 35) {
                    movesCopy.add(lastMove + 7);
                }

                while (getGameResult(boardCopy, lastMove) == null && movesCopy.size() > 0) {

                    int moveIndex = random.nextInt(movesCopy.size());
                    lastMove = movesCopy.remove(moveIndex);

                    if (lastMove < 35) {
                        movesCopy.add(lastMove + 7);
                    }

                    boardCopy.set(lastMove, turnCopy);
                    turnCopy = 3 - turnCopy;

                }

                Integer gameResult = getGameResult(boardCopy, lastMove);
                if (gameResult != null) {
                    winCounts.set(m, winCounts.get(m) - (gameResult * 2 - 3));
                }

                //System.out.println(getGameResult(boardCopy, lastMove));
                //printBoard(boardCopy);
                //System.out.println();
            }

        }

        int maxIndex = 0;
        int maxValue = -9999999;
        for (int i = 0; i < moves.size(); i++) {
            //System.out.println(String.format("%s: %s", moves.get(i), winCounts.get(i)));
            if (winCounts.get(i) * -(turn * 2 - 3) >= maxValue) {
                maxValue = winCounts.get(i) * -(turn * 2 - 3);
                maxIndex = i;
            }
        }
        //System.out.println(maxIndex);

        return new MonteCarloResult(moves, winCounts, moves.get(maxIndex));
    }

    private static void printBoard(List<Integer> board) {
        for (int row = 5; row >= 0; row--) {
            int pos = 7 * row;
            System.out.println(String.format("%s %s %s %s %s %s %s", board.get(pos), board.get(pos+1), board.get(pos+2), board.get(pos+3), board.get(pos+4), board.get(pos+5), board.get(pos+6)));
        }
    }

    private static List<Integer> copyMoves(List<Integer> moves) {
        return new ArrayList<>(moves);
    }

    private static List<Integer> flatten(List<List<Integer>> board) {
        List<Integer> flatList = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                flatList.add(board.get(row).get(col));
            }
        }
        return flatList;
    }

    private static List<Integer> getMoves(List<List<Integer>> board) {
        List<Integer> moveList = new ArrayList<>();
        for (int col = 0; col < 7; col++) {
            for (int row = 5; row >= 0; row--) {
                if (board.get(row).get(col) != 0) {
                    if (row < 5) {
                        moveList.add((row + 1) * 7 + col);
                    }
                    break;
                } else if (row == 0) {
                    moveList.add(col);
                }
            }
        }
        return moveList;
    }

    private static Integer getGameResult(List<Integer> board, Integer move) {

        if (move == null) {
            return null;
        }

        int col = move % 7;
        int row = (move - col) / 7;

        for (int p = 1; p <= 2; p++) {

            // check below
            if (row >= 3) {
                if (board.get(move) == p && board.get(move - 7) == p && board.get(move - 14) == p && board.get(move - 21) == p) {
                    return p;
                }
            }

            // check horizontal
            for (int startCol = Math.max(0, col - 3); startCol <= Math.min(3, col); startCol++) {
                int startPos = 7 * row + startCol;
                if (board.get(startPos) == p && board.get(startPos + 1) == p && board.get(startPos + 2) == p && board.get(startPos + 3) == p) {
                    return p;
                }
            }

            // check leading diagonal
            for (int startCol = col - Math.min(row, col); startCol <= Math.min(3, col); startCol++) {
                int startRow = row + (startCol - col);
                if (startRow <= 2) {
                    int startPos = 7 * startRow + startCol;
                    if (board.get(startPos) == p && board.get(startPos + 8) == p && board.get(startPos + 16) == p && board.get(startPos + 24) == p) {
                        return p;
                    }
                }
            }

            // check other diagonal
            for (int startCol = col + Math.min(row, 6 - col); startCol >= Math.min(3, col); startCol--) {
                int startRow = row + (col - startCol);
                if (startRow <= 2) {
                    int startPos = 7 * startRow + startCol;
                    if (board.get(startPos) == p && board.get(startPos + 6) == p && board.get(startPos + 12) == p && board.get(startPos + 18) == p) {
                        return p;
                    }
                }
            }
        }

        return null;
    }

    public static void main(String[] args) {
        List<List<Integer>> board = new ArrayList<>();
        for (int i = 0; i < 6; i++) {
            List<Integer> row = new ArrayList<>();
            for (int j = 0; j < 7; j++) {
                row.add(0);
            }
            board.add(row);
        }
        MonteCarlo.monteCarlo(board, 2, 1000);
    }



}
