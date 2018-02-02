package qirkat;

import static qirkat.PieceColor.*;
import static qirkat.Command.Type.*;

/** A Player that receives its moves from its Game's getMoveCmnd method.
 *  @author Brandon Griffin
 */
class Manual extends Player {

    /** A Player that will play MYCOLOR on GAME, taking its moves from
     *  GAME. */
    Manual(Game game, PieceColor myColor) {
        super(game, myColor);
        _prompt = myColor + ": ";
    }

    @Override
    Move myMove() {

        String[] move = game().getMoveCmnd(_prompt).operands();
        Move m = Move.parseMove(move[0]);
        for (int i = 1; i < move.length; i++) {
            m = Move.move(m, Move.parseMove(move[i]));
        }
        return m;
    }

    /** Identifies the player serving as a source of input commands. */
    private String _prompt;
}

