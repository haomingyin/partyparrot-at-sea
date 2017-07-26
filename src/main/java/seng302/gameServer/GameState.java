package seng302.gameServer;

import java.util.*;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import seng302.model.Player;
import seng302.model.Yacht;
import seng302.model.stream.packets.StreamPacket;
import seng302.server.messages.BoatActionType;

/**
 * A Static class to hold information about the current state of the game (model)
 * Created by wmu16 on 10/07/17.
 */
public class GameState {

    private static Long previousUpdateTime;
    public static Double windDirection;
    private static Double windSpeed;

    private static String hostIpAddress;
    private static List<Player> players;
    private static Map<Integer, Yacht> yachts;
    private static Boolean isRaceStarted;
    private static GameStages currentStage;

    // TODO: 26/07/17 cir27 - Super hackish fix until something more permanent can be made.
    private static ObservableList<String> observablePlayers = FXCollections.observableArrayList();
    private static Map<Player, String> playerStringMap = new HashMap<>();
    /*
        Ideally I would like to make this class an object instantiated by the server and given to
        it's created threads if necessary. Outside of that I think the dependencies on it
        (atm only Yacht & GameClient) can be removed from most other classes. The observable list of
        players could be pulled directly from the server by the GameClient since it instantiates it
        and it is reasonable for it to pull data. The current setup of publicly available statics is
        pretty meh IMO because anything can change it making it unreliable and like people did with
        the old ServerParser class everything that needs shared just gets thrown in the static
        collections and things become a real mess.
     */

    public GameState(String hostIpAddress) {
        windDirection = 170d;
        windSpeed = 10000d;
        yachts = new HashMap<>();
        players = new ArrayList<>();
        GameState.hostIpAddress = hostIpAddress;
        players = new ArrayList<>();
        currentStage = GameStages.LOBBYING;
        isRaceStarted = false;
        yachts = new HashMap<>();
        //set this when game stage changes to prerace
        previousUpdateTime = System.currentTimeMillis();
        yachts = new HashMap<>();
    }

    public static String getHostIpAddress() {
        return hostIpAddress;
    }

    public static List<Player> getPlayers() {
        return players;
    }

    public static ObservableList<String> getObservablePlayers () {
        return observablePlayers;
    }

    public static void addPlayer(Player player) {
        players.add(player);
        String playerText = player.getYacht().getSourceId() + " " + player.getYacht().getBoatName() + " " + player.getYacht().getCountry();
        observablePlayers.add(playerText);
        playerStringMap.put(player, playerText);
    }
    
    public static void removePlayer(Player player) {
        players.remove(player);
        observablePlayers.remove(playerStringMap.get(player));
        playerStringMap.remove(player);
    }

    public static void addYacht(Integer sourceId, Yacht yacht) {
        yachts.put(sourceId, yacht);
    }

    public static void removeYacht(Integer yachtId) {
        yachts.remove(yachtId);
    }

    public static Boolean getIsRaceStarted() {
        return isRaceStarted;
    }

    public static GameStages getCurrentStage() {
        return currentStage;
    }

    public static void setCurrentStage(GameStages currentStage) {
        GameState.currentStage = currentStage;
    }

    public static Double getWindDirection() {
        return windDirection;
    }

    public static Double getWindSpeedMMS() {
        return windSpeed;
    }

    public static Double getWindSpeedKnots() {
        return windSpeed / 1000 * 1.943844492; // TODO: 26/07/17 cir27 - remove magic numbers
    }

    public static Map<Integer, Yacht> getYachts() {
        return yachts;
    }

    public static void updateBoat(Integer sourceId, BoatActionType actionType) {
        Yacht playerYacht = yachts.get(sourceId);
//        System.out.println("-----------------------");
        switch (actionType) {
            case VMG:
                playerYacht.turnToVMG();
//                System.out.println("Snapping to VMG");
                // TODO: 22/07/17 wmu16 - Add in the vmg calculation code here
                break;
            case SAILS_IN:
                playerYacht.toggleSailIn();
//                System.out.println("Toggling Sails");
                break;
            case SAILS_OUT:
                playerYacht.toggleSailIn();
//                System.out.println("Toggling Sails");
                break;
            case TACK_GYBE:
                playerYacht.tackGybe(windDirection);
//                System.out.println("Tack/Gybe");
                break;
            case UPWIND:
                playerYacht.turnUpwind();
//                System.out.println("Moving upwind");
                break;
            case DOWNWIND:
                playerYacht.turnDownwind();
//                System.out.println("Moving downwind");
                break;
        }

        System.out.println("-----------------------");
        System.out.println("Sails are in: " + playerYacht.getSailIn());
        System.out.println("Heading: " + playerYacht.getHeading());
        System.out.println("Velocity: " + playerYacht.getVelocityMMS() / 1000);
        System.out.println("Lat: " + playerYacht.getLocation().getLat());
        System.out.println("Lng: " + playerYacht.getLocation().getLng());
        System.out.println("-----------------------\n");
    }

    public static void update() {

        Long timeInterval = System.currentTimeMillis() - previousUpdateTime;
        previousUpdateTime = System.currentTimeMillis();
        for (Yacht yacht : yachts.values()) {
            yacht.update(timeInterval);
        }
    }


    /**
     * Generates a new ID based off the size of current players + 1
     * @return a playerID to be allocated to a new connetion
     */
    public static Integer getUniquePlayerID() {
        // TODO: 22/07/17 wmu16 - This may not be robust enough and may have to be improved on.
        return yachts.size() + 1;
    }
}
