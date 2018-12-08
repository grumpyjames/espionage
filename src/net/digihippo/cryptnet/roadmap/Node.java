package net.digihippo.cryptnet.roadmap;

final class Node
{
    LatLn latLn;

    @Override
    public String toString()
    {
        return latLn != null ? latLn.toString() : "()";
    }
}
