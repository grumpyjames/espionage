package net.digihippo.cryptnet.server;

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

}
