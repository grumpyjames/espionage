package net.digihippo.cryptnet.roadmap;

import net.digihippo.cryptnet.DoublePoint;

import java.util.ArrayList;
import java.util.List;

final class Way
{
    final List<Node> nodes;

    Way(List<Node> nodes)
    {
        this.nodes = nodes;
    }

    @Override
    public String toString()
    {
        return nodes.toString();
    }

    public NormalizedWay translate(double originX, double originY, int zoomLevel)
    {
        final List<DoublePoint> result = new ArrayList<>(nodes.size());
        for (Node node : nodes)
        {
            double ourXPixel = OsmSource.x(node.latLn.lon, zoomLevel);
            double x = ourXPixel - originX;
            double ourYPixel = OsmSource.y(node.latLn.lat, zoomLevel);
            double y = ourYPixel - originY;
            result.add(new DoublePoint(x, y));
        }

        return new NormalizedWay(result);
    }

    public Way concat(long joiningNode, Way another)
    {
        final List<Node> nodes = new ArrayList<>(this.nodes.size() + another.nodes.size());
        if (
            joiningNode == this.lastNodeId() &&
            joiningNode == another.firstNodeId())
        {
            nodes.addAll(this.nodes);
            nodes.remove(nodes.size() - 1);
            nodes.addAll(another.nodes);
        }
        else if (
            joiningNode == this.firstNodeId() &&
            joiningNode == another.lastNodeId())
        {
            nodes.addAll(another.nodes);
            nodes.remove(nodes.size() - 1);
            nodes.addAll(this.nodes);
        }
        else if (
            joiningNode == this.lastNodeId() &&
            joiningNode == another.lastNodeId()
            )
        {
            nodes.addAll(this.nodes);
            for (int i = another.nodes.size() - 2; i >= 0; i--)
            {
                 nodes.add(another.nodes.get(i));
            }
        }
        else if (
            joiningNode == this.firstNodeId() &&
            joiningNode == another.firstNodeId()
            )
        {
            for (int i = this.nodes.size() - 1; i > 0; i--)
            {
                nodes.add(this.nodes.get(i));
            }
            nodes.addAll(another.nodes);
        }


        return new Way(nodes);
    }

    private long lastNodeId()
    {
        return nodes.get(nodes.size() - 1).nodeId;
    }

    private long firstNodeId()
    {
        return this.nodes.get(0).nodeId;
    }
}
