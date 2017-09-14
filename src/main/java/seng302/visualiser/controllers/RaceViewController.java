package seng302.visualiser.controllers;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXDialog;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Point2D;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Data;
import javafx.scene.chart.XYChart.Series;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polyline;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import seng302.model.ClientYacht;
import seng302.model.RaceState;
import seng302.model.mark.CompoundMark;
import seng302.model.mark.Mark;
import seng302.model.stream.xml.parser.RaceXMLData;
import seng302.utilities.Sounds;
import seng302.visualiser.GameView3D;
import seng302.visualiser.controllers.annotations.ImportantAnnotationController;
import seng302.visualiser.controllers.annotations.ImportantAnnotationDelegate;
import seng302.visualiser.controllers.annotations.ImportantAnnotationsState;
import seng302.visualiser.controllers.dialogs.FinishDialogController;
import seng302.visualiser.fxObjects.ChatHistory;
import seng302.visualiser.fxObjects.assets_2D.WindArrow;
import seng302.visualiser.fxObjects.assets_3D.BoatObject;

/**
 * Controller class that manages the display of a race
 */
public class RaceViewController extends Thread implements ImportantAnnotationDelegate {

    private final int CHAT_LIMIT = 128;

    @FXML
    private Pane basePane;
    @FXML
    private JFXButton chatSend;
    @FXML
    private Pane chatHistoryHolder;
    @FXML
    private TextField chatInput;
    @FXML
    private LineChart<String, Double> raceSparkLine;
    @FXML
    private NumberAxis sparklineYAxis;
    @FXML
    private VBox positionVbox;
    @FXML
    private CheckBox toggleFps;
    @FXML
    private Label timerLabel;
    @FXML
    private StackPane contentAnchorPane;

    private GridPane contentGridPane;
    @FXML
    private AnchorPane rvAnchorPane;
    @FXML
    private AnchorPane windArrowHolder;
    @FXML
    private Slider annotationSlider;
    @FXML
    private Button selectAnnotationBtn;
    @FXML
    private ComboBox<ClientYacht> yachtSelectionComboBox;
    @FXML
    private Text fpsDisplay;
    @FXML
    private ImageView windImageView;
    @FXML
    private Label windDirectionLabel;
    @FXML
    private Label windSpeedLabel;
    @FXML
    private Label positionLabel, boatSpeedLabel, boatHeadingLabel;

    //Race Data
    private Map<Integer, ClientYacht> participants;
    private Map<Integer, CompoundMark> markers;
    private RaceXMLData courseData;
    private GameView3D gameView;
    private RaceState raceState;

    private ChatHistory chatHistory;

    private Timeline timerTimeline;
    private Timer timer = new Timer();
    private List<Series<String, Double>> sparkLineData = new ArrayList<>();
    private ImportantAnnotationsState importantAnnotations;
    private Polyline windArrow = new WindArrow(Color.LIGHTGRAY);
    private ObservableList<ClientYacht> selectionComboBoxList = FXCollections.observableArrayList();
    private ClientYacht player;
    private JFXDialog finishScreenDialog;
    private FinishDialogController finishDialogController;

    public void initialize() {
        Sounds.stopMusic();
        Sounds.playRaceMusic();

        finishScreenDialog = createFinishDialog();

        // Load a default important annotation state
        //importantAnnotations = new ImportantAnnotationsState();

        //Formatting the y axis of the sparkline
//        raceSparkLine.getYAxis().setRotate(180);
//        raceSparkLine.getYAxis().setTickLabelRotation(180);
//        raceSparkLine.getYAxis().setTranslateX(-5);
        //raceSparkLine.visibleProperty().setValue(false);
        //raceSparkLine.getYAxis().setAutoRanging(false);
        //sparklineYAxis.setTickMarkVisible(false);

        //positionVbox.getStylesheets().add(getClass().getResource("/css/master.css").toString());
//        raceSparkLine.visibleProperty().setValue(false);
//        raceSparkLine.getYAxis().setAutoRanging(false);
//        sparklineYAxis.setTickMarkVisible(false);
//        positionVbox.getStylesheets().add(getClass().getResource("/css/master.css").toString());

        //selectAnnotationBtn.setOnAction(event -> loadSelectAnnotationView());
//        rvAnchorPane.prefWidthProperty().bind(ViewManager.getInstance().getDecorator().widthProperty());
//        rvAnchorPane.prefHeightProperty().bind(ViewManager.getInstance().getDecorator().heightProperty());
//        selectAnnotationBtn.setOnAction(event -> loadSelectAnnotationView());
//        windArrowHolder.getChildren().addAll(windArrow);
//        windArrow.setLayoutX(windArrowHolder.getWidth() / 2);
//        windArrow.setLayoutY(windArrowHolder.getHeight() / 2);

//        selectAnnotationBtn.setOnAction(event -> loadSelectAnnotationView());
        chatInput.lengthProperty().addListener((obs, oldLen, newLen) -> {
            if (newLen.intValue() > CHAT_LIMIT) {
                chatInput.setText(chatInput.getText().substring(0, CHAT_LIMIT));
            }
        });
        chatHistory = new ChatHistory();
        chatHistoryHolder.getChildren().addAll(chatHistory);
        chatHistory.prefWidthProperty().bind(
            chatHistoryHolder.widthProperty()
        );
        chatHistory.prefHeightProperty().bind(
            chatHistoryHolder.heightProperty()
        );
//        chatHistory.setFitToWidth(true);
//        chatHistory.setFitToHeight(true);
//        chatHistory.textProperty().addListener((obs, oldValue, newValue) -> {
//            chatHistory.setScrollTop(Double.MAX_VALUE);
//        });
        rvAnchorPane.setOnMouseClicked((event) ->
                rvAnchorPane.requestFocus()
        );

        //Makes the chat history non transparent when clicked on
        chatInput.focusedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue,
                Boolean newValue) {
                if (newValue) {
                    chatHistory.increaseOpacity();
                } else {
                    chatHistory.decreaseOpacity();
                }
            }
        });
    }

    public void showFinishDialog(ArrayList<ClientYacht> finishedBoats) {
        raceState.setRaceStarted(false);
        finishDialogController.setFinishedBoats(finishedBoats);
        finishScreenDialog.show();
    }

    private JFXDialog createFinishDialog() {
        FXMLLoader dialog = new FXMLLoader(
            getClass().getResource("/views/dialogs/RaceFinishDialog.fxml"));

        JFXDialog finishScreenDialog = null;

        try {
            finishScreenDialog = new JFXDialog(contentAnchorPane, dialog.load(),
                JFXDialog.DialogTransition.CENTER);
        } catch (IOException e) {
            e.printStackTrace();
        }

        finishDialogController = dialog.getController();

        return finishScreenDialog;
    }

    public void loadRace (
        Map<Integer, ClientYacht> participants, RaceXMLData raceData, RaceState raceState,
        ClientYacht player) {

        this.participants = participants;
        this.courseData = raceData;
        this.markers = raceData.getCompoundMarks();
        this.raceState = raceState;
        this.player = player;

        raceState.getPlayerPositions().addListener((ListChangeListener<ClientYacht>) c -> {
            while (c.next()) {
                if (c.wasPermutated()) {
                    updateOrder(raceState.getPlayerPositions());
                    updateSparkLine();
                }
            }
        });

        updateOrder(raceState.getPlayerPositions());
        gameView = new GameView3D();
//        gameView.setFrameRateFXText(fpsDisplay);
        Platform.runLater(() -> {
            contentAnchorPane.getChildren().add(0, gameView.getAssets());
            ((SubScene) gameView.getAssets()).widthProperty()
                .bind(ViewManager.getInstance().getStage().widthProperty());
            ((SubScene) gameView.getAssets()).heightProperty()
                .bind(ViewManager.getInstance().getStage().heightProperty());
        });
        gameView.setBoats(new ArrayList<>(participants.values()));
        gameView.updateBorder(raceData.getCourseLimit());
        gameView.updateTokens(raceData.getTokens());
        gameView.updateCourse(
            new ArrayList<>(raceData.getCompoundMarks().values()), raceData.getMarkSequence()
        );
//        gameView.enableZoom();
        gameView.setBoatAsPlayer(player);
//        gameView.startRace();

//        raceState.addCollisionListener(gameView::drawCollision);
        raceState.windDirectionProperty().addListener((obs, oldDirection, newDirection) -> {
            gameView.setWindDir(newDirection.doubleValue());
            Platform.runLater(() -> updateWindDirection(newDirection.doubleValue()));
        });
        raceState.windSpeedProperty().addListener((obs, oldSpeed, newSpeed) ->
            Platform.runLater(() -> updateWindSpeed(newSpeed.doubleValue()))
        );
        Platform.runLater(() -> {
            updateWindDirection(raceState.windDirectionProperty().doubleValue());
            updateWindSpeed(raceState.getWindSpeed());
        });
        gameView.setWindDir(raceState.windDirectionProperty().doubleValue());
        Platform.runLater(() -> {
            initializeUpdateTimer();
        });
    }

    /**
     * The important annotations have been changed, update this view
     *
     * @param importantAnnotationsState The current state of the selected annotations
     */
    public void importantAnnotationsChanged(ImportantAnnotationsState importantAnnotationsState) {
        this.importantAnnotations = importantAnnotationsState;
        setAnnotations((int) annotationSlider.getValue()); // Refresh the displayed annotations
    }


    /**
     * Loads the "select annotations" view in a new window
     */
    private void loadSelectAnnotationView() {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader();
            Stage stage = new Stage();
            // Set controller
            ImportantAnnotationController controller = new ImportantAnnotationController(
                this, stage
            );
            fxmlLoader.setController(controller);
            // Load FXML and set CSS
            fxmlLoader.setLocation(
                getClass().getResource("/views/importantAnnotationSelectView.fxml")
            );
            Scene scene = new Scene(fxmlLoader.load(), 469, 298);
            scene.getStylesheets().add(getClass().getResource("/css/master.css").toString());
            stage.initStyle(StageStyle.UNDECORATED);
            stage.setScene(scene);
            stage.show();
            controller.loadState(importantAnnotations);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initialiseFPSCheckBox() {
//        toggleFps.selectedProperty().addListener((obs, oldVal, newVal) ->
//            gameView.setFPSVisibility(toggleFps.isSelected())
//        );
    }

    private void initialiseAnnotationSlider() {
//        annotationSlider.setLabelFormatter(new StringConverter<Double>() {
//            @Override
//            public String toString(Double n) {
//                if (n == 0) {
//                    return "None";
//                }
//                if (n == 1) {
//                    return "Important";
//                }
//                if (n == 2) {
//                    return "All";
//                }
//                return "All";
//            }
//
//            @Override
//            public Double fromString(String s) {
//                switch (s) {
//                    case "None":
//                        return 0d;
//                    case "Important":
//                        return 1d;
//                    case "All":
//                        return 2d;
//
//                    default:
//                        return 2d;
//                }
//            }
//        });
//        annotationSlider.setValue(2);
//        annotationSlider.valueProperty().addListener((obs, oldVal, newVal) ->
//            setAnnotations((int) annotationSlider.getValue())
//        );
    }


    /**
     * Used to add any new yachts into the race that may have started late or not have had data received yet
     */
    private void updateSparkLine(){
//        // TODO: 2/08/17 there is about 0 chance of this working. Once we are keeping track of boat positions it can be fixed.
//        // Collect the racing yachts that aren't already in the chart
//        sparkLineData.clear();
//        List<ClientYacht> sparkLineCandidates = new ArrayList<>(participants.values());
//        // Create a new data series for new yachts
//        sparkLineCandidates
//            .stream()
//            .filter(yacht -> yacht.getPosition() != null)
//            .forEach(yacht -> {
//                Series<String, Double> yachtData = new Series<>();
//                yachtData.setName(yacht.getSourceId().toString());
//                yachtData.getData().add(
//                    new Data<>(
//                        Integer.toString(yacht.getLegNumber()),
//                        1.0 + participants.size() - yacht.getPosition()
//                    )
//                );
//            sparkLineData.add(yachtData);
//            });
//
//        // Lambda function to sort the series in order of leg (later legs shown more to the right)
//        sparkLineData.sort((o1, o2) -> {
//            Integer leg1 =  Integer.parseInt(o1.getData().get(o1.getData().size()-1).getXValue());
//            Integer leg2 =  Integer.parseInt(o2.getData().get(o2.getData().size()-1).getXValue());
//            if (leg2 < leg1){
//                return 1;
//            } else {
//                return -1;
//            }
//        });
//
//        // Adds the new data series to the sparkline (and set the colour of the series)
//        Platform.runLater(() -> {
//            sparkLineData
//                .stream()
//                .filter(spark -> !raceSparkLine.getData().contains(spark))
//                .forEach(spark -> {
//                    raceSparkLine.getData().add(spark);
//                    spark.getNode().lookup(".chart-series-line").setStyle("-fx-stroke:" + getBoatColorAsRGB(spark.getName()));
//                });
//        });
    }

    private void initialiseSparkLine() {
//        sparklineYAxis.setUpperBound(participants.size() + 1);
//        raceSparkLine.setCreateSymbols(false);
    }

    /**
     * Updates the yachts sparkline of the desired yacht and using the new leg number
     * @param yacht The yacht to be updated on the sparkline
     * @param legNumber the leg number that the position will be assigned to
     */
    void updateYachtPositionSparkline(ClientYacht yacht, Integer legNumber){
        for (XYChart.Series<String, Double> positionData : sparkLineData) {
            positionData.getData().add(
                new Data<>(
                    Integer.toString(legNumber),
                    1.0 + participants.size() - yacht.getPlacing()
                )
            );
        }
    }


    /**
     * gets the rgb string of the yachts colour to use for the chart via css
     * @param yachtId id of yacht passed in to get the yachts colour
     * @return the colour as an rgb string
     */
    private String getBoatColorAsRGB(String yachtId){
        Color color = participants.get(Integer.valueOf(yachtId)).getColour();
        if (color == null){
            return String.format("#%02X%02X%02X",255,255,255);
        }
        return String.format( "#%02X%02X%02X",
            (int)( color.getRed() * 255 ),
            (int)( color.getGreen() * 255 ),
            (int)( color.getBlue() * 255 )
        );
    }


    /**
     * Initialises a timer which updates elements of the RaceView such as wind direction, yacht
     * orderings etc.. which are dependent on the info from the stream parser constantly.
     * Updates of each of these attributes are called ONCE EACH SECOND
     */
    private void initializeUpdateTimer() {
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                Platform.runLater(() -> updatePosition());
                Platform.runLater(() -> updateBoatSpeed());
                Platform.runLater(() -> updateBoatHeading());
                Platform.runLater(() -> updateRaceTime());
            }
        }, 0, 1000);
    }

    /**
     * Iterates over all corners until ones SeqID matches with the yachts current leg number.
     * Then it gets the compoundMarkID of that corner and uses it to fetch the appropriate mark
     * Returns null if no next mark found.
     * @param bg The BoatGroup to find the next mark of
     * @return The next Mark or null if none found
     */
    private Mark getNextMark(BoatObject bg) {
        // TODO: 1/08/17 Move to GameView
//
//        Integer legNumber = bg.getClientYacht().getLegNumber();
//        List<Corner> markSequence = courseData.getMarkSequence();
//
//        if (legNumber == 0) {
//            return null;
//        } else if (legNumber == markSequence.size() - 1) {
//            return null;
//        }
//
//        for (Corner corner : markSequence) {
//            if (legNumber + 2 == corner.getSeqID()) {
//                return courseData.getCompoundMarks().get(corner.getCompoundMarkID());
//            }
//        }
//        return null;
        return null;
    }


    /**
     * Updates the wind direction arrow and text as from info from the StreamParser
     * @param direction the from north angle of the wind.
     */
    private void updateWindDirection(double direction) {
        windDirectionLabel.setText(String.format("%.1f°", direction));
        windImageView.setRotate(direction);
    }

    /**
     * Updates the speed of the wind as displayed by the info pane.
     * @param windSpeed Windspeed in knots.
     */
    private void updateWindSpeed(double windSpeed) {
        windSpeedLabel.setText(String.format("%.1f", windSpeed) + " Knots");
    }


    /**
     * Updates the clock for the race
     */
    private void updateRaceTime() {
        if (raceState.getTimeTillStart() <= 0L && !raceState.isRaceStarted()) {
            timerLabel.setText("Race Finished!");
        } else {
            timerLabel.setText(raceState.getRaceTimeStr());
        }
    }

    /**
     * Updates player position with ordinal number up to 23rd position.
     */
    private void updatePosition() {
        if (player.getPosition() == null) {
            positionLabel.setText("Position:\n-");
        } else {
            switch (player.getPosition()) {
                case 1:
                    positionLabel.setText("Position:\n1st");
                    break;
                case 2:
                    positionLabel.setText("Position:\n2nd");
                    break;
                case 3:
                    positionLabel.setText("Position:\n3rd");
                    break;
                case 21:
                    positionLabel.setText("Position:\n21st");
                    break;
                case 22:
                    positionLabel.setText("Position:\n22nd");
                    break;
                case 23:
                    positionLabel.setText("Position:\n23rd");
                    break;
                default:
                    positionLabel.setText("Position:\n" + player.getPosition() + "th");
            }
        }
    }

    /**
     * Updates boat speed value displayed on race view.
     */
    private void updateBoatSpeed() {
        boatSpeedLabel.setText("Boat Speed:\n" + String.valueOf(player.getCurrentVelocity()));
    }

    /**
     * Updates boat heading value displayed on race view.
     */
    private void updateBoatHeading() {
        boatHeadingLabel.setText(String.format("Boat Heading:\n%.1f°", player.getHeading()));
    }

    /**
     * Updates the order of the yachts as from the StreamParser and sets them in the yacht order
     * section
     */
    private void updateOrder(ObservableList<ClientYacht> yachts) {
//        List<Text> vboxEntries = new ArrayList<>();
//
//        for (int i = 0; i < yachts.size(); i++) {
////            System.out.println("yacht == null  " + String.valueOf(yacht == null));
//            if (yachts.get(i).getBoatStatus() == BoatStatus.FINISHED
//                .getCode()) {  // 3 is finish status
//                Text textToAdd = new Text(i + 1 + ". " +
//                    yachts.get(i).getShortName() + " (Finished)");
//                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
//                vboxEntries.add(textToAdd);
//
//            } else {
//                Text textToAdd = new Text(i + 1 + ". " +
//                    yachts.get(i).getShortName() + " ");
//                textToAdd.setFill(Paint.valueOf("#d3d3d3"));
//                textToAdd.setStyle("");
//                vboxEntries.add(textToAdd);
//            }
//        }
//        Platform.runLater(() ->
//            positionVbox.getChildren().setAll(vboxEntries)
//        );
    }


    private void updateLaylines(BoatObject bg) {
        // TODO: 1/08/17 move to GameView
//
//        Mark nextMark = getNextMark(bg);
//        Boolean isUpwind = null;
//        // Can only calc leg direction if there is a next mark and it is a gate mark
//        if (nextMark != null) {
//            if (nextMark instanceof GateMark) {
//                if (bg.isUpwindLeg(gameViewController, nextMark)) {
//                    isUpwind = true;
//                } else {
//                    isUpwind = false;
//                }
//
//                for(MarkObject mg : gameViewController.getMarkGroups()) {
//
//                    mg.removeLaylines();
//
//                    if (mg.getMainMark().getId() == nextMark.getId()) {
//
//                        SingleMark singleMark1 = ((GateMark) nextMark).getSingleMark1();
//                        SingleMark singleMark2 = ((GateMark) nextMark).getSingleMark2();
//                        Point2D markPoint1 = gameViewController
//                            .findScaledXY(singleMark1.getLatitude(), singleMark1.getLongitude());
//                        Point2D markPoint2 = gameViewController
//                            .findScaledXY(singleMark2.getLatitude(), singleMark2.getLongitude());
//                        HashMap<Double, Double> angleAndSpeed;
//                        if (isUpwind) {
//                            angleAndSpeed = PolarTable.getOptimalUpwindVMG(StreamParser.getWindSpeed());
//                        } else {
//                            angleAndSpeed = PolarTable.getOptimalDownwindVMG(StreamParser.getWindSpeed());
//                        }
//
//                        Double resultingAngle = angleAndSpeed.keySet().iterator().next();
//
//
//                        Point2D yachtCurrentPos = new Point2D(bg.getBoatLayoutX(), bg.getBoatLayoutY());
//                        Point2D gateMidPoint = markPoint1.midpoint(markPoint2);
//                        Integer lineFuncResult = GeoUtility.lineFunction(yachtCurrentPos, gateMidPoint, markPoint2);
//                        Line rightLayline = new Line();
//                        Line leftLayline = new Line();
//                        if (lineFuncResult == 1) {
//                            rightLayline = makeRightLayline(markPoint2, 180 - resultingAngle, StreamParser.getWindDirection());
//                            leftLayline = makeLeftLayline(markPoint1, 180 - resultingAngle, StreamParser.getWindDirection());
//                        } else if (lineFuncResult == -1) {
//                            rightLayline = makeRightLayline(markPoint1, 180 - resultingAngle, StreamParser.getWindDirection());
//                            leftLayline = makeLeftLayline(markPoint2, 180 - resultingAngle, StreamParser.getWindDirection());
//                        }
//
//                        leftLayline.setStrokeWidth(0.5);
//                        leftLayline.setStroke(bg.getBoat().getColour());
//
//                        rightLayline.setStrokeWidth(0.5);
//                        rightLayline.setStroke(bg.getBoat().getColour());
//
//                        bg.setLaylines(leftLayline, rightLayline);
//                        mg.addLaylines(leftLayline, rightLayline);
//
//                    }
//                }
//            }
//        }
    }


    private Point2D getPointRotation(Point2D ref, Double distance, Double angle) {
        Double newX = ref.getX() + (ref.getX() + distance - ref.getX()) * Math.cos(angle)
            - (ref.getY() + distance - ref.getY()) * Math.sin(angle);
        Double newY = ref.getY() + (ref.getX() + distance - ref.getX()) * Math.sin(angle)
            + (ref.getY() + distance - ref.getY()) * Math.cos(angle);

        return new Point2D(newX, newY);
    }


    public Line  makeLeftLayline(Point2D startPoint, Double layLineAngle, Double baseAngle) {
        Point2D ep = getPointRotation(startPoint, 50.0, baseAngle + layLineAngle);
        Line line = new Line(startPoint.getX(), startPoint.getY(), ep.getX(), ep.getY());
        return line;

    }


    public Line makeRightLayline(Point2D startPoint, Double layLineAngle, Double baseAngle) {

        Point2D ep = getPointRotation(startPoint, 50.0, baseAngle - layLineAngle);
        Line line = new Line(startPoint.getX(), startPoint.getY(), ep.getX(), ep.getY());
        return line;

    }


    /**
     * Initialised the combo box with any yachts currently in the race and adds the required listener
     * for the combobox to take action upon selection
     */
    private void initialiseBoatSelectionComboBox() {
//        yachtSelectionComboBox.setItems(
//            FXCollections.observableArrayList(participants.values())
//        );
//        //Null check is if the listener is fired but nothing selected
//        yachtSelectionComboBox.valueProperty().addListener((obs, lastSelection, selectedBoat) -> {
//            if (selectedBoat != null) {
//                gameView.selectBoat(selectedBoat);
//            }
//        });

        //TODO uncomment out
//        selectionComboBoxList.setAll(participants.values());
//        yachtSelectionComboBox.setItems(selectionComboBoxList);
//        yachtSelectionComboBox.valueProperty().addListener((obs, lastSelection, selectedBoat) -> {
//            if (selectedBoat != null) {
//                gameView.selectBoat(selectedBoat);
//            }
//        });
    }

    /**
     * Display the list of yachts in the order they finished the race
     */
    private void loadRaceResultView() {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/FinishView.fxml"));

        try {
            contentGridPane.getChildren().removeAll();
            contentGridPane.getChildren().clear();
            contentGridPane.getChildren().addAll((Pane) loader.load());

        } catch (javafx.fxml.LoadException e) {
            System.err.println(e.getCause().toString());
        } catch (IOException e) {
            System.err.println(e.toString());
        }
    }

    private String getMillisToFormattedTime(long milliseconds) {
        return String.format("%02d:%02d:%02d",
            TimeUnit.MILLISECONDS.toHours(milliseconds),
            TimeUnit.MILLISECONDS.toMinutes(milliseconds) % 60, //Modulus 60 minutes per hour
            TimeUnit.MILLISECONDS.toSeconds(milliseconds) % 60  //Modulus 60 seconds per minute
        );
    }

    private void setAnnotations(Integer annotationLevel) {
//        switch (annotationLevel) {
//            // No Annotations
//            case 0:
//                gameView.setAnnotationVisibilities(
//                    false, false, false, false, false, false
//                );
//                break;
//            // Important Annotations
//            case 1:
//                gameView.setAnnotationVisibilities(
//                    importantAnnotations.getAnnotationState(Annotation.NAME),
//                    importantAnnotations.getAnnotationState(Annotation.SPEED),
//                    importantAnnotations.getAnnotationState(Annotation.ESTTIMETONEXTMARK),
//                    importantAnnotations.getAnnotationState(Annotation.LEGTIME),
//                    importantAnnotations.getAnnotationState(Annotation.TRACK),
//                    importantAnnotations.getAnnotationState(Annotation.WAKE)
//                );
//                break;
//            // All Annotations
//            case 2:
//                gameView.setAnnotationVisibilities(
//                    true, true, true, true, true, true
//                );
//                break;
//        }
    }


    /**
     * Sets all the annotations of the selected yacht to be visible and all others to be hidden
     *
     * @param yacht The yacht for which we want to view all annotations
     */
    private void setSelectedBoat(ClientYacht yacht) {
//        for (BoatObject bg : gameViewController.getBoatGroups()) {
//            //We need to iterate over all race groups to get the matching yacht group belonging to this yacht if we
//            //are to toggle its annotations, there is no other backwards knowledge of a yacht to its yachtgroup.
//            if (bg.getBoat().getHullID().equals(yacht.getHullID())) {
////                updateLaylines(bg);
//                bg.setIsSelected(true);
////                selectedBoat = yacht;
//            } else {
//                bg.setIsSelected(false);
//            }
//        }
    }

    public void updateTokens(RaceXMLData raceData) {
        gameView.updateTokens(raceData.getTokens());
    }

    public ReadOnlyBooleanProperty getSendPressedProperty() {
        return chatSend.pressedProperty();
    }

    public boolean isChatInputFocused() {
        return chatInput.focusedProperty().getValue();
    }

    public String readChatInput() {
        String chat = chatInput.getText();
        chatInput.clear();
        rvAnchorPane.requestFocus();
        return chat;
    }

    public void updateChatHistory(Paint playerColour, String newMessage) {
        Platform.runLater(() -> chatHistory.addMessage(playerColour, newMessage));
    }

}