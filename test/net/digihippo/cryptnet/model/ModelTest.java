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
    private final Model model = Model.createModel(ways(way(
            node(51.5629829089533, -0.1793216022757321),
            node(51.56899086921811, -0.17475457002941547))),
            new Random(),
            events);
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
            return this.timeMillis;
        }
    }

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
        public void sentryPositionChanged(String patrolIdentifier, LatLn location, UnitVector orientation)
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
        public void gameRejected(String message)
        {

        }

        @Override
        public void gameStarted()
        {

        }
    }


    @Test
    public void simplestPossibleVictory()
    {
        long startTime = clock.timeMillis;

        model.setPlayerLocation(new LatLn(51.5664837824125, -0.17661640054047678));
        model.rules((timeMillis, playerLocation, sentryLocations) ->
        {
            long durationMillis = timeMillis - startTime;
            if (durationMillis >= 30_000)
            {
                return Rules.State.Victory;
            }

            return Rules.State.Continue;
        });

        model.startGame(clock.timeMillis);
        model.time(clock.forward(10, ChronoUnit.SECONDS));
        model.time(clock.forward(10, ChronoUnit.SECONDS));
        model.time(clock.forward(10, ChronoUnit.SECONDS));

        assertTrue(events.victory);
    }

    @Test
    public void simplestPossibleDefeat()
    {
        long startTime = clock.timeMillis;

        model.setPlayerLocation(new LatLn(51.5629829089533, -0.1793216022757321));
        // Very near, but not exactly the same - there's a bug in LatLn::directionFrom
        model.addSentry(new LatLn(51.562982, -0.179321));
        model.rules((timeMillis, playerLocation, sentryLocations) ->
        {
            long durationMillis = timeMillis - startTime;
            if (durationMillis >= 30_000)
            {
                return Rules.State.Victory;
            }
            for (LatLn sentryLocation: sentryLocations)
            {
                double v = sentryLocation.distanceTo(playerLocation);
                if (v <= 2)
                {
                    return Rules.State.GameOver;
                }
            }

            return Rules.State.Continue;
        });

        model.startGame(clock.timeMillis);
        model.time(clock.forward(1, ChronoUnit.SECONDS));

        assertTrue(events.gameOver);
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

    private Node node(double lat, double lon)
    {
        Node node = new Node(nextNodeId++);
        node.latLn = new LatLn(lat, lon);
        return node;
    }
}