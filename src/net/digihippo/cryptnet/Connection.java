package net.digihippo.cryptnet;

class Connection
{
    final DoublePoint connectionPoint;
    final Line line;
    final double distance;
    final Path path;

    Connection(Point point, DoublePoint connectionPoint, Line line, Path path)
    {
        this.connectionPoint = connectionPoint;
        this.line = line;
        this.distance = DoublePoint.distanceBetween(point.asDoublePoint(), connectionPoint);
        this.path = path;
    }

    static Connection nearestConnection(Iterable<Path> paths, Point point)
    {
        double best = Double.MAX_VALUE;
        Connection result = null;
        for (Path path : paths)
        {
            for (Line line : path.lines)
            {
                Connection connection = line.connectionTo(path, point);
                if (connection.distance < best)
                {
                    result = connection;
                    best = connection.distance;
                }
            }
        }
        return result;
    }
}
