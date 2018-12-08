package net.digihippo.cryptnet.roadmap;

final class LatLn
{
    final double lat, lon;

    LatLn(double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString()
    {
        return "(" + lon + ", " + lat + ")";
    }
}
