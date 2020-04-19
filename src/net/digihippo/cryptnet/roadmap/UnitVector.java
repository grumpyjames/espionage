package net.digihippo.cryptnet.roadmap;

// This is almost certainly not a "unit" of distance globally but locally it's _close_.
// It models a change in lat/lon (both _radians_) that should result in a distance change of one metre.
public class UnitVector
{
    private final double dLat;
    private final double dLon;

    UnitVector(double dLat, double dLon)
    {
        this.dLat = dLat;
        this.dLon = dLon;
    }

    public LatLn applyTo(LatLn location)
    {
        return new LatLn(location.lat + dLat, location.lon + dLon);
    }

    public UnitVector reverse()
    {
        return new UnitVector(-dLat, -dLon);
    }
}
