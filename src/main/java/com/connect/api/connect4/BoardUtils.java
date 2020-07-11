package com.connect.api.connect4;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.Pair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class BoardUtils {

    public static final String START_POS = "000000000000000000000000000000000000000000";

    public static List<List<Integer>> fromBoardString(String board) {
        List<List<Integer>> boardArray = new ArrayList<>();
        for (int row = 0; row < 6; row++) {
            List<Integer> rowArray = new ArrayList<>();
            for (int col = 0; col < 7; col++) {
                char element = board.charAt(row * 7 + col);
                rowArray.add(Integer.parseInt(String.valueOf(element)));
            }
            boardArray.add(rowArray);
        }
        return boardArray;
    }

    public static String toBoardString(List<List<Integer>> board) {
        StringBuilder boardString = new StringBuilder("");
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                boardString.append(board.get(row).get(col));
            }
        }
        return boardString.toString();
    }

    public static boolean wasLegalUpdate(List<List<Integer>> current, List<List<Integer>> update, int whoseTurn) {
        int changeCount = 0;
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                int currentValue = current.get(row).get(col);
                int updatedValue = update.get(row).get(col);
                if (currentValue != updatedValue) {
                    changeCount++;
                    log.info("(Row, Col) = ({}, {}) - updatedValue: {} - whoseTurn: {}", row, col, updatedValue, whoseTurn);
                    if (updatedValue != whoseTurn || !isValidMove(current, row, col)) {
                        return false;
                    }
                }
            }
        }
        log.info("Change Count: {}", changeCount);
        return (changeCount == 1);
    }

    private static boolean isValidMove(List<List<Integer>> board, int row, int col) {
        for (int i = 0; i < row; i++) {
            if (board.get(i).get(col) == 0) {
                return false;
            }
        }
        return true;
    }

    public static Pair<Long, List<Integer>> checkGameOver(List<List<Integer>> board) {
        for (int p = 1; p <= 3; p++) {

            // horizontal check
            for (int row = 0; row < 6; row++) {
                for (int col = 0; col < 4; col++) {
                    if (board.get(row).get(col) == p && board.get(row).get(col + 1) == p &&
                            board.get(row).get(col + 2) == p && board.get(row).get(col + 3) == p) {
                        return Pair.of((long) p, Arrays.asList(row * 7 + col,
                                                                row * 7 + col + 1,
                                                                row * 7 + col + 2,
                                                                row * 7 + col + 3));
                    }
                }
            }

            // vertical check
            for (int col = 0; col < 7; col++) {
                for (int row = 0; row < 3; row++) {
                    if (board.get(row).get(col) == p && board.get(row + 1).get(col) == p &&
                            board.get(row + 2).get(col) == p && board.get(row + 3).get(col) == p) {
                        return Pair.of((long) p, Arrays.asList(row * 7 + col,
                                (row + 1) * 7 + col,
                                (row + 2) * 7 + col,
                                (row + 3) * 7 + col));
                    }
                }
            }

            // diagright
            for (int col = 0; col < 4; col++) {
                for (int row = 0; row < 3; row++) {
                    if (board.get(row).get(col) == p && board.get(row + 1).get(col + 1) == p &&
                            board.get(row + 2).get(col + 2) == p && board.get(row + 3).get(col + 3) == p) {
                        return Pair.of((long) p, Arrays.asList(row * 7 + col,
                                (row + 1) * 7 + (col + 1),
                                (row + 2) * 7 + (col + 2),
                                (row + 3) * 7 + (col + 3)));
                    }
                }
            }

            //diagleft
            for (int col = 3; col < 7; col++) {
                for (int row = 0; row < 3; row++) {
                    if (board.get(row).get(col) == p && board.get(row + 1).get(col - 1) == p &&
                            board.get(row + 2).get(col - 2) == p && board.get(row + 3).get(col - 3) == p) {
                        return Pair.of((long) p, Arrays.asList(row * 7 + col,
                                (row + 1) * 7 + (col - 1),
                                (row + 2) * 7 + (col - 2),
                                (row + 3) * 7 + (col - 3)));
                    }
                }
            }
        }

        // check draw
        for (int row = 0; row < 6; row++) {
            for (int col = 0; col < 7; col++) {
                if (board.get(row).get(col) == 0) {
                    return null;
                }
            }
        }

        return Pair.of(0L, new ArrayList<>());
    }
}
