package net.digihippo.cryptnet.roadmap;

final class Node
{
    final long nodeId;
    LatLn latLn;

    public Node(long nodeId)
    {
        this.nodeId = nodeId;
    }

    @Override
    public String toString()
    {
        return latLn != null ? latLn.toString() : "()";
    }
}
