package net.digihippo.cryptnet.dimtwo;

public class Connection<T extends Connection.HasPath>
{
    public final DoublePoint connectionPoint;
    public final Line line;
    public final T context;

    public Connection(DoublePoint to, Line line, T context)
    {
        this.connectionPoint = to;
        this.line = line;
        this.context = context;
    }

    @Override
    public String toString()
    {
        return context.getPath().highlighting(line) + "@" + connectionPoint.toString();
    }

    public Path getPath()
    {
        return context.getPath();
    }

    interface HasPath
    {
        Path getPath();
    }

    public static <T extends HasPath> Connection<T> nearestConnection(Iterable<T> paths, DoublePoint point)
    {
        double best = Double.MAX_VALUE;
        Connection<T> result = null;
        for (T hasPath : paths)
        {
            Path path = hasPath.getPath();
            for (Line line : path.lines)
            {
                Connection<T> connection = line.connectionTo(hasPath, point);
                double distance =
                    DoublePoint.distanceBetween(point, connection.connectionPoint);
                if (distance < best)
                {
                    result = connection;
                    best = distance;
                }
            }
        }
        return result;
    }

    public static Connection<Path> parse(String string)
    {
        String[] parts = string.split("@");
        HighlightedLine highlightedLine = HighlightedLine.parse(parts[0]);
        DoublePoint connectionPoint = DoublePoint.parse(parts[1]);

        return new Connection<>(connectionPoint, highlightedLine.line, highlightedLine.path);
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
        return !(context != null ? !context.equals(that.context) : that.context != null);

    }

    @Override
    public int hashCode()
    {
        int result = connectionPoint != null ? connectionPoint.hashCode() : 0;
        result = 31 * result + (line != null ? line.hashCode() : 0);
        result = 31 * result + (context != null ? context.hashCode() : 0);
        return result;
    }
}
