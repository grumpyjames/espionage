package net.digihippo.cryptnet;

import java.util.List;

class Connection
{
    final Point connectionPoint;
    final Line line;
    final double distance;

    Connection(Point point, Point connectionPoint, Line line)
    {
        this.connectionPoint = connectionPoint;
        this.line = line;
        this.distance = Point.distanceBetween(point, connectionPoint);
    }

    static Connection nearestConnection(List<Line> lines, Point point)
    {
        double best = Double.MAX_VALUE;
        Connection result = null;
        for (Line line : lines)
        {
            Connection connection = line.connectionTo(point);
            if (connection.distance < best)
            {
                result = connection;
                best = connection.distance;
            }
        }
        return result;
    }
}
