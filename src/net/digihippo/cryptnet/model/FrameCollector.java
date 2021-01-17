package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

public final class FrameCollector
{
    private final GameEvents consumer;

    public FrameCollector(GameEvents consumer)
    {
        this.consumer = consumer;
    }

    private Frame frame;

    public void rules(StayAliveRules rules)
    {
        consumer.rules(rules);
    }

    public void path(Path path)
    {
        consumer.path(path);
    }

    public void gameReady(String gameId)
    {
        consumer.gameReady(gameId);
    }

    public void gameStarted()
    {
        consumer.gameStarted();
    }

    public void frameStart(int frameCounter)
    {
        frame = new Frame(frameCounter);
    }

    public void playerPositionChanged(LatLn location)
    {
        frame.playerLocation = location;
    }

    public void patrolPositionChanged(
            String patrolIdentifier,
            LatLn location,
            UnitVector orientation)
    {
        frame.patrols.add(new PatrolView(location, orientation));
    }

    public void joiningPatrolPositionChanged(
            String identifier,
            LatLn movedTo,
            UnitVector direction,
            LatLn joiningLocation)
    {
        frame.joining.add(new JoiningView(movedTo, direction, joiningLocation));
    }

    public void gameOver()
    {
        frame.gameOver = true;
    }

    public void victory()
    {
        frame.victory = true;
    }

    public void frameEnd(int frameCounter)
    {
        this.consumer.onFrame(frame);
    }
}
