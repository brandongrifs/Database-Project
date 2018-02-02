package qirkat;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.Observable;
import java.util.Observer;
import static qirkat.PieceColor.*;
import static qirkat.Move.*;

/** A Qirkat board.   The squares are labeled by column (a char value between
 *  'a' and 'e') and row (a char value between '1' and '5'.
 *
 *  For some purposes, it is useful to refer to squares using a single
 *  integer, which we call its "linearized index".  This is simply the
 *  number of the square in row-major order (with row 0 being the bottom row)
 *  counting from 0).
 *
 *  Moves on this board are denoted by Moves.
 *  @author Brandon Griffin
 */
class Board extends Observable {
    /** Holds the current board in linear indices.*/
    private PieceColor[] _linearBoard;

    /** Keeps track of which spaces can move horiontally. */
    private char[] horAllowed;

    /** Holds all past moves of this game. */
    private MoveList pastMoves;

    /** Holds all past boards of this game. */
    private BoardList pastBoards;

    /** A new, cleared board at the start of the game. */
    Board() {
        clear();
        pastBoards.add(new Board(this));
    }

    /** A copy of B. */
    Board(Board b) {
        internalCopy(b);
    }

    /** Return a constant view of me (allows any access method, but no
     *  method that modifies it). */
    Board constantView() {
        return this.new ConstantBoard();
    }

    /** Clear me to my starting state, with pieces in their initial
     *  positions. */
    void clear() {
        _whoseMove = WHITE;
        _gameOver = false;
        pastMoves = new MoveList();
        pastBoards = new BoardList();
        _linearBoard = new PieceColor[]{WHITE, WHITE, WHITE, WHITE, WHITE,
                                        WHITE, WHITE, WHITE, WHITE, WHITE,
                                        BLACK, BLACK, EMPTY, WHITE, WHITE,
                                        BLACK, BLACK, BLACK, BLACK, BLACK,
                                        BLACK, BLACK, BLACK, BLACK, BLACK};
        horAllowed = new char[]{'b', 'b', 'b', 'b', 'b',
                                'b', 'b', 'b', 'b', 'b',
                                'b', 'b', 'b', 'b', 'b',
                                'b', 'b', 'b', 'b', 'b',
                                'b', 'b', 'b', 'b', 'b'};
        setChanged();
        notifyObservers();
    }

    /** Copy B into me. */
    void copy(Board b) {
        internalCopy(b);
    }

    /** Copy B into me. */
    private void internalCopy(Board b) {
        this.pastMoves = (MoveList) b.pastMoves.clone();
        this.pastBoards = (BoardList) b.pastBoards.clone();
        this._linearBoard = b._linearBoard.clone();
        this.horAllowed = b.horAllowed.clone();
        this._whoseMove = b.whoseMove();
    }

    /** Set my contents as defined by STR.  STR consists of 25 characters,
     *  each of which is b, w, or -, optionally interspersed with whitespace.
     *  These give the contents of the Board in row-major order, starting
     *  with the bottom row (row 1) and left column (column a). All squares
     *  are initialized to allow horizontal movement in either direction.
     *  NEXTMOVE indicates whose move it is.
     */
    void setPieces(String str, PieceColor nextMove) {
        if (nextMove == EMPTY || nextMove == null) {
            throw new IllegalArgumentException("bad player color");
        }
        str = str.replaceAll("\\s", "");
        if (!str.matches("[bw-]{25}")) {
            throw new IllegalArgumentException("bad board description");
        }

        for (int k = 0; k < str.length(); k += 1) {
            switch (str.charAt(k)) {
            case '-':
                set(k, EMPTY);
                break;
            case 'b': case 'B':
                set(k, BLACK);
                break;
            case 'w': case 'W':
                set(k, WHITE);
                break;
            default:
                break;
            }
        }

        horAllowed = new char[]{'b', 'b', 'b', 'b', 'b',
                                'b', 'b', 'b', 'b', 'b',
                                'b', 'b', 'b', 'b', 'b',
                                'b', 'b', 'b', 'b', 'b',
                                'b', 'b', 'b', 'b', 'b'};
        _whoseMove = nextMove;
        if (!isMove() || getMoves() == null) {
            _gameOver = true;
        } else {
            _gameOver = false;
        }
        setChanged();
        notifyObservers();
    }

    /** Return true iff the game is over: i.e., if the current player has
     *  no moves. */
    boolean gameOver() {
        return _gameOver;
    }

    /** Return the current contents of square C R, where 'a' <= C <= 'e',
     *  and '1' <= R <= '5'.  */
    PieceColor get(char c, char r) {
        assert validSquare(c, r);
        return get(index(c, r));
    }

    /** Return the current contents of the square at linearized index K. */
    PieceColor get(int k) {
        assert validSquare(k);
        return _linearBoard[k];
    }

    /** Set get(C, R) to V, where 'a' <= C <= 'e', and
     *  '1' <= R <= '5'. */
    private void set(char c, char r, PieceColor v) {
        assert validSquare(c, r);
        set(index(c, r), v);
    }

    /** Set get(K) to V, where K is the linearized index of a square. */
    private void set(int k, PieceColor v) {
        assert validSquare(k);
        _linearBoard[k] = v;
    }

    /** Return true iff MOV is legal on the current board. */
    boolean legalMove(Move mov) {
        if (mov.isJump()) {
            if (checkJump(mov, false)) {
                return true;
            }
        } else {
            if (jumpPossible(index(mov.col0(), mov.row0()))) {
                return false;
            }
            ArrayList<Move> moves = new ArrayList<>();
            getMoves(moves, index(mov.col0(), mov.row0()));
            for (Move m : moves) {
                if (m.equals(mov)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return a list of all legal moves from the current position. */
    ArrayList<Move> getMoves() {
        ArrayList<Move> result = new ArrayList<>();
        getMoves(result);
        return result;
    }

    /** Add all legal moves from the current position to MOVES. */
    void getMoves(ArrayList<Move> moves) {
        if (gameOver()) {
            return;
        }
        if (jumpPossible()) {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getJumps(moves, k);
            }
        } else {
            for (int k = 0; k <= MAX_INDEX; k += 1) {
                getMoves(moves, k);
            }
        }
    }

    /** Add all legal non-capturing moves from the position
     *  with linearized index K to MOVES. Indexed from top counterclockwise*/
    private void getMoves(ArrayList<Move> moves, int k) {
        char c = col(k);
        char r = row(k);
        if (whoseMove() == BLACK && get(k).shortName().charAt(0) == 'b') {
            if (row(k) > '1') {
                if (validSquare(k - 1) && !get(k - 1).isPiece()
                        && (horAllowed[k] != 'r')) {
                    moves.add(Move.move(c, r, col(k - 1), row(k - 1)));
                }
                if (validSquare(k + 1) && !this.get(k + 1).isPiece()
                        && (horAllowed[k] != 'l')) {
                    moves.add(Move.move(c, r, col(k + 1), row(k + 1)));
                }
                if (validSquare(k - 5) && !this.get(k - 5).isPiece()) {
                    moves.add(Move.move(c, r, col(k - 5), row(k - 5)));
                }
                if (k % 2 == 0) {
                    int[] index2 = new int[]{-6, -4};
                    if (validSquare(k - 6) && !this.get(k - 6).isPiece()
                            && col(k - 6) < col(k) && row(k - 6) < row(k)) {
                        moves.add(Move.move(c, r, col(k - 6), row(k - 6)));
                    }
                    if (validSquare(k - 4) && !this.get(k - 4).isPiece()
                            && col(k - 4) > col(k) && row(k - 4) < row(k)) {
                        moves.add(Move.move(c, r, col(k - 4), row(k - 4)));
                    }
                }
            }
        } else if (whoseMove() == WHITE && get(k).shortName().equals("w")) {
            if (row(k) < '5') {
                if (validSquare(k - 1) && !this.get(k - 1).isPiece()
                        && (horAllowed[k] != 'r')) {
                    moves.add(Move.move(col(k), row(k),
                            col(k - 1), row(k - 1)));
                }
                if (validSquare(k + 1) && !this.get(k + 1).isPiece()
                        && (horAllowed[k] != 'l')) {
                    moves.add(
                            Move.move(col(k), row(k), col(k + 1), row(k + 1)));
                }
                if (validSquare(k + 5) && !this.get(k + 5).isPiece()) {
                    moves.add(
                            Move.move(col(k), row(k), col(k + 5), row(k + 5)));
                }
                if (k % 2 == 0) {
                    if (validSquare(k + 6) && !this.get(k + 6).isPiece()
                            && col(k + 6) > col(k) && row(k + 6) > row(k)) {
                        moves.add(Move.move(col(k), row(k),
                                col(k + 6), row(k + 6)));
                    }
                    if (validSquare(k + 4) && !this.get(k + 4).isPiece()
                            && col(k + 4) < col(k) && row(k + 4) > row(k)) {
                        moves.add(Move.move(col(k), row(k),
                                col(k + 4), row(k + 4)));
                    }
                }
            }
        }
    }

    /** Add all legal captures from the position with linearized index K
     *  to MOVES. */
    private void getJumps(ArrayList<Move> moves, int k) {
        if (!jumpPossible(k)
                || !whoseMove().shortName().equals(get(k).shortName())) {
            return;
        }
        int[] index;
        if (k % 2 == 0) {
            index = new int[]{2, -2, 10, -10, 8, -8, 12, -12};
        } else {
            index = new int[]{2, -2, 10, -10};
        }
        for (int i : index) {
            int to = k + i;
            if ((col(k) >= 'd') && i > 0 && i % 10 != 0 && i % 8 != 0) {
                continue;
            } else if ((col(k) <= 'b') && i < 0 && i % 10 != 0 && i % 8 != 0) {
                continue;
            }
            Move m = null;
            if (validSquare(to) && get(k + (i / 2))
                    == whoseMove().opposite() && !get(to).isPiece()) {
                if ((i > 2 && ((col(k + (i / 2)) > col(k))
                        || row(k + (i / 2)) > row(k))) || (i < -2
                        && ((col(k + (i / 2)) < col(k))
                        || row(k + (i / 2)) < row(k)))) {
                    m = Move.move(col(k), row(k), col(to), row(to));
                } else if (i == -2 || i == 2) {
                    m = Move.move(col(k), row(k), col(to), row(to));
                }
                if (m != null && jumpPossible(to))  {
                    moves.add(jumpTrain(m));
                } else if (m != null) {
                    moves.add(m);
                }
            }
        }
    }

    /** Finds entire train of jumps starting from Move START, assumes Move
    START is a valid jump, and for now I think it will only find one train.
     @return Move*/
    private Move jumpTrain(Move start) {
        int from = index(start.col0(), start.row0());
        int to = index(start.col1(), start.row1());
        int p = to - from;
        int[] index;
        Move next = null;
        if (jumpPossible(to)) {
            if (to % 2 == 0) {
                index = new int[]{2, -2, 10, -10, 8, -8, 12, -12};
            } else {
                index = new int[]{2, -2, 10, -10};
            }

            for (int j : index) {
                if (j == -p) {
                    continue;
                }
                if ((col(from) >= 'd') && j > 0) {
                    continue;
                } else if ((col(from) <= 'b') && j < 0) {
                    continue;
                }
                from = to;
                to = to + j;
                if (validSquare(to)
                        && get(from + (j / 2)) == whoseMove().opposite()
                        && !get(to).isPiece()) {
                    next = Move.move(col(from), row(from), col(to), row(to));
                    next.set(col(from), row(from), col(to),
                            row(to), jumpTrain(next));
                }
            }
        }
        start.set(start.col0(), start.row0(), start.col1(), start.row1(), next);
        return start;
    }

    /** Return true iff MOV is a valid jump sequence on the current board.
     *  MOV must be a jump or null.  If ALLOWPARTIAL, allow jumps that
     *  could be continued and are valid as far as they go.  */
    boolean checkJump(Move mov, boolean allowPartial) {
        if (mov == null || !mov.isJump()) {
            return false;
        }
        int to = index(mov.col1(), mov.row1());
        int from = index(mov.col0(), mov.row0());
        int p = to - from;
        if (!jumpPossible(from)) {
            return false;
        }

        if (!validSquare(to) || get(from + (p / 2)) != whoseMove().opposite()
                || get(to).isPiece()) {
            return false;
        }
        if (p % 8 == 0 || p % 12 == 0) {
            if (from % 2 != 0) {
                return false;
            }
        }

        if (mov.jumpTail() == null && allowPartial) {
            return true;
        } else if (mov.jumpTail() == null && !allowPartial
                && jumpPossible(to)) {
            return false;
        } else if (mov.jumpTail() != null) {
            mov = mov.jumpTail();
            checkJump(mov, allowPartial);
        }
        return true;
    }

    /** Return true iff a jump is possible for a piece at position C R. */
    boolean jumpPossible(char c, char r) {
        return jumpPossible(index(c, r));
    }

    /** Return true iff a jump is possible for a piece at position with
     *  linearized index K. */
    boolean jumpPossible(int k) {
        assert validSquare(k);
        if (!whoseMove().shortName().equals(get(k).shortName())) {
            return false;
        }
        if (col(k) >= 'c') {
            if (validSquare(k - 2) && get(k - 1) == whoseMove().opposite()
                    && !get(k - 2).isPiece()) {
                return true;
            }
        }
        if (col(k) <= 'c') {
            if (validSquare(k + 2) && validSquare(k + 1) && get(k + 1)
                    == whoseMove().opposite() && !get(k + 2).isPiece()) {
                return true;
            }
        }
        if (validSquare(k + 10) && get(k + 5) == whoseMove().opposite()
                && !get(k + 10).isPiece()) {
            return true;
        } else if (validSquare(k - 10) && get(k - 5) == whoseMove().opposite()
                && !get(k - 10).isPiece()) {
            return true;
        }
        if (k % 2 == 0) {
            if (col(k) >= 'c') {
                if (validSquare(k + 8) && get(k + 4) == whoseMove().opposite()
                        && !get(k + 8).isPiece() && col(k + 4) < col(k)
                        && row(k + 4) > row(k) && col(k + 8) < col(k)
                        && row(k + 8) > row(k)) {
                    return true;
                } else if (validSquare(k - 12) && get(k - 6)
                        == whoseMove().opposite() && !get(k - 12).isPiece()
                        && col(k - 6) < col(k) && row(k - 6) < row(k)
                        && col(k - 12) < col(k) && row(k - 12) < row(k)) {
                    return true;
                }
            }
            if (col(k) <= 'c') {
                if (validSquare(k + 12) && get(k + 6) == whoseMove().opposite()
                        && !get(k + 12).isPiece() && col(k + 6) > col(k)
                        && row(k + 6) > row(k) && col(k + 12) > col(k)
                        && row(k + 12) > row(k)) {
                    return true;
                } else if (validSquare(k - 8) && get(k - 4)
                        == whoseMove().opposite() && !get(k - 8).isPiece()
                        && col(k - 4) > col(k) && row(k - 4) < row(k)
                        && col(k - 8) > col(k) && row(k - 8) < row(k)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Return true iff a jump is possible from the current board. */
    boolean jumpPossible() {
        for (int k = 0; k <= MAX_INDEX; k += 1) {
            if (jumpPossible(k)) {
                return true;
            }
        }
        return false;
    }

    /** Return the color of the player who has the next move.  The
     *  value is arbitrary if gameOver(). */
    PieceColor whoseMove() {
        return _whoseMove;
    }

    /** Perform the move C0R0-C1R1, or pass if C0 is '-'.  For moves
     *  other than pass, assumes that legalMove(C0, R0, C1, R1). */
    void makeMove(char c0, char r0, char c1, char r1) {
        makeMove(Move.move(c0, r0, c1, r1, null));
    }

    /** Make the multi-jump C0 R0-C1 R1..., where NEXT is C1R1....
     *  Assumes the result is legal. */
    void makeMove(char c0, char r0, char c1, char r1, Move next) {
        makeMove(Move.move(c0, r0, c1, r1, next));
    }

    /** Make the Move MOV on this Board, assuming it is legal. */
    void makeMove(Move mov) {
        if (getMoves().isEmpty()) {
            _gameOver = true;
        }
        if (!legalMove(mov)) {
            return;
        }
        int k = index(mov.col0(), mov.row0());
        int k2 = index(mov.col1(), mov.row1());
        int p = k2 - k;
        if (mov.isJump()) {
            set(k2, whoseMove());
            set(k, EMPTY);
            set(k + p / 2, EMPTY);
            horAllowed[k] = 'b';
            horAllowed[k2] = 'b';
            horAllowed[k + p / 2] = 'b';
            Move m = mov.jumpTail();
            if (m != null) {
                makeMove(m);
                pastBoards.remove(pastBoards.size() - 1);
                _whoseMove = whoseMove().opposite();
            }
        } else {
            if (mov.isLeftMove()) {
                if (horAllowed[k] == 'r') {
                    return;
                }
                set(k, EMPTY);
                set(k - 1, whoseMove());
                horAllowed[k] = 'b';
                horAllowed[k - 1] = 'l';
            } else if (mov.isRightMove()) {
                if (horAllowed[k] == 'l') {
                    return;
                }
                set(k, EMPTY);
                set(k + 1, whoseMove());
                horAllowed[k] = 'b';
                horAllowed[k + 1] = 'r';
            } else {
                horAllowed[k2] = 'b';
                horAllowed[k] = 'b';
                set(k, EMPTY);
                set(k2, whoseMove());
            }
        }
        _whoseMove = whoseMove().opposite();
        pastMoves.add(mov);
        pastBoards.add(new Board(this));
        if (getMoves().isEmpty()) {
            _gameOver = true;
        }
        setChanged();
        notifyObservers();
    }

    /** Undo the last move, if any. */
    void undo() {
        if (pastMoves.size() <= 1) {
            if (pastBoards.size() > 1) {
                internalCopy(pastBoards.get(0));
                return;
            }
            return;
        }
        Move m =  pastMoves.get(pastMoves.size() - 1);
        int init = index(m.col0(), m.row0());

        if (!m.isJump()) {
            if (m.isLeftMove()) {
                if (pastMoves.size() > 2) {
                    Move q = pastMoves.get(pastMoves.size() - 2);
                    if (q.isLeftMove()) {
                        horAllowed[init] = 'l';
                    } else {
                        horAllowed[init] = 'b';
                    }
                }
            } else if (m.isRightMove()) {
                if (pastMoves.size() > 2) {
                    Move p = pastMoves.get(pastMoves.size() - 2);
                    if (p.isRightMove()) {
                        horAllowed[init] = 'r';
                    } else {
                        horAllowed[init] = 'b';
                    }
                }
            }
        }

        internalCopy(pastBoards.get(pastBoards.size() - 1));
        pastMoves.remove(pastMoves.size() - 1);
        _gameOver = false;
        setChanged();
        notifyObservers();
    }

    @Override
    public String toString() {
        return toString(false);
    }
    /** Returns the linear board as one String Object.
     * @param b */
    String toStringList(Board b) {
        String result = "";
        for (int i = 0; i < b._linearBoard.length; i++) {
            if (i == b._linearBoard.length - 1) {
                result = result + b.get(i).shortName();
            } else {
                result = result + b.get(i).shortName() + " ";
            }
        }
        return result;
    }
    /** Return a text depiction of the board.  If LEGEND, supply row and
     *  column numbers around the edges. */
    String toString(boolean legend) {
        Formatter out = new Formatter();
        if (legend) {
            for (char j = '5'; j > '0'; j = (char) (j - 1)) {
                out.format("%1$c ", j);
                for (char i = 'a'; i < 'f'; i = (char) (i + 1)) {
                    out.format(" %1$c",
                            this.get(index(i, j)).shortName().charAt(0));
                }
                out.format("%n");
            }
            out.format("   a b c d e");
        } else {
            for (char j = '5'; j > '0'; j = (char) (j - 1)) {
                out.format(" ");
                for (char i = 'a'; i < 'f'; i = (char) (i + 1)) {
                    out.format(" %1$c",
                            this.get(index(i, j)).shortName().charAt(0));
                }
                if (j != '1') {
                    out.format("%n");
                }
            }
        }
        return out.toString();
    }

    /** Return true iff there is a move for the current player. */
    private boolean isMove() {
        if (getMoves().isEmpty()) {
            return false;
        }
        return true;
    }

    /** Player that is on move. */
    private PieceColor _whoseMove;

    /** Set true when game ends. */
    private boolean _gameOver;

    /** Convenience value giving values of pieces at each ordinal position. */
    static final PieceColor[] PIECE_VALUES = PieceColor.values();


    /** One cannot create arrays of ArrayList<Move>, so we introduce
     *  a specialized private list type for this purpose. */
    private static class MoveList extends ArrayList<Move> {
    }

    /** A list of Boards, used to store past states of the game.*/
    private static class BoardList extends ArrayList<Board> {
    }

    /** Overriding the .equals method, BECAUSE YOU TOLD ME TOOOOO. */
    @Override
    public boolean equals(Object o) {
        if (o instanceof Board) {
            Board b = (Board) o;
            return (toStringList(b).equals(toStringList(this))
                    && _whoseMove == b.whoseMove());
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    /** A read-only view of a Board. */
    private class ConstantBoard extends Board implements Observer {
        /** A constant view of this Board. */
        ConstantBoard() {
            super(Board.this);
            Board.this.addObserver(this);
        }

        @Override
        void copy(Board b) {
            assert false;
        }

        @Override
        void clear() {
            assert false;
        }

        @Override
        void makeMove(Move move) {
            assert false;
        }

        /** Undo the last move. */
        @Override
        void undo() {
            assert false;
        }

        @Override
        public void update(Observable obs, Object arg) {
            super.copy((Board) obs);
            setChanged();
            notifyObservers(arg);
        }
    }
}

