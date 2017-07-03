package gamemechanics.model.tavlei;

import entity.Move;
import entity.Position;
import entity.Side;
import entity.SideHasNoMovesException;
import gamemechanics.model.Piece;
import jdk.nashorn.internal.ir.annotations.Immutable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Stateful TavleiBoardImpl. Uses Strategy pattern. Uses @{@link TavleiRules} as the strategy for game rules.
 *
 * @author Irina
 */
public class TavleiBoardImpl implements TavleiBoard {

    private final static byte SIZE = 9;

    @Override
    public Map<Piece, Position> getWhitePositions() {
        return whitePositions;
    }

    @Override
    public Map<Piece, Position> getBlackPositions() {
        return blackPositions;
    }

    private final Map<Piece, Position> whitePositions;
    private final Map<Piece, Position> blackPositions;
    private Piece king;
    private final Piece[][] board;
    private Position throne;
    private ArrayList<Position> exits;
    @SuppressWarnings("WeakerAccess")
    protected TavleiRules rules;
    @SuppressWarnings("WeakerAccess")
    protected Move lastMove;


    //creates a chess board with all pieces in the right place
    public TavleiBoardImpl() {
        whitePositions = new HashMap<>();
        blackPositions = new HashMap<>();
        board = new TavleiPiece[getSize()][getSize()];
        rules = new TavleiRulesImpl(this);
        this.lastMove = new Move(new Position(0, 0), new Position(0, 0));
        initPositions();
    }

    @SuppressWarnings("WeakerAccess")
    public TavleiBoardImpl(TavleiBoardImpl b) {
        board = new TavleiPiece[getSize()][getSize()];

        for (int row = 0; row < board.length; row++) {
            System.arraycopy(b.board[row], 0, board[row], 0, board[row].length);
        }

        whitePositions = new HashMap<>(b.whitePositions);
        blackPositions = new HashMap<>(b.blackPositions);
        this.king = b.king;
        this.exits = b.exits;//! Due to exits are permanent for the board, it doesn't need to copy it.
        this.rules = b.rules.clone(this);
        this.lastMove = b.lastMove;
        this.throne = b.throne;//! Due throne is permanent for the board, it doesn't need to copy it.
    }

    @SuppressWarnings("WeakerAccess")
    protected void initPositions() {
        byte center = (byte) (getSize() / 2);
        // /Place for throne
        this.throne = new Position(center, center);
        //Places for exits;
        this.exits = new ArrayList<>(4);
        this.exits.add(new Position(0, 0));
        this.exits.add(new Position(0, getSize() - 1));
        this.exits.add(new Position(getSize() - 1, 0));
        this.exits.add(new Position(getSize() - 1, getSize() - 1));

        PieceFabric pieceFabric = new PieceFabric(this::outOfBound);
        // Defenders
        for (byte i = 2; i < getSize() - 1; i++) {
            if (i == center) continue;
            placePiece(pieceFabric.newRook(Side.WHITE), new Position(center, i));
            placePiece(pieceFabric.newRook(Side.WHITE), new Position(i, center));
        }

        this.king = pieceFabric.newKing();
        placePiece(this.king, new Position(center, center));

        // Attackers
        for (byte i = 3; i < 6; i++) {
            if (i == center) {
                placePiece(pieceFabric.newRook(Side.BLACK), new Position(1, i));
                placePiece(pieceFabric.newRook(Side.BLACK), new Position(i, 1));
                placePiece(pieceFabric.newRook(Side.BLACK), new Position(getSize() - 2, i));
                placePiece(pieceFabric.newRook(Side.BLACK), new Position(i, getSize() - 2));
            }
            placePiece(pieceFabric.newRook(Side.BLACK), new Position(0, i));
            placePiece(pieceFabric.newRook(Side.BLACK), new Position(i, 0));
            placePiece(pieceFabric.newRook(Side.BLACK), new Position(getSize() - 1, i));
            placePiece(pieceFabric.newRook(Side.BLACK), new Position(i, getSize() - 1));
        }
    }


    @Override
    public Position getThrone() {
        return throne;
    }

    @Override
    public ArrayList<Position> getExits() {
        return exits;
    }

    @Override
    public byte getSize() {
        return SIZE;
    }

    @Override
    public Move getLastMove() {
        return lastMove;
    }

    @Override
    public void movePiece(Piece p, Move m) {

        Position destPos = m.getDestination();

        //remove a piece
        if (!m.getDefeated().isEmpty()) {
            for (Position remPiece : m.getDefeated()) {
                removePiece(getPieceAt(remPiece));
            }
        }

        //move p
        removePiece(p);
        placePiece(p, destPos);
        lastMove = m;
    }

    /**
     * Generate moves from side with additional information about each move;     *
     */

    @Override
    public Map<Piece, Set<Move>> generateAllMovesForSide(Side s)
            throws SideHasNoMovesException {

        Map<Piece, Set<Move>> allMoves = new HashMap<>();
        Map<Piece, Position> piecePositions = (s == Side.WHITE)
                ? whitePositions
                : blackPositions;

        for (Map.Entry<Piece, Position> p : piecePositions.entrySet()) {
            Set<Move> moves = p.getKey().generateMoves(p.getValue());
            Set<Move> filtered = moves
                    .stream()
                    .filter(m -> pieceCanMove(m, s))
                    .map(move -> addInfoMove(move, s))
                    .collect(Collectors.toCollection(HashSet<Move>::new));
            allMoves.put(p.getKey(), filtered);
        }

        if (allMoves.isEmpty()) {
            throw new SideHasNoMovesException(s.toString() + " has no moves.");
        }

        return allMoves;
    }

    @Override
    public Move addInfoMove(Move m, Side s) {
        m.setDefeated(getDefeatableFrom(m.getDestination(), s));
        return m;
    }

    @Override
    @Immutable
    public boolean pieceCanMove(Move m, Side movingSide) {
        return rules.pieceCanMove(m, movingSide);
    }


    @Override
    public Map<Piece, Position> getAllActivePiecesPositions() {
        Map<Piece, Position> all = new HashMap<>();
        all.putAll(whitePositions);
        all.putAll(blackPositions);
        return all;
    }

    @Override
    public boolean outOfBound(int row, int col) {
        return row < 0 || row >= getSize() || col < 0 || col >= getSize();
    }

    @Override
    public Piece getPieceAt(Position p) {
        return getPieceAt(p.getRow(), p.getCol());
    }

    @Override
    public void replacePieceAt(Position pos, Piece newPiece) {
        Piece old = getPieceAt(pos);
        removePiece(old);
        if (newPiece != null) {
            placePiece(newPiece, pos);
        }
    }


    @SuppressWarnings("WeakerAccess")
    protected void placePiece(Piece p, Position pos) {
        if (p.getSide().equals(Side.BLACK)) {
            blackPositions.put(p, pos);
        } else {
            whitePositions.put(p, pos);
        }
        board[pos.getRow()][pos.getCol()] = p;
    }

    @Override
    public void removePiece(Piece p) {
        if (p == null) return;

        Position pos;

        if (p.getSide().equals(Side.BLACK)) {
            pos = blackPositions.get(p);
            blackPositions.remove(p);
        } else {
            pos = whitePositions.get(p);
            whitePositions.remove(p);
        }
        board[pos.getRow()][pos.getCol()] = null;
    }

    @Override
    public Position getKingPosition() {
        return this.whitePositions.get(king);
    }

    @Override
    public boolean isWin(Side s) {
        return rules.isWin(s);
    }

    @SuppressWarnings("CloneDoesntCallSuperClone")
    public TavleiBoard clone() {
        //I don't need super.clone here
        return new TavleiBoardImpl(this);
    }

    @Override
    public void setRules(TavleiRules rules) {
        this.rules = rules.clone(this);
    }

    @SuppressWarnings("WeakerAccess")

    protected ArrayList<Position> getDefeatableFrom(Position p, Side s) {
        return rules.getDefeatableFrom(p, s);
    }

    @Override
    public int hashCode() {
        return java.util.Arrays.deepHashCode(board) + rules.getBoardInfoHash();
    }

    /*
    * Helper methods, usually used for Rules
    */
    @Override
    public boolean isKingAtThrone() {
        return isThronePosition(getKingPosition()) || getKingPosition().cross().stream().anyMatch(position -> (isThronePosition(position)));
    }

    /*
    * Return if the Piece is near King
    * */
    @Override
    public boolean isPieceAtKing(Position p) {
        return getKingPosition().cross().stream().anyMatch(position -> (p.equals(position)));
    }

    @Override
    public boolean isExitPosition(Position p) {
        return exits.contains(p);
    }

    @Override
    public boolean isThronePosition(Position p) {
        return p.equals(throne);
    }

    @Override
    public boolean isEmptyAt(Position p) {
        if (p == null) return true;
        Piece piece = getPieceAt(p);
        return (piece == null);
    }

    /**
     * Check if piece of side s exists in the position p
     *
     * @param p
     * @param s
     * @return Piece with the specific side exists in the position p
     */
    @Override
    public boolean isPieceAt(Position p, Side s) {
        if (s == null) return false;
        Piece piece = getPieceAt(p);
        return (piece != null && piece.getSide().equals(s));
    }

    @Override
    public Side getSideFromPosition(Position p) {
        Piece piece = getPieceAt(p);
        if (piece == null) return null;
        return piece.getSide();
    }

    @Override
    public boolean isKing(Piece piece) {
        return piece != null && piece.equals(this.king);
    }

    @Override
    public boolean isKing(Position pos) {
        return pos != null && getPieceAt(pos) != null && isKing(getPieceAt(pos));
    }

    @Override
    public boolean isInSpecialFields(Position p) {
        return isExitPosition(p) || isThronePosition(p);
    }

    /**
     * Helper method for fast checking if the piece exists at the positions
     *
     * @param row
     * @param col
     * @return
     */
    @Override
    public Piece getPieceAt(int row, int col) {
        if (outOfBound(row, col)) return null;
        return board[row][col];
    }


}
