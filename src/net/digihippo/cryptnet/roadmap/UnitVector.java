package net.digihippo.cryptnet.roadmap;

import java.util.Objects;

// This is almost certainly not a "unit" of distance globally but locally it's _close_.
// It models a change in lat/lon (both _radians_) that should result in a distance change of one metre.
public class UnitVector
{
    public final double dLat;
    public final double dLon;

    public UnitVector(double dLat, double dLon)
    {
        this.dLat = dLat;
        this.dLon = dLon;
    }

    public LatLn applyTo(LatLn location)
    {
        return new LatLn(location.lat + dLat, location.lon + dLon);
    }

    public LatLn applyWithScalar(LatLn location, double scalar)
    {
        return new LatLn(location.lat + (dLat * scalar), location.lon + (dLon * scalar));
    }

    public UnitVector reverse()
    {
        return new UnitVector(-dLat, -dLon);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UnitVector that = (UnitVector) o;
        return Double.compare(that.dLat, dLat) == 0 &&
                Double.compare(that.dLon, dLon) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(dLat, dLon);
    }

    @Override
    public String toString()
    {
        return "UnitVector{" +
                "dLat=" + dLat +
                ", dLon=" + dLon +
                '}';
    }
}
