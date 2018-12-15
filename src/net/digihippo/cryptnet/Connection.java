package net.digihippo.cryptnet;

class Connection
{
    final DoublePoint connectionPoint;
    final Line line;
    final Path path;

    Connection(DoublePoint to, Line line, Path path)
    {
        this.connectionPoint = to;
        this.line = line;
        this.path = path;
    }

    @Override
    public String toString()
    {
        return path.highlighting(line) + "@" + connectionPoint.toString();
    }

    static Connection nearestConnection(Iterable<Path> paths, Point point)
    {
        double best = Double.MAX_VALUE;
        Connection result = null;
        DoublePoint from = point.asDoublePoint();
        for (Path path : paths)
        {
            for (Line line : path.lines)
            {
                Connection connection = line.connectionTo(path, point);
                double distance =
                    DoublePoint.distanceBetween(from, connection.connectionPoint);
                if (distance < best)
                {
                    result = connection;
                    best = distance;
                }
            }
        }
        return result;
    }

    public static Connection parse(String string)
    {
        String[] parts = string.split("@");
        HighlightedLine highlightedLine = HighlightedLine.parse(parts[0]);
        DoublePoint connectionPoint = DoublePoint.parse(parts[1]);

        return new Connection(connectionPoint, highlightedLine.line, highlightedLine.path);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Connection that = (Connection) o;

        if (connectionPoint != null ? !connectionPoint.equals(that.connectionPoint) : that.connectionPoint != null)
            return false;
        if (line != null ? !line.equals(that.line) : that.line != null) return false;
        return !(path != null ? !path.equals(that.path) : that.path != null);

    }

    @Override
    public int hashCode()
    {
        int result = connectionPoint != null ? connectionPoint.hashCode() : 0;
        result = 31 * result + (line != null ? line.hashCode() : 0);
        result = 31 * result + (path != null ? path.hashCode() : 0);
        return result;
    }
}
