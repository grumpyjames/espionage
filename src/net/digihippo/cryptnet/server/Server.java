package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.dimtwo.DoublePoint;
import net.digihippo.cryptnet.model.Model;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class Server
{
    public Server(
        Events events,
        Random random)
    {
        this.events = events;
        this.random = random;
    }

    private static final class Game
    {
        private final String identifier;
        private final Model model;
        private long nextTickMillis;

        private Game(
            String identifier,
            Model model,
            long lastTickMillis)
        {
            this.identifier = identifier;
            this.model = model;
            this.nextTickMillis = lastTickMillis + 40;
        }

        public void tick(
            long currentTimeMillis,
            Random random,
            final Events events)
        {
            while (nextTickMillis < currentTimeMillis)
            {
                model.tick(random, new Model.Events() {
                    @Override
                    public void playerPositionChanged(DoublePoint location)
                    {
                        events.playerPositionChanged(identifier, location);
                    }

                    @Override
                    public void sentryPositionChanged(
                        String patrolIdentifier, DoublePoint location, DoublePoint orientation)
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
                });

                nextTickMillis += 40;
            }
        }

        public void onPlayerLocation(int playerX, int playerY)
        {
            model.setPlayerLocation(playerX, playerY);
        }

        public void onClick(int x, int y)
        {
            model.click(x, y);
        }
    }

    private final Map<String, Game> games = new HashMap<>();
    private final Events events;
    private final Random random;

    public void startGame(
        final long currentTimeMillis,
        final String gameIdentifier,
        final Model model)
    {
        if (games.containsKey(gameIdentifier))
        {
            events.gameRejected(gameIdentifier, "Duplicate game id!");
        }
        else
        {
            games.put(gameIdentifier, new Game(gameIdentifier, model, currentTimeMillis));
            events.gameStarted(gameIdentifier);
        }
    }

    public void onPlayerLocation(String gameIdentifier, int playerX, int playerY)
    {
        games.get(gameIdentifier).onPlayerLocation(playerX, playerY);
    }

    public void onClick(String gameIdentifier, int x, int y)
    {
        games.get(gameIdentifier).onClick(x, y);
    }

    public void tick(
        final long currentTimeMillis)
    {
        for (Game game : games.values())
        {
            game.tick(currentTimeMillis, random, events);
        }
    }

    // Immutable values only, this is likely to become a thread hop.
    public interface Events
    {
        void gameRejected(
            String gameIdentifier,
            String message);

        void gameStarted(
            String gameIdentifier);

        void playerPositionChanged(
            String gameIdentifier,
            DoublePoint location);

        void sentryPositionChanged(
            String gameIdentifier,
            String patrolIdentifier,
            DoublePoint location,
            DoublePoint orientation);

        void gameOver(
            String gameIdentifier
        );

        void victory(
            String gameIdentifier
        );
    }

}
