package net.digihippo.cryptnet.roadmap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

public final class Way
{
    public final List<Node> nodes;

    public Way(List<Node> nodes)
    {
        this.nodes = nodes;
    }

    public static Way way(Node... nodes)
    {
        return new Way(Arrays.asList(nodes));
    }

    public static Collection<Way> ways(Way... ways)
    {
        return Arrays.asList(ways);
    }

    @Override
    public String toString()
    {
        return nodes.toString();
    }

//    NormalizedWay translate(double originX, double originY, int zoomLevel, double tileSize)
//    {
//        final List<DoublePoint> result = new ArrayList<>(nodes.size());
//        for (Node node : nodes)
//        {
//            double ourXPixel = WebMercator.x(node.latLn.lon, zoomLevel, tileSize);
//            double x = ourXPixel - originX;
//            double ourYPixel = WebMercator.y(node.latLn.lat, zoomLevel, tileSize);
//            double y = ourYPixel - originY;
//            result.add(new DoublePoint(x, y));
//        }
//
//        return new NormalizedWay(result);
//    }

    Way concat(long joiningNode, Way another)
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

    long lastNodeId()
    {
        return nodes.get(nodes.size() - 1).nodeId;
    }

    long firstNodeId()
    {
        return this.nodes.get(0).nodeId;
    }

    long oppositeEndTo(long nodeId)
    {
        if (firstNodeId() == nodeId)
        {
            return lastNodeId();
        }
        return firstNodeId();
    }

    public int nodeCount() {
        return nodes.size();
    }
}
