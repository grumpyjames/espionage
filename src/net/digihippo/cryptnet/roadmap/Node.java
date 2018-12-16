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
        return Long.toString(nodeId);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Node node = (Node) o;

        return nodeId == node.nodeId;
    }

    @Override
    public int hashCode()
    {
        return (int) (nodeId ^ (nodeId >>> 32));
    }
}
