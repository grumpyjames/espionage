package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

public final class FrameCollector implements Model.Events
{
    private final FrameConsumer consumer;

    public FrameCollector(FrameConsumer consumer)
    {
        this.consumer = consumer;
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
