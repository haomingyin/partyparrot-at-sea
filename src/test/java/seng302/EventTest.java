package seng302;

import org.junit.Test;
import seng302.models.Boat;
import seng302.models.Event;
import seng302.models.Mark;

import static org.junit.Assert.assertEquals;

/**
 * Test for Event class
 * Created by Haoming on 7/03/17.
 */
public class EventTest {

    @Test
    public void getTimeString() throws Exception {
        Boat boat = new Boat("testBoat");
        Event event = new Event(1231242.2, boat, new Mark("mark1"), new Mark("mark2"));
        assertEquals("20:31:242", event.getTimeString());
    }

    @Test
    public void testBoatHeading() throws Exception {
        Boat boat = new Boat("testBoat");
        Event event = new Event(1231242.2, boat, new Mark("mark1", 142.5, 122.1), new Mark("mark2", 121.9,99.2));

        assertEquals(event.getBoatHeading(), 221.9733862944651, 1e-15);
    }

    @Test
    public void testDistanceBetweenMarks() throws Exception {
        Boat boat = new Boat("testBoat");
        Event event = new Event(1231242.2, boat, new Mark("mark1", 142.5, 122.1), new Mark("mark2", 121.9,99.2));

        assertEquals(event.getDistanceBetweenMarks(), 30.80211031731429, 1e-15);
    }
}