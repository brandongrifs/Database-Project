package qirkat;

/** Class that represents moves as a combo of move and heuristic value.
 * @author Brandon Griffin*/
class MoveValue {

    /** Value of the current MoveValue object. */
    private double returnValue;

    /** Move of the current MoveValue object. */
    private Move returnMove;

    /** Empty constructor. */
    public MoveValue() {
        returnValue = 0;
    }

    /** Constructor with only a value.
     *  @param val value to be assigned to this. */
    public MoveValue(double val) {
        this.returnValue = val;
    }

    /** Full constructor.
     * @param val value to be constructed.
     * @param m move to be constructed. */
    public MoveValue(double val, Move m) {
        this.returnValue = val;
        this.returnMove = m;
    }

    /** Return this MoveValue's move.
     * @return Move */
    public Move getMove() {
        return this.returnMove;
    }

    /** Setter for this.move.
     * @param m  move to be set to this. */
    public void setMove(Move m) {
        this.returnMove = m;
    }

    /** Return this MoveValue's value.
     * @return double */
    public double getValue() {
        return this.returnValue;
    }

    /** Setter for this.value.
     * @param v value to be set to this. */
    public void setValue(double v) {
        this.returnValue = v;
    }

}
