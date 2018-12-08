package net.digihippo.cryptnet.roadmap;

import net.digihippo.cryptnet.DoublePoint;

import java.util.ArrayList;
import java.util.List;

final class Way
{
    private final List<Node> nodes;

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
}
