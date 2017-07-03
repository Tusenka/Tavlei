package entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * represents a 2D coordinate pair
 *
 * @author Joe
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PositionType", propOrder = {
        "row",
        "col"
})
public class Position implements Serializable {
    private int row;
    private int col;

    /**
     * Constructs a 2D pair
     *
     * @param row
     * @param col
     */
    public Position(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public Position() {
    }

    /**
     * @return the row
     */
    public int getRow() {
        return row;
    }

    /**
     * @return the column
     */
    public int getCol() {
        return col;
    }

    //return positions around title
    private Position up() {
        return new Position(this.getRow() + 1, this.getCol());
    }

    private Position down() {
        return new Position(this.getRow() - 1, this.getCol());
    }

    private Position right() {
        return new Position(this.getRow(), this.getCol() + 1);
    }

    private Position left() {
        return new Position(this.getRow(), this.getCol() - 1);
    }

    public ArrayList<Position> cross() {
        ArrayList<Position> result = new ArrayList<>();
        result.add(up());
        result.add(right());
        result.add(down());
        result.add(left());
        return result;
    }

    @Override
    public int hashCode() {
        return 100 * col + row;
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Position)) {
            return false;
        }
        Position p = (Position) o;
        return p.row == this.row && p.col == this.col;
    }

    @Override
    //Do not touch this method
    public String toString() {
        return "(" +
                row +
                ", " +
                col +
                ")";
    }

    public void setRow(int row) {
        this.row = row;
    }

    public void setCol(int col) {
        this.col = col;
    }
}
