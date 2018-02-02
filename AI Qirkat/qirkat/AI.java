package qirkat;

import static qirkat.PieceColor.*;
import java.util.ArrayList;

/** A Player that computes its own moves.
 *  @author Brandon Griffin
 */
class AI extends Player {

    /** Maximum minimax search depth before going to static evaluation. */
    private static final int MAX_DEPTH = 8;
    /** A position magnitude indicating a win (for white if positive, black
     *  if negative). */
    private static final int WINNING_VALUE = Integer.MAX_VALUE - 1;
    /** A magnitude greater than a normal value. */
    private static final int INFTY = Integer.MAX_VALUE;

    /** A new AI for GAME that will play MYCOLOR. */
    AI(Game game, PieceColor myColor) {
        super(game, myColor);
    }

    @Override
    Move myMove() {
        Main.startTiming();
        Move move = findMove();
        Main.endTiming();
        if (move != null && board().legalMove(move)) {
            System.out.println(myColor() + " moves " + move + ".");
        }
        return move;
    }

    /** Return a move for me from the current position, assuming there
     *  is a move. */
    private Move findMove() {
        Board b = new Board(board());
        if (myColor() == WHITE) {
            findMove(b, MAX_DEPTH, true, 1, -INFTY, INFTY);
        } else {
            findMove(b, MAX_DEPTH, true, -1, -INFTY, INFTY);
        }
        return _lastFoundMove;
    }

    /** The move found by the last call to one of the ...FindMove methods
     *  below. */
    private Move _lastFoundMove;

    /** Find a move from position BOARD and return its value, recording
     *  the move found in _lastFoundMove iff SAVEMOVE. The move
     *  should have maximal value or have value > BETA if SENSE==1,
     *  and minimal value or value < ALPHA if SENSE==-1. Searches up to
     *  DEPTH levels.  Searching at level 0 simply returns a static estimate
     *  of the board value and does not set _lastMoveFound. */
    private int findMove(Board board, int depth, boolean saveMove, int sense,
                         int alpha, int beta) {
        Move best = null;
        int val1 = -INFTY;
        int val2 = INFTY;
        ArrayList<Move> listOfMoves = board.getMoves();
        int maxer = players(board.whoseMove(), board);
        int minner = players(board.whoseMove().opposite(), board);
        if (depth == 0 || listOfMoves.size() == 0) {
            return staticScore(board);
        }

        if (board.gameOver() && (maxer > minner)) {
            return WINNING_VALUE;
        } else if (board.gameOver() && (maxer < minner)) {
            return -WINNING_VALUE;
        } else {
            for (Move move : listOfMoves) {
                board.makeMove(move);
                int next = findMove(board,
                        depth - 1, false, -sense, alpha, beta);
                if (sense == 1) {
                    if (next >= val1) {
                        best = move;
                        val1 = next;
                        alpha = Math.max(alpha, next);
                    }
                } else {
                    if (next <= val2) {
                        best = move;
                        val2 = next;
                        beta = Math.min(beta, next);
                    }
                }
                board.undo();
                if (beta <= alpha) {
                    break;
                }
            }
            if (saveMove) {
                _lastFoundMove = best;
            }
            if (sense == -1) {
                return val2;
            }
            return val1;
        }
    }

    /** Return a heuristic value for BOARD.*/
    private int staticScore(Board board) {
        String b = board.toStringList(board);
        int white = 0;
        int black = 0;
        for (int i = 0; i < b.length(); i++) {
            if (b.charAt(i) == 'w') {
                white++;
            } else if (b.charAt(i) == 'b') {
                black++;
            }
        }
        if (board.whoseMove().equals(WHITE)) {
            return white - black;
        } else {
            return black - white;
        }
    }

    /** Return # of players this player has.
     * @param p player to be counted
     * @param board current board state*/
    private int players(PieceColor p, Board board) {
        String b = board.toStringList(board);
        int players = 0;
        for (int i = 0; i < b.length(); i++) {
            if (b.charAt(i) == (p.shortName().charAt(0))) {
                players++;
            }
        }
        return players;
    }
}
