package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.dimtwo.DoublePoint;

class NoOpEvents implements Model.Events
{
    @Override
    public void playerPositionChanged(DoublePoint location)
    {

    }

    @Override
    public void sentryPositionChanged(String patrolIdentifier, DoublePoint location, DoublePoint orientation)
    {

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
