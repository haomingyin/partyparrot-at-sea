package seng302.model.mark;

import java.util.ArrayList;
import java.util.List;
import seng302.model.GeoPoint;

/**
 * An abstract class to represent general marks
 * Created by Haoming Yin (hyi25) on 17/3/17.
 */
public class Mark extends GeoPoint {

    @FunctionalInterface
    public interface PositionListener {
        void notifyPositionChange(Mark mark, double lat, double lon);
    }

    private int seqID;
    private String name;
    private int sourceID;
    private List<PositionListener> positionListeners = new ArrayList<>();

    public Mark(String name, double lat, double lng, int sourceID) {
        super(lat, lng);
        this.name = name;
        this.sourceID = sourceID;
    }

    /**
     * Prints out mark's info and its geo location, good for testing
     * @return a string showing its details
     */
    @Override
    public String toString() {
        return String.format("Mark%d: %s, source: %d, lat: %f, lng: %f", seqID, name, sourceID, getLat(), getLng());
    }

    public int getSeqID() {
        return seqID;
    }

    public void setSeqID(int seqID) {
        this.seqID = seqID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSourceID() {
        return sourceID;
    }

    public void setSourceID(int sourceID) {
        this.sourceID = sourceID;
    }

    public void updatePosition (double lat, double lon) {
        this.setLat(lat);
        this.setLng(lon);
        for (PositionListener listener : positionListeners) {
            listener.notifyPositionChange(this, lat, lon);
        }
    }

    public void addPositionListener (PositionListener listener) {
        positionListeners.add(listener);
    }

    public void removePositionListener (PositionListener listener) {
        positionListeners.remove(listener);
    }
}


