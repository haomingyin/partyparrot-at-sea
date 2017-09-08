package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import com.jfoenix.controls.JFXDialog.DialogTransition;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import seng302.gameServer.GameStages;
import seng302.gameServer.GameState;
import seng302.model.Colors;
import seng302.model.RaceState;
import seng302.visualiser.ServerListener;

public class LobbyController implements Initializable {

    @FXML
    private VBox playerListVBox;

    @FXML
    private ScrollPane playerListScrollpane;

    @FXML
    private JFXButton customizeButton, leaveLobbyButton, beginRaceButton;

    @FXML
    private StackPane serverListMainStackPane;

    @FXML
    private Label serverName;

    @FXML
    private Label mapName;

    private List<LobbyController_old.LobbyCloseListener> lobbyListeners = new ArrayList<>();
    private RaceState raceState;
    private JFXDialog customizationDialog;

    @Override
    public void initialize(URL location, ResourceBundle resources) {

        leaveLobbyButton.setOnMouseReleased(event -> leaveLobby());
        beginRaceButton.setOnMouseReleased(event -> beginRace());

        Platform.runLater(() -> {
            serverName.setText(ViewManager.getInstance().getProperty("serverName"));
            mapName.setText(ViewManager.getInstance().getProperty("mapName"));

            ViewManager.getInstance().getPlayerList().addListener((ListChangeListener<String>) c -> Platform.runLater(this::refreshPlayerList));

            ViewManager.getInstance().getPlayerList().setAll(ViewManager.getInstance().getPlayerList().sorted());
        });

        Platform.runLater(() -> {
            Integer playerId = ViewManager.getInstance().getGameClient().getServerThread().getClientId();
            String name = ViewManager.getInstance().getGameClient().getPlayerNames().get(playerId - 1);

            Color playerColor = Colors.getColor( playerId - 1);
            customizationDialog = ViewManager.getInstance().loadCustomizationDialog(serverListMainStackPane, this, playerColor, name);

            customizeButton.setOnMouseReleased(event -> customizationDialog.show());
        });

    }

    private void beginRace() {
        beginRaceButton.setDisable(true);
        customizeButton.setDisable(true);
        GameState.setCurrentStage(GameStages.PRE_RACE);
        GameState.resetStartTime();
        Platform.runLater(()-> ViewManager.getInstance().getGameClient().startGame());
    }

    private void refreshPlayerList() {
        playerListVBox.getChildren().clear();

        for (String player : ViewManager.getInstance().getPlayerList()) {
            VBox pane = null;

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/views/cells/PlayerCell.fxml"));

            loader.setController(new PlayerCell(player));

            try {
                pane = loader.load();
            } catch (IOException e) {
                // TODO replace with logger
                e.printStackTrace();
            }

            playerListVBox.getChildren().add(pane);
        }
    }

    public void leaveLobby() {
        // TODO: 10/07/17 wmu16 - Finish function!
        GameState.setCurrentStage(GameStages.CANCELLED);
//        for (LobbyController_old.LobbyCloseListener readyListener : lobbyListeners)
//            readyListener.notify(LobbyController_old.CloseStatus.LEAVE);
        ViewManager.getInstance().getGameClient().stopGame();
        ViewManager.getInstance().goToStartView();
    }

    public void disableReadyButton() {
        this.beginRaceButton.setDisable(true);
        this.beginRaceButton.setText("Waiting for host...");
    }
    
    public void updateRaceState(RaceState raceState){
        this.raceState = raceState;
        this.beginRaceButton.setText("Starting in: " + raceState.getRaceTimeStr());
    }

    public void closeCustomizationDialog() {
        customizationDialog.close();
    }
}
