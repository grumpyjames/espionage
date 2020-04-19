package net.digihippo.cryptnet.client;

import net.digihippo.cryptnet.model.Model.Events;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.HashMap;
import java.util.Map;

public class GameView implements Events
{
    public GameView(String identifier)
    {
        this.identifier = identifier;
    }

    @Override
    public void playerPositionChanged(LatLn location)
    {
        this.playerLocation = location;
    }

    @Override
    public void sentryPositionChanged(String patrolIdentifier, LatLn location, UnitVector orientation)
    {
        SentryView sentryView = sentries.get(patrolIdentifier);
        if (sentryView == null)
        {
            SentryView view = new SentryView(patrolIdentifier);
            sentries.put(patrolIdentifier, view);
            view.setLocation(location, orientation);
        }
        else
        {
            sentryView.setLocation(location, orientation);
        }
    }

    @Override
    public void gameOver()
    {
        this.state = State.GameOver;
    }

    @Override
    public void victory()
    {
        this.state = State.Victory;
    }

    @Override
    public void gameRejected(String message)
    {

    }

    @Override
    public void gameStarted()
    {
        this.state = State.Live;
    }

    public boolean isGameOver()
    {
        return state == State.GameOver;
    }

    public Iterable<? extends SentryView> sentries()
    {
        return sentries.values();
    }

    public LatLn playerLocation()
    {
        return playerLocation;
    }

    public static class SentryView
    {
        private final String identifier;
        public LatLn location;
        public LatLn orientation;

        private SentryView(String identifier)
        {
            this.identifier = identifier;
        }

        public void setLocation(LatLn location, LatLn orientation)
        {
            this.location = location;
            this.orientation = orientation;
        }
    }

    private final String identifier;
    private final Map<String, SentryView> sentries = new HashMap<>();
    private LatLn playerLocation;
    private State state = State.Idle;

    enum State
    {
        Idle,
        Live,
        GameOver,
        Victory
    }

}
