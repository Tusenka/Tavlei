package JavaFxClient.boardview;

import JavaFxClient.controllers.JavaTavleiClientController;
import JavaFxClient.model.MyAppDataConfig;
import entity.Logger.LoggableI;
import entity.MyAppConfigurationLoader;
import entity.Side;
import gamemechanics.controller.tavlei.TavleiControllerManager;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import org.springframework.context.ApplicationContext;

import java.io.FileNotFoundException;
import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * Main class for the tavlei application
 * Sets up the top level of the GUI
 *
 * @author Irina
 */
public class TavleiFX extends Application implements LoggableI {

    @SuppressWarnings("CanBeFinal")//Could be replaced
    private TavleiControllerManager controllerManager;
    @SuppressWarnings("CanBeFinal")
    private BoardView board;
    private Side playerSide = Side.BLACK;
    private Side aiSide = Side.WHITE;


    public TavleiFX() {
        super();
        ApplicationContext context = MyAppDataConfig.getContext();
        controllerManager = (TavleiControllerManager) context.getBean("defaultTavleiControllerManager");
        controllerManager.playWithLocalUser();
        this.board = new TavleiBoardView();
    }

    @Override
    public void start(Stage primaryStage) {
        board.reset();
        primaryStage.setTitle("Tavlei");
        HBox root = new HBox();
        root.getChildren().add(board.getView());
        Button reset = new Button("Reset");
        Button host = new Button("Host");
        Button advise = new Button("Help me!");
        TitledPane playComputerGroup = new TitledPane();
        playComputerGroup.setText("Play Computer");

        Button playComputer = new Button("Play!");
        ToggleGroup playerSideGroup = new ToggleGroup();
        RadioButton blackSide = new RadioButton("Black side");
        RadioButton whiteSide = new RadioButton("White side");
        blackSide.setToggleGroup(playerSideGroup);
        whiteSide.setToggleGroup(playerSideGroup);
        //Set up AI toggle
        if (this.aiSide.equals(Side.WHITE)) {
            playerSideGroup.selectToggle(blackSide);
        } else {
            playerSideGroup.selectToggle(whiteSide);
        }

        // Content for playComputerGroup
        VBox playComputerContent = new VBox();
        playComputerContent.getChildren().add(whiteSide);
        playComputerContent.getChildren().add(blackSide);
        playComputerContent.getChildren().add(playComputer);
        playComputerGroup.setContent(playComputerContent);
        playComputerGroup.setCollapsible(false);


        TextField textField = new TextField();
        Label ip = new Label();
        try {
            ip = new Label(InetAddress.getLocalHost().toString());
        } catch (UnknownHostException e) {
            getLog().fatal("Can't find localhost...");
        }
        Button join = new Button("Join");

        //Button actions
        reset.setOnAction(e -> {
            stop();
            controllerManager.playWithLocalUser();
            start(primaryStage);
        });

        playComputer.setOnAction(e -> {
            stop();
            this.aiSide = blackSide.isSelected() ? Side.WHITE : Side.BLACK;
            this.playerSide = this.aiSide.getOppositeSide();
            this.board.setMySide(playerSide);
            controllerManager.playWithCompute(playerSide);
            start(primaryStage);
        });

        advise.setOnAction(event ->
                board.adviceNextMove()
        );


        join.setOnAction(e -> {
            stop();
            try {
                //TODO: Rearrange default port and host from LocalUserController to here
                String hostAddress = textField.getText();
                if (hostAddress.isEmpty())
                    hostAddress = "http://localhost:" + loadNetworkConfig().getString("server.port");
                board.setStateLabel(new Label("Connecting to the server..." + hostAddress));
                controllerManager.joinToServer(hostAddress);
            } catch (JavaTavleiClientController.NotConnectedException e1) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Can't connect to the server");
                alert.setHeaderText(null);
                alert.setContentText("Check if the server with address host" + textField.getText() + "available and allow to connect. Or try another address. ");
                alert.showAndWait();
            }
            start(primaryStage);
        });

        VBox panel = new VBox();
        panel.setAlignment(Pos.BASELINE_CENTER);
        HBox but1 = new HBox();
        but1.setPadding(new Insets(15));
        but1.setSpacing(10);
        but1.getChildren().add(reset);
        but1.getChildren().add(advise);
        but1.getChildren().add(playComputerGroup);
        panel.getChildren().add(but1);


        board.setStateLabel(new Label(controllerManager.getCurrentState().toString()));
        board.setTurnLabel(new Label(controllerManager.getCurrentSide() + "'s Turn"));

        HBox but0 = new HBox();
        but0.setSpacing(10);
        but0.setPadding(new Insets(10));
        but0.getChildren().add(board.getTurnLabel());
        but0.getChildren().add(board.getStateLabel());
        panel.getChildren().add(but0);


        HBox but3 = new HBox();
        but3.setSpacing(10);
        but3.getChildren().add(ip);
        but3.getChildren().add(textField);
        but3.getChildren().add(host);
        but3.getChildren().add(join);
        panel.getChildren().add(but3);

        root.getChildren().add(panel);
        primaryStage.setScene(new Scene(root, board.getBoardSize() * TileView.titleSize + 15 + 450, board.getBoardSize() * TileView.titleSize + 35));
        primaryStage.show();
        controllerManager.startGame();
    }

    public void stop() {
        controllerManager.reset();
    }

    public static void start(String[] args) throws FileNotFoundException {

        launch(args);
    }

    private static MyAppConfigurationLoader loadNetworkConfig() {
        MyAppConfigurationLoader config;
        try {
            config = new MyAppConfigurationLoader("application.properties");
        } catch (MyAppConfigurationLoader.PropertiesException e) {
            e.printStackTrace();
            return new MyAppConfigurationLoader();
        }
        return config;

    }

    public static void main(String[] args) throws FileNotFoundException {
        launch(args);
    }


}
