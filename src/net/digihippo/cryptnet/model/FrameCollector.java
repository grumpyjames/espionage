package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public final class FrameCollector implements Model.Events
{
    private final Consumer<Frame> consumer;

    public FrameCollector(Consumer<Frame> consumer)
    {
        this.consumer = consumer;
    }

    public static final class SentryView
    {
        public final boolean joining;
        public final LatLn location;
        public final UnitVector orientation;
        public final LatLn connection;

        public SentryView(boolean joining, LatLn location, UnitVector orientation, LatLn connection)
        {
            this.joining = joining;
            this.location = location;
            this.orientation = orientation;
            this.connection = connection;
        }
    }

    public static final class Frame
    {
        public final int frameCounter;
        public boolean gameOver, victory;
        public LatLn playerLocation;
        public List<SentryView> sentries = new ArrayList<>();

        private Frame(int frameCounter)
        {
            this.frameCounter = frameCounter;
        }
    }

    private Frame frame;

    @Override
    public void gameStarted()
    {
        // Good.
    }

    @Override
    public void frameStart(int frameCounter)
    {
        frame = new Frame(frameCounter);
    }

    @Override
    public void playerPositionChanged(LatLn location)
    {
        frame.playerLocation = location;
    }

    @Override
    public void sentryPositionChanged(
            boolean joining,
            String patrolIdentifier,
            LatLn location,
            UnitVector orientation,
            LatLn latLn)
    {
        frame.sentries.add(new SentryView(joining, location, orientation, latLn));
    }

    @Override
    public void gameOver()
    {
        frame.gameOver = true;
    }

    @Override
    public void victory()
    {
        frame.victory = true;
    }

    @Override
    public void frameEnd(int frameCounter)
    {
        this.consumer.accept(frame);
    }
}
