package net.digihippo.cryptnet.api;

public interface GameClient {
    void onGameStarted();
    void onGameObject(GameObject gameObject);
    void onLocationChange(GameObjectIdentifier id, Location location);
    void onGameEnded(GameResult gameResult);
}
