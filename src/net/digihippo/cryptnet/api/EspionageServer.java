package net.digihippo.cryptnet.api;

public interface EspionageServer
{
    void onLocation(Location location);
    void nearbyGames();
    void joinGame(GameIdentifier gameId, GameClient gameClient);
}
