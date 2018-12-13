package net.digihippo.cryptnet;

import java.util.ArrayList;
import java.util.List;

class IntersectionEntry
{
    final Path path;
    final Line line;
    final Direction direction;

    public IntersectionEntry(Path path, Line line, Direction direction)
    {
        this.path = path;
        this.line = line;
        this.direction = direction;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntersectionEntry that = (IntersectionEntry) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (line != null ? !line.equals(that.line) : that.line != null) return false;
        return direction == that.direction;

    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (line != null ? line.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return direction + " along " + path.highlighting(line);
    }

    public static IntersectionEntry parse(String string)
    {
        String[] parts = string.split(" along ");

        Direction direction = Direction.valueOf(parts[0]);

        String[] points = parts[1].split("->");
        final List<Line> lines = new ArrayList<>(points.length - 1);
        Line selected = null;
        for (int i = 0; i < points.length - 1; i++)
        {
            String pointOne = points[i];
            String pointTwo = points[i + 1];
            if (pointOne.endsWith("_"))
            {
                final Point p = Point.parse(pointOne.substring(0, pointOne.length() - 1));
                final Point q = Point.parse(pointTwo.substring(1));
                Line line = Line.createLine(p, q);
                lines.add(line);
                selected = line;
            }
            else
            {
                final Point p = Point.parse(pointOne.replace("_", ""));
                final Point q = Point.parse(pointTwo);
                Line line = Line.createLine(p, q);
                lines.add(line);
            }
        }

        return new IntersectionEntry(new Path(lines), selected, direction);
    }
}
