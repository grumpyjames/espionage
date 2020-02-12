package net.digihippo.cryptnet.api;

import java.util.List;

public interface EspionageClient {
    void onConnected();
    void onNearbyGames(final List<GameInfo> games);
    void onJoin(final Game game);
}
