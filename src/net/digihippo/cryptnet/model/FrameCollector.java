package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class FrameCollector implements Model.Events
{
    private final FrameConsumer consumer;

    public FrameCollector(FrameConsumer consumer)
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

        @Override
        public String toString()
        {
            return "PatrolView{" +
                    "location=" + location +
                    ", orientation=" + orientation +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            PatrolView that = (PatrolView) o;
            return Objects.equals(location, that.location) &&
                    Objects.equals(orientation, that.orientation);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(location, orientation);
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

        @Override
        public String toString()
        {
            return "JoiningView{" +
                    "location=" + location +
                    ", orientation=" + orientation +
                    ", connectionLocation=" + connectionLocation +
                    '}';
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            JoiningView that = (JoiningView) o;
            return Objects.equals(location, that.location) &&
                    Objects.equals(orientation, that.orientation) &&
                    Objects.equals(connectionLocation, that.connectionLocation);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(location, orientation, connectionLocation);
        }
    }

    public static final class Frame
    {
        public final int frameCounter;
        public boolean gameOver, victory;
        public LatLn playerLocation;
        public List<JoiningView> joining = new ArrayList<>();
        public List<PatrolView> patrols = new ArrayList<>();

        public Frame(int frameCounter)
        {
            this.frameCounter = frameCounter;
        }

        @Override
        public boolean equals(Object o)
        {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Frame frame = (Frame) o;
            return frameCounter == frame.frameCounter &&
                    gameOver == frame.gameOver &&
                    victory == frame.victory &&
                    Objects.equals(playerLocation, frame.playerLocation) &&
                    Objects.equals(joining, frame.joining) &&
                    Objects.equals(patrols, frame.patrols);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(frameCounter, gameOver, victory, playerLocation, joining, patrols);
        }

        @Override
        public String toString()
        {
            return "Frame{" +
                    "frameCounter=" + frameCounter +
                    ", gameOver=" + gameOver +
                    ", victory=" + victory +
                    ", playerLocation=" + playerLocation +
                    ", joining=" + joining +
                    ", patrols=" + patrols +
                    '}';
        }
    }

    private Frame frame;

    @Override
    public void gameStarted()
    {
        consumer.gameStarted();
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
        this.consumer.onFrame(frame);
    }
}
