package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Node;
import net.digihippo.cryptnet.roadmap.UnitVector;
import net.digihippo.cryptnet.roadmap.Way;
import org.junit.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Random;

import static org.junit.Assert.assertTrue;

public class ModelTest
{
    private final TestEvents events = new TestEvents();
    // This is Spaniards road at the top of Hampstead Heath. It's very straight.
    private final LatLn jackStrawsCastle = LatLn.toRads(51.5629829089533, -0.1793216022757321);
    private final LatLn zebraNearSpaniards = LatLn.toRads(51.56899086921811, -0.17475457002941547);
    private Model model;

    private Model createModel(int sentryCount, double initialSentryDistance, double sentrySpeed, int gameDurationMillis)
    {
        return Model.createModel(
                Paths.from(Way.ways(Way.way(
                        node(jackStrawsCastle),
                        node(zebraNearSpaniards)))),
                new StayAliveRules(sentryCount, initialSentryDistance, sentrySpeed, gameDurationMillis),
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

    private static final class TestEvents implements GameEvents
    {
        private boolean victory = false;
        private boolean gameOver = false;

        @Override
        public void rules(StayAliveRules rules)
        {

        }

        @Override
        public void path(Path path)
        {

        }

        @Override
        public void gameReady(String gameId)
        {

        }

        @Override
        public void gameStarted()
        {

        }

        @Override
        public void onFrame(Frame frame)
        {
            this.gameOver = frame.gameOver;
            this.victory = frame.victory;
        }
    }


    @Test
    public void simplestPossibleVictory()
    {
        model = createModel(0, 1, 1.2, 30_000);
        model.setPlayerLocation(LatLn.toRads(51.5664837824125, -0.17661640054047678));

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
        model = createModel(1, 0.1, 1.2, 30_000);
        model.setPlayerLocation(LatLn.toRads(51.5629829089533, -0.1793216022757321));

        model.startGame(clock.timeMillis);
        model.time(clock.forward(1, ChronoUnit.SECONDS));

        assertTrue(events.gameOver);
    }

    @Test
    public void playerEscapes()
    {
        model = createModel(1, 10, 0.8, 30_000);

        LatLn initialLocation = LatLn.toRads(51.5629829089533, -0.1793216022757321);
        model.setPlayerLocation(initialLocation);

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
        double sentrySpeedPerSecond = 1.8;
        int initialSentryDistance = 10;
        int gameDuration = 76_000;
        model = createModel(1, initialSentryDistance, sentrySpeedPerSecond, gameDuration);

        LatLn initialLocation = LatLn.toRads(51.5629829089533, -0.1793216022757321);
        model.setPlayerLocation(initialLocation);

        UnitVector thisWay = zebraNearSpaniards.directionFrom(jackStrawsCastle);

        model.startGame(clock.timeMillis);
        double playerSpeedMetresPerSecond = 6d * 1000d / (60d * 60d); //6km/h -> 1.67m/s -> 0.00167m/ms
        double playerSpeedMetresPerTick = playerSpeedMetresPerSecond / 25d;
        double sentrySpeedMetresPerTick = sentrySpeedPerSecond / 25d;
        double gainedPerTick = sentrySpeedMetresPerTick - playerSpeedMetresPerTick;

        double requiredTicks = Math.ceil(initialSentryDistance / gainedPerTick);
        if (requiredTicks * 40 > gameDuration)
        {
            throw new IllegalArgumentException("Game duration must be at least " + (requiredTicks * 40) + "ms");
        }

        for (int i = 0; i < requiredTicks; i++)
        {
            LatLn latLn = thisWay.applyWithScalar(initialLocation, i * playerSpeedMetresPerTick);
            model.setPlayerLocation(latLn);
            model.time(clock.forward(40, ChronoUnit.MILLIS));
        }

        assertTrue(events.gameOver);
    }

    private int nextNodeId = 0;

    private Node node(LatLn latLn)
    {
        return Node.node(nextNodeId++, latLn);
    }
}