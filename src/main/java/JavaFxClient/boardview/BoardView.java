package JavaFxClient.boardview;

import entity.Side;
import javafx.scene.control.Label;
import javafx.scene.layout.Pane;


/**
 * Created by Irina
 */
interface BoardView {
    byte getBoardSize();

    Label getTurnLabel();

    void setTurnLabel(Label turnLabel);

    Label getStateLabel();

    void setStateLabel(Label stateLabel);

    Pane getView();

    void adviceNextMove();

    Side getMySide();

    void setMySide(Side mySide);

    void reset();
}
