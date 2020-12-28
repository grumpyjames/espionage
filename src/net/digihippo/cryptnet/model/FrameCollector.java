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

    public static final class PatrolView
    {
        public final LatLn location;
        public final UnitVector orientation;

        public PatrolView(LatLn location, UnitVector orientation)
        {
            this.location = location;
            this.orientation = orientation;
        }
    }

    public static final class JoiningView
    {
        public final LatLn location;
        public final UnitVector orientation;
        public final LatLn connectionLocation;

        public JoiningView(LatLn location, UnitVector orientation, LatLn connectionLocation)
        {
            this.location = location;
            this.orientation = orientation;
            this.connectionLocation = connectionLocation;
        }
    }

    public static final class Frame
    {
        public final int frameCounter;
        public boolean gameOver, victory;
        public LatLn playerLocation;
        public List<JoiningView> joining = new ArrayList<>();
        public List<PatrolView> patrols = new ArrayList<>();

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
    public void patrolPositionChanged(
            String patrolIdentifier,
            LatLn location,
            UnitVector orientation)
    {
        frame.patrols.add(new PatrolView(location, orientation));
    }

    @Override
    public void joiningPatrolPositionChanged(
            String identifier,
            LatLn movedTo,
            UnitVector direction,
            LatLn joiningLocation)
    {
        frame.joining.add(new JoiningView(movedTo, direction, joiningLocation));
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
