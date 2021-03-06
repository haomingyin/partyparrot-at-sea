package seng302.gameServer.server;

import java.io.IOException;
import org.junit.Assert;
import org.junit.Test;
import seng302.gameServer.GameState;
import seng302.gameServer.MainServerThread;
import seng302.gameServer.messages.BoatStatus;
import seng302.model.stream.packets.StreamPacket;
import seng302.model.stream.parser.RaceStatusData;
import seng302.utilities.StreamParser;
import seng302.visualiser.ClientToServerThread;

/**
 * Created by cir27 on 3/09/17.
 */
public class ChatCommandsTest {


//    private boolean dcSent = false;
//    private ClientToServerThread client;
//    private ClientToServerThread host;
//    private MainServerThread mst;
//
//    @Test
//    public void sendFinishAsHost () {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        try {
//            dcSent = false;
//            new GameState();
//            mst = new MainServerThread();
//            host = new ClientToServerThread("localhost", 4942);
//            host.addStreamObserver(() -> {
//                while (host.getPacketQueue().peek() != null) {
//                    StreamPacket packet = host.getPacketQueue().poll();
//                    switch (packet.getType()) {
//                        case RACE_STATUS:
//                            RaceStatusData rsd = StreamParser.extractRaceStatus(packet);
//                            if (rsd.getBoatData().get(0)[4] == BoatStatus.FINISHED.getCode()) {
//                                mst.terminate();
//                                Assert.assertTrue(dcSent);
//                            }
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            });
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ie) {
//                ie.printStackTrace();
//            }
//            mst.startGame();
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ie) {
//                ie.printStackTrace();
//            }
//            host.sendChatterMessage("[time_prefix] <name_prefix> /finish");
//            dcSent = true;
//            try {
//                Thread.sleep(2000);
//            } catch (InterruptedException ie) {
//                ie.printStackTrace();
//            }
//            mst.terminate();
//            host = null;
//            client = null;
//            mst = null;
//
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//    }
//
//    @Test
//    public void sendSpeedAsHostValid () {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        new GameState();
//        mst = new MainServerThread();
//        host = null;
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        try {
//            host = new ClientToServerThread("localhost", 4942);
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        mst.startGame();
//        host.sendChatterMessage("[time_prefix] <name_prefix> /speed 5");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        Assert.assertEquals(5.0, GameState.getServerSpeedMultiplier(), 0.00001);
//        mst.terminate();
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        host = null;
//        client = null;
//        mst = null;
//    }
//
//    @Test
//    public void sendSpeedAsHostInvalid () {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        new GameState();
//        mst = new MainServerThread();
//        host = null;
//        try {
//            host = new ClientToServerThread("localhost", 4942);
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        mst.startGame();
//        host.sendChatterMessage("[time_prefix] <name_prefix> /speed fdgdgdfg");
//        try {
//            Thread.sleep(100);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        mst.terminate();
//        Assert.assertEquals(1.0, GameState.getServerSpeedMultiplier(), 0.00001);
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//    }
//
//    @Test
//    public void sendCommandAsClient () {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        mst = new MainServerThread();
//        try {
//            host = new ClientToServerThread("localhost", 4942);
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ie) {
//                ie.printStackTrace();
//            }
//            client = new ClientToServerThread("localhost", 4942);
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        mst.startGame();
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        client.sendChatterMessage("[time_prefix] <name_prefix> /speed 5.0");
//        try {
//            Thread.sleep(200);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        Assert.assertEquals(1.0, GameState.getServerSpeedMultiplier(), 0.00001);
//        mst.terminate();
//        host.setSocketToClose();
//        client.setSocketToClose();
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//    }
//
//    @Test
//    public void receiveFinishedAsClient () {
//        try {
//            Thread.sleep(1000);
//        } catch (InterruptedException ie) {
//            ie.printStackTrace();
//        }
//        new GameState();
//        dcSent = false;
//        mst = new MainServerThread();
//        host = null;
//        try {
//            host = new ClientToServerThread("localhost", 4942);
//            try {
//                Thread.sleep(100);
//            } catch (InterruptedException ie) {
//                ie.printStackTrace();
//            }
//            client = new ClientToServerThread("localhost", 4942);
//            try {
//                Thread.sleep(1000);
//            } catch (InterruptedException ie) {
//                ie.printStackTrace();
//            }
//            client.addStreamObserver(() -> {
//                while (client.getPacketQueue().peek() != null) {
//                    StreamPacket packet = client.getPacketQueue().poll();
//                    switch (packet.getType()) {
//                        case RACE_STATUS:
//                            RaceStatusData rsd = StreamParser.extractRaceStatus(packet);
//                            if (rsd.getBoatData().get(0)[4] == BoatStatus.FINISHED.getCode()) {
//                                mst.terminate();
//                                Assert.assertTrue(dcSent);
//                            }
//                            break;
//                        default:
//                            break;
//                    }
//                }
//            });
//        } catch (IOException ioe) {
//            ioe.printStackTrace();
//        }
//        host.sendChatterMessage("[time_prefix] <name_prefix> /finish");
//        dcSent = true;
//    }
}
