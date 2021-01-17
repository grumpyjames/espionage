package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.util.Objects;

public final class PatrolView
{
    public final LatLn location;
    public final UnitVector orientation;

    public PatrolView(LatLn location, UnitVector orientation)
    {
        this.location = location;
        this.orientation = orientation;
    }

    @Override
    public String toString()
    {
        return "PatrolView{" +
                "location=" + location +
                ", orientation=" + orientation +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PatrolView that = (PatrolView) o;
        return Objects.equals(location, that.location) &&
                Objects.equals(orientation, that.orientation);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(location, orientation);
    }
}
