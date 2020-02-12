package net.digihippo.cryptnet.api;

public interface Game
{
    void onLocation(double lat, double lng);
    void onClick(double lat, double lng);
    void quit();
}
