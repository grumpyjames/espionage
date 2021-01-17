package net.digihippo.cryptnet.model;

public interface GameEvents
{
    void rules(StayAliveRules rules);
    void path(Path path);
    void gameReady(String gameId);
    void gameStarted();
    void onFrame(Frame frame);
}
