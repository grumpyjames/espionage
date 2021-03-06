package net.digihippo.cryptnet.roadmap;

public final class Node
{
    public final long nodeId;
    public LatLn latLn;

    public Node(long nodeId)
    {
        this.nodeId = nodeId;
    }

    public static Node node(int nodeId, LatLn latLn)
    {
        Node node = new Node(nodeId);
        node.latLn = latLn;
        return node;
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
