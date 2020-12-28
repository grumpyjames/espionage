package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.Model;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

final class Game
{
    private final String identifier;
    private final Model model;
    private long nextTickMillis;

    Game(
            String identifier,
            Model model,
            long lastTickMillis)
    {
        this.identifier = identifier;
        this.model = model;
        this.nextTickMillis = lastTickMillis + 40;
    }

    void tick(long currentTimeMillis)
    {
        while (nextTickMillis < currentTimeMillis)
        {
            model.tick();
            nextTickMillis += 40;
        }
    }

    void onPlayerLocation(LatLn location)
    {
        model.setPlayerLocation(location);
    }

    void onClick(LatLn location)
    {
        model.click(location);
    }

    private final class GameEvents implements Model.Events {
        private final Events events;

        GameEvents(Events events) {
            this.events = events;
        }

        @Override
        public void playerPositionChanged(LatLn location)
        {
            events.playerPositionChanged(identifier, location);
        }

        @Override
        public void sentryPositionChanged(String patrolIdentifier, LatLn location, UnitVector orientation)
        {
            events.sentryPositionChanged(identifier, patrolIdentifier, location, orientation);
        }

        @Override
        public void gameOver()
        {
            events.gameOver(identifier);
        }

        @Override
        public void victory()
        {
            events.victory(identifier);
        }

        @Override
        public void gameRejected(String message)
        {
            events.gameStarted(identifier);
        }

        @Override
        public void gameStarted()
        {
            events.gameStarted(identifier);
        }
    }
}
