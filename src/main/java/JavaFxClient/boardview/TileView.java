package JavaFxClient.boardview;

import entity.MyAppConfigurationLoader;
import entity.Position;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;

/**
 * View class for a tile on a board
 * A tile should be able to display a piece
 * as well as highlight itself during the game.
 *
 * @author <Irina>
 */
public class TileView implements Tile {

    private final Position position;
    private final StackPane root;
    private String symbol;
    public static final int titleSize = 100;

    public void setColor(Color color) {
        this.color = color;
        root.setStyle("-fx-background-color: #" + this.color.toString().substring(2));
    }

    private Color color=null;

    /**
     * Creates a TileView with a specified position
     *
     * @param p
     */
    public TileView(Position p) {
        position = p;
        MyAppConfigurationLoader config = loadConfiguration();

        root = new StackPane();
        root.setCache(false);
        //Add background
        root.setMinWidth(titleSize);
        root.setMinHeight(titleSize);

        //Determine background color based on location for not specified tiles
        if (this.color==null) {
            if ((p.getRow() + p.getCol()) % 2 == 0) {
                setColor(config.getFXColor("odd.color"));
            } else {
                setColor(config.getFXColor("even.color"));
            }
        }


    }

    private MyAppConfigurationLoader loadConfiguration() {
        MyAppConfigurationLoader config;
        try {
            config = new MyAppConfigurationLoader("TavleiBoardView.properties");
        } catch (MyAppConfigurationLoader.PropertiesException e) {
            throw new RuntimeException(e);
        }
        return config;

    }

    @Override
    public Position getPosition() {
        return position;
    }


    @Override
    public Node getRootNode() {
        return root;
    }

    @Override
    public void setSymbol(String symbol) {
        this.symbol = symbol;
        if (symbol.equals("")) {
            //Remove piece
            root.getChildren().clear();
        } else {
            //Add piece
            Canvas symbolCanvas = new Canvas(titleSize, titleSize);
            GraphicsContext gc = symbolCanvas.getGraphicsContext2D();
            gc.setFont(new Font(titleSize));
            gc.fillText(getSymbol(), 0, titleSize * 0.9);
            root.getChildren().add(symbolCanvas);
        }
    }


    @Override
    public String getSymbol() {
        return symbol;
    }

    @Override
    public void highlight(Color highlightColor) {
        root.setStyle("-fx-background-color: #" + highlightColor.toString().substring(2));
    }

    @Override
    public void clear() {
        root.setStyle("-fx-background-color: #" + this.color.toString().substring(2));
    }

}
