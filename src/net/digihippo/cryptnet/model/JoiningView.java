package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.Objects;

public final class JoiningView
{
    public final LatLn location;
    public final UnitVector orientation;
    public final LatLn connectionLocation;

    public JoiningView(LatLn location, UnitVector orientation, LatLn connectionLocation)
    {
        this.location = location;
        this.orientation = orientation;
        this.connectionLocation = connectionLocation;
    }

    @Override
    public String toString()
    {
        return "JoiningView{" +
                "location=" + location +
                ", orientation=" + orientation +
                ", connectionLocation=" + connectionLocation +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JoiningView that = (JoiningView) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(orientation, that.orientation) &&
                Objects.equals(connectionLocation, that.connectionLocation);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(location, orientation, connectionLocation);
    }
}
