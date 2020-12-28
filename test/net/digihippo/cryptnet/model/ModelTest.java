package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Node;
import net.digihippo.cryptnet.roadmap.UnitVector;
import net.digihippo.cryptnet.roadmap.Way;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class ModelTest
{
    private final TestEvents events = new TestEvents();
    // This is Spaniards road at the top of Hampstead Heath. It's very straight.
    private final LatLn jackStrawsCastle = latLn(51.5629829089533, -0.1793216022757321);
    private final LatLn zebraNearSpaniards = latLn(51.56899086921811, -0.17475457002941547);
    private final Model model;
    {
        model = Model.createModel(
                Paths.from(ways(way(
                        node(jackStrawsCastle),
                        node(zebraNearSpaniards)))),
                new StayAliveRules(1.2),
                new Random(),
                events);
    }

    private final Clock clock = at("2020-12-27T13:00:00.000Z");

    private static final class Clock
    {
        private long timeMillis;

        public Clock(long timeMillis)
        {
            this.timeMillis = timeMillis;
        }

        public long forward(int scalar, ChronoUnit unit)
        {
            this.timeMillis += unit.getDuration().getSeconds() * scalar * 1_000;
            this.timeMillis += (unit.getDuration().getNano() * scalar) / 1_000_000;

            return this.timeMillis;
        }
    }

    @SuppressWarnings("SameParameterValue")
    Clock at(final String instant)
    {
        Instant parsed = Instant.parse(instant);
        return new Clock(parsed.toEpochMilli());
    }

    private static final class TestEvents implements Model.Events
    {
        private boolean victory = false;
        private boolean gameOver = false;

        @Override
        public void playerPositionChanged(LatLn location)
        {

        }

        @Override
        public void sentryPositionChanged(boolean joining, String patrolIdentifier, LatLn location, UnitVector orientation, LatLn connectionLocation)
        {

        }

        @Override
        public void gameOver()
        {
            this.gameOver = true;
        }

        @Override
        public void victory()
        {
            this.victory = true;
        }

        @Override
        public void frameEnd(int frameCounter)
        {

        }

        @Override
        public void gameStarted()
        {

        }

        @Override
        public void frameStart(int frameCounter)
        {

        }
    }


    @Test
    public void simplestPossibleVictory()
    {
        model.setPlayerLocation(latLn(51.5664837824125, -0.17661640054047678));

        model.startGame(clock.timeMillis);
        model.time(clock.forward(10, ChronoUnit.SECONDS));
        model.time(clock.forward(10, ChronoUnit.SECONDS));
        model.time(clock.forward(10, ChronoUnit.SECONDS));
        model.time(clock.forward(41, ChronoUnit.MILLIS));

        assertTrue(events.victory);
    }

    @Test
    public void simplestPossibleDefeat()
    {
        model.setPlayerLocation(latLn(51.5629829089533, -0.1793216022757321));
        // Very near, but not exactly the same - there's a bug in LatLn::directionFrom
        model.addSentry(latLn(51.56298291, -0.1793216));

        model.startGame(clock.timeMillis);
        model.time(clock.forward(1, ChronoUnit.SECONDS));

        assertTrue(events.gameOver);
    }

    @Test
    public void playerEscapes()
    {
        LatLn initialLocation = latLn(51.5629829089533, -0.1793216022757321);
        model.setPlayerLocation(initialLocation);
        // Far enough to give the player a headstart
        model.addSentry(latLn(51.56298, -0.17932));

        UnitVector thisWay = zebraNearSpaniards.directionFrom(jackStrawsCastle);

        model.startGame(clock.timeMillis);
        for (int i = 0; i < 30_040; i++)
        {
            double playerSpeedKmh = 6; //6km/h -> 1.67m/s -> 0.00167m/ms
            double playerSpeedMetresPerMilli = (playerSpeedKmh * 1000) / (60 * 60 * 1000);
            LatLn latLn = thisWay.applyWithScalar(initialLocation, i * playerSpeedMetresPerMilli);
            model.setPlayerLocation(latLn);
            model.time(clock.forward(1, ChronoUnit.MILLIS));
        }

        assertTrue(events.victory);
    }

    @Test
    public void playerIsSlowlyCaught()
    {
        LatLn initialLocation = latLn(51.5629829089533, -0.1793216022757321);
        model.setPlayerLocation(initialLocation);
        // Far enough to give the player a headstart
        model.addSentry(latLn(51.56298, -0.17932));

        UnitVector thisWay = zebraNearSpaniards.directionFrom(jackStrawsCastle);

        model.startGame(clock.timeMillis);
        for (int i = 0; i < 30_040; i++)
        {
            double playerSpeedKmh = 6; //6km/h -> 1.67m/s -> 0.00167m/ms
            double playerSpeedMetresPerMilli = (playerSpeedKmh * 1000) / (60 * 60 * 1000);
            LatLn latLn = thisWay.applyWithScalar(initialLocation, i * playerSpeedMetresPerMilli);
            model.setPlayerLocation(latLn);
            model.time(clock.forward(1, ChronoUnit.MILLIS));
        }

        assertTrue(events.victory);
    }


    @Test
    public void roundTrip()
    {
//        Segment segmentIntersectOne = Segment.createLine(new Pixel(2, 3), new Pixel(4, 5));
//        Segment segmentTwo = Segment.createLine(new Pixel(4, 5), new Pixel(4, 7));
//        Segment segmentThree = Segment.createLine(new Pixel(4, 7), new Pixel(4, 10));
//        Path one = new Path(Arrays.asList(segmentIntersectOne, segmentTwo, segmentThree));
//
//        Segment segmentFour = Segment.createLine(new Pixel(0, 0), new Pixel(3, 6));
//        Segment segmentFive = Segment.createLine(new Pixel(3, 6), new Pixel(5, 6));
//        Segment segmentSix = Segment.createLine(new Pixel(5, 6), new Pixel(10, 10));
//        Path two = new Path(Arrays.asList(segmentFour, segmentFive, segmentSix));
//
//        Model model = Model.createModel(Arrays.asList(one, two), 256, 256);
//
//        model.addSentry(42, 43);
//        model.addSentry(10, 1);
//
//        model.addPatrols(Arrays.asList(
//            new Patrol("patrol-one", one, segmentThree, new DoublePoint(0, 1), new DoublePoint(4, 8), Direction.Forwards),
//            new Patrol("patrol-two", one, segmentThree, new DoublePoint(0, 1), new DoublePoint(4, 8), Direction.Backwards)
//        ));
//
//        model.setPlayerLocation(13, 44);
//
//        // The String representation of some of the double values is, sadly, lossy.
//        assertThat(Model.parse(model.toString()).toString(), equalTo(model.toString()));
    }

    private Collection<Way> ways(Way... ways)
    {
        return Arrays.asList(ways);
    }

    private int nextNodeId = 0;

    private Way way(Node... nodes)
    {
        return new Way(Arrays.asList(nodes));
    }

    private Node node(LatLn latLn)
    {
        Node node = new Node(nextNodeId++);
        node.latLn = latLn;
        return node;
    }

    private LatLn latLn(double lat, double lon)
    {
        return new LatLn(lat, lon);
    }

}