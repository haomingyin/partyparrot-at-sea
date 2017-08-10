package seng302.visualiser;

import java.io.IOException;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Map;
import java.util.TimeZone;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import seng302.gameServer.MainServerThread;
import seng302.model.RaceState;
import seng302.model.Yacht;
import seng302.model.stream.packets.StreamPacket;
import seng302.model.stream.parser.MarkRoundingData;
import seng302.model.stream.parser.PositionUpdateData;
import seng302.model.stream.parser.PositionUpdateData.DeviceType;
import seng302.model.stream.parser.RaceStatusData;
import seng302.model.stream.parser.YachtEventData;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.model.stream.xml.parser.RegattaXMLData;
import seng302.gameServer.server.messages.BoatActionMessage;
import seng302.gameServer.server.messages.BoatActionType;
import seng302.utilities.StreamParser;
import seng302.utilities.XMLParser;
import seng302.visualiser.controllers.LobbyController;
import seng302.visualiser.controllers.LobbyController.CloseStatus;
import seng302.visualiser.controllers.RaceViewController;

/**
 * Created by cir27 on 20/07/17.
 */
public class GameClient {

    private Pane holderPane;
    private ClientToServerThread socketThread;
    private MainServerThread server;

    private RaceViewController raceView;

    private Map<Integer, Yacht> allBoatsMap;
    private RegattaXMLData regattaData;
    private RaceXMLData courseData;
    private RaceState raceState = new RaceState();

    private ObservableList<String> clientLobbyList = FXCollections.observableArrayList();

    private long lastSendingTime;
    private int KEY_STROKE_SENDING_FREQUENCY = 50;

    public GameClient(Pane holder) {
        this.holderPane = holder;
    }

    public void runAsClient(String ipAddress, Integer portNumber) {
        try {
            socketThread = new ClientToServerThread(ipAddress, portNumber);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Unable to connect to host...");
        }
        socketThread.addStreamObserver(this::parsePackets);
        LobbyController lobbyController = loadLobby();
        lobbyController.setPlayerListSource(clientLobbyList);
        lobbyController.disableReadyButton();
        lobbyController.setTitle("Connected to host - IP : " + ipAddress + " Port : " + portNumber);
        lobbyController.addCloseListener((exitCause) -> this.loadStartScreen());
    }

    public void runAsHost(String ipAddress, Integer portNumber) {
        server = new MainServerThread();
        try {
            socketThread = new ClientToServerThread(ipAddress, portNumber);
        } catch (IOException ioe) {
            ioe.printStackTrace();
            System.out.println("Unable to make local connection to host...");
        }
        socketThread.addStreamObserver(this::parsePackets);
        LobbyController lobbyController = loadLobby();
        lobbyController.setPlayerListSource(clientLobbyList);
        lobbyController.setTitle("Hosting Lobby - IP : " + ipAddress + " Port : " + portNumber);
        lobbyController.addCloseListener(exitCause -> {
            if (exitCause == CloseStatus.READY) {
                server.startGame();
            } else if (exitCause == CloseStatus.LEAVE) {
                loadStartScreen();
            }
        });

        server.setGameClient(this);
    }

    private void loadStartScreen() {
        socketThread.setSocketToClose();
        socketThread = null;
        if (server != null) {
            // TODO: 26/07/17 cir27 - handle disconnecting
//            server.shutDown();
            server = null;
        }
        FXMLLoader fxmlLoader = new FXMLLoader(
            getClass().getResource("/views/StartScreenView.fxml"));
        try {
            holderPane.getChildren().clear();
            holderPane.getChildren().add(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Loads a view of the lobby into the clients pane
     *
     * @return the lobby controller.
     */
    private LobbyController loadLobby() {
        FXMLLoader fxmlLoader = new FXMLLoader(
            GameClient.class.getResource("/views/LobbyView.fxml"));
        try {
            holderPane.getChildren().clear();
            holderPane.getChildren().add(fxmlLoader.load());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return fxmlLoader.getController();
    }

    private void loadRaceView() {
        FXMLLoader fxmlLoader = new FXMLLoader(
            RaceViewController.class.getResource("/views/RaceView.fxml"));
        try {
            final Node node = fxmlLoader.load();
            Platform.runLater(() -> {
                holderPane.getChildren().clear();
                holderPane.getChildren().add(node);
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        holderPane.getScene().setOnKeyPressed(this::keyPressed);
        holderPane.getScene().setOnKeyReleased(this::keyReleased);
        raceView = fxmlLoader.getController();
        Yacht player = allBoatsMap.get(socketThread.getClientId());
        raceView.loadRace(allBoatsMap, courseData, raceState, player);
    }

    private void parsePackets() {
        while (socketThread.getPacketQueue().peek() != null) {
            StreamPacket packet = socketThread.getPacketQueue().poll();
            switch (packet.getType()) {
                case RACE_STATUS:
                    processRaceStatusUpdate(StreamParser.extractRaceStatus(packet));
                    startRaceIfAllDataReceived();
                    break;

                case REGATTA_XML:
                    regattaData = XMLParser.parseRegatta(
                        StreamParser.extractXmlMessage(packet)
                    );
                    raceState.setTimeZone(
                        TimeZone.getTimeZone(
                            ZoneId.ofOffset("UTC", ZoneOffset.ofHours(regattaData.getUtcOffset()))
                        )
                    );
                    break;

                case RACE_XML:
                    courseData = XMLParser.parseRace(
                        StreamParser.extractXmlMessage(packet)
                    );
                    if (raceView != null) {
                        raceView.updateRaceData(courseData);
                    }
                    break;

                case BOAT_XML:
//                    System.out.println("GOT SUM BOATS YAY :)");
                    allBoatsMap = XMLParser.parseBoats(
                        StreamParser.extractXmlMessage(packet)
                    );
                    clientLobbyList.clear();
                    allBoatsMap.forEach((id, boat) -> {
                        clientLobbyList.add(id + " " + boat.getBoatName());
//                        System.out.println(id + " " + boat.getBoatName());

                    });
//                    startRaceIfAllDataReceived();
                    break;

                case RACE_START_STATUS:
                    raceState.updateState(StreamParser.extractRaceStartStatus(packet));
                    break;

                case BOAT_LOCATION:
                    updatePosition(StreamParser.extractBoatLocation(packet));
                    break;

                case MARK_ROUNDING:
                    updateMarkRounding(StreamParser.extractMarkRounding(packet));
                    break;

                case YACHT_EVENT_CODE:
                    showCollisionAlert(StreamParser.extractYachtEventCode(packet));
                    break;
            }
        }
    }

    private void startRaceIfAllDataReceived() {
        if (allXMLReceived() && raceView == null) {
            loadRaceView();
        }
    }

    private boolean allXMLReceived() {
        return courseData != null && allBoatsMap != null && regattaData != null;
    }

    /**
     * Updates the position of a boat. Boat and position are given in the provided data.
     */
    private void updatePosition(PositionUpdateData positionData) {
        if (positionData.getType() == DeviceType.YACHT_TYPE) {
            if (allXMLReceived() && allBoatsMap.containsKey(positionData.getDeviceId())) {
                Yacht yacht = allBoatsMap.get(positionData.getDeviceId());
                yacht.updateLocation(positionData.getLat(),
                    positionData.getLon(), positionData.getHeading(),
                    positionData.getGroundSpeed());
            }
        } else if (positionData.getType() == DeviceType.MARK_TYPE) {
            //CompoundMark mark = courseData.getCompoundMarks().get(positionData.getDeviceId());
        }
    }

    /**
     * Updates the boat as having passed the mark. Boat and mark are given by the ids in the
     * provided data.
     *
     * @param roundingData Contains data for the rounding of a mark.
     */
    private void updateMarkRounding(MarkRoundingData roundingData) {
        if (allXMLReceived()) {
            Yacht yacht = allBoatsMap.get(roundingData.getBoatId());
            yacht.setMarkRoundingTime(roundingData.getTimeStamp());
            yacht.updateTimeSinceLastMarkProperty(
                raceState.getRaceTime() - roundingData.getTimeStamp());
            yacht.setLastMarkRounded(
                courseData.getCompoundMarks().get(
                    roundingData.getMarkId()
                )
            );
        }
    }

    private void processRaceStatusUpdate(RaceStatusData data) {
        if (allXMLReceived()) {
            raceState.updateState(data);
            for (long[] boatData : data.getBoatData()) {
                Yacht yacht = allBoatsMap.get((int) boatData[0]);
                yacht.setEstimateTimeTillNextMark(raceState.getRaceTime() - boatData[1]);
                yacht.setEstimateTimeAtFinish(boatData[2]);
                int legNumber = (int) boatData[3];
                yacht.setLegNumber(legNumber);
                yacht.setBoatStatus((int) boatData[4]);
                if (legNumber != yacht.getLegNumber()) {
                    int placing = 1;
                    for (Yacht otherYacht : allBoatsMap.values()) {
                        if (otherYacht.getSourceId() != boatData[0] &&
                            yacht.getLegNumber() <= otherYacht.getLegNumber()) {
                            placing++;
                        }
                    }
                    yacht.setPositionInteger(placing);
                }
            }
        }
    }

    private void close() {
        socketThread.setSocketToClose();
    }


    /**
     * Handle the key-pressed event from the text field.
     *
     * @param e The key event triggering this call
     */
    public void keyPressed(KeyEvent e) {
        BoatActionMessage boatActionMessage;
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastSendingTime > KEY_STROKE_SENDING_FREQUENCY) {
            lastSendingTime = currentTime;
            switch (e.getCode()) {
                case SPACE: // align with vmg
                    boatActionMessage = new BoatActionMessage(BoatActionType.VMG);
                    socketThread.sendBoatActionMessage(boatActionMessage);
                    break;
                case PAGE_UP: // upwind
                    boatActionMessage = new BoatActionMessage(BoatActionType.UPWIND);
                    socketThread.sendBoatActionMessage(boatActionMessage);
                    break;
                case PAGE_DOWN: // downwind
                    boatActionMessage = new BoatActionMessage(BoatActionType.DOWNWIND);
                    socketThread.sendBoatActionMessage(boatActionMessage);
                    break;
                case ENTER: // tack/gybe
                    boatActionMessage = new BoatActionMessage(BoatActionType.TACK_GYBE);
                    socketThread.sendBoatActionMessage(boatActionMessage);
                    break;
                //TODO Allow a zoom in and zoom out methods
                case Z:  // zoom in
                    System.out.println("Zoom in");
                    break;
                case X:  // zoom out
                    System.out.println("Zoom out");
                    break;
            }
        }
    }

    public void keyReleased(KeyEvent e) {
        switch (e.getCode()) {
            //TODO 12/07/17 Determine the sail state and send the appropriate packet (eg. if sails are in, send a sail out packet)
            case SHIFT:  // sails in/sails out
                BoatActionMessage boatActionMessage = new BoatActionMessage(
                    BoatActionType.SAILS_IN);
                socketThread.sendBoatActionMessage(boatActionMessage);
                break;
        }
    }

    public RaceXMLData getCourseData() {
        return courseData;
    }

    /**
     * Tells race view to show a collision animation.
     */
    private void showCollisionAlert(YachtEventData yachtEventData) {
        // 33 is the agreed code to show collision
        if (yachtEventData.getEventId() == 33) {
            raceView.showCollision(yachtEventData.getSubjectId());
        }
    }
}
