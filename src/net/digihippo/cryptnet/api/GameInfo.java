package net.digihippo.cryptnet.api;

class GameInfo
{
    public final Geobox bounds;
    public final Rules rules;
    public final GameIdentifier gameIdentifier;

    GameInfo(
            Geobox bounds,
            Rules rules,
            GameIdentifier gameIdentifier)
    {
        this.bounds = bounds;
        this.rules = rules;
        this.gameIdentifier = gameIdentifier;
    }
}
