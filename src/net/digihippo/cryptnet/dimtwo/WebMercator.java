package net.digihippo.cryptnet.dimtwo;

public final class WebMercator
{
    public static double y(double latRads, int zoomLevel, double tileSize)
    {
        return multiplier(zoomLevel, tileSize) * (Math.PI - Math.log(Math.tan((Math.PI / 4) + (latRads / 2))));
    }

    public static double lat(double y, int zoomLevel)
    {
        return 2 * (Math.atan(Math.exp(Math.PI - (y / multiplier(zoomLevel, 256D)))) - (Math.PI / 4));
    }

    public static double x(double lonRads, int zoomLevel, double tileSize)
    {
        return multiplier(zoomLevel, tileSize) * (lonRads + Math.PI);
    }

    public static double lon(double x, int zoomLevel)
    {
        return (x / multiplier(zoomLevel, 256D)) - Math.PI;
    }

    private static double multiplier(int zoomLevel, double tileSize)
    {
        return (tileSize / (2 * Math.PI)) * Math.pow(2, zoomLevel);
    }

    private WebMercator() {}
}
