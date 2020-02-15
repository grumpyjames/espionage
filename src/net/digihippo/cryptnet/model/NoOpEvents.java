package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public class NoOpEvents implements Model.Events
{
    @Override
    public void playerPositionChanged(LatLn location) {

    }

    @Override
    public void sentryPositionChanged(String patrolIdentifier, LatLn location, LatLn orientation) {

    }

    @Override
    public void gameOver()
    {

    }

    @Override
    public void victory()
    {

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
