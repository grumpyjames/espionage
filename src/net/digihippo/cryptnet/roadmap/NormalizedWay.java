package net.digihippo.cryptnet.roadmap;

import net.digihippo.cryptnet.DoublePoint;

import java.util.List;

public final class NormalizedWay
{
    public final List<DoublePoint> doublePoints;

    NormalizedWay(List<DoublePoint> doublePoints)
    {
        this.doublePoints = doublePoints;
    }

    @Override
    public String toString()
    {
        return doublePoints.toString();
    }
}
