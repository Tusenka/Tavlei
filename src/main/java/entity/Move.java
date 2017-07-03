package entity;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * A 2D translation object. Contains information about whether the move is
 * continuous, or represents a jump
 *
 * @author Joe
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "MoveType", propOrder = {
        "start",
        "destination",
        "defeated"
})
public class Move implements Serializable {
    @XmlElement(required = true)
    private Position start;
    @XmlElement(required = true)
    private Position destination;
    private ArrayList<Position> defeated = new ArrayList<>();

    public Move() {
    }

    public Move(Position start, Position destination) {
        this.start = start;
        this.destination = destination;
    }

    public Move(Position start, Position destination, ArrayList<Position> defeated) {
        this.start = start;
        this.destination = destination;
        this.defeated = new ArrayList<>(defeated);
    }

    /**
     * @return the start position of this move
     */
    public Position getStart() {
        return start;
    }

    /**
     * @return the destination position of this move
     */
    public Position getDestination() {
        return destination;
    }

    @Override
    public int hashCode() {
        return start.hashCode() - destination.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (null == o) {
            return false;
        }
        if (this == o) {
            return true;
        }
        if (!(o instanceof Move)) {
            return false;
        }
        Move m = (Move) o;
        return start.equals(m.start) && destination.equals(m.destination);
    }

    public void setStart(Position start) {
        this.start = start;
    }

    public void setDestination(Position destination) {
        this.destination = destination;
    }

    @Override
    public String toString() {
        return String.valueOf(start) +
                "->" +
                destination;
    }

    public ArrayList<Position> getDefeated() {
        return defeated;
    }

    public void setDefeated(ArrayList<Position> defeated) {
        this.defeated = defeated;
    }

}
