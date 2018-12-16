package net.digihippo.cryptnet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Path implements HasLines
{
    final List<Line> lines;

    public Path(List<Line> lines)
    {
        this.lines = lines;
    }

    @Override
    public Iterable<Line> lines()
    {
        return lines;
    }

    public boolean startsAt(Point pixel)
    {
        return lines.get(0).startsAt(pixel);
    }

    public boolean endsAt(Point pixel)
    {
        return lines.get(lines.size() - 1).endsAt(pixel);
    }

    public boolean turnsAt(Point pixel)
    {
        for (int i = 0; i < lines.size() - 1; i++)
        {
            final Line line = lines.get(i);
            if (line.endsAt(pixel))
            {
                return true;
            }
        }
        return false;
    }

    public Line lineAfter(Line line, Direction direction)
    {
        if (direction == Direction.Forwards)
        {
            for (int i = 0; i < lines.size() - 1; i++)
            {
                final Line candidate = lines.get(i);
                if (candidate.equals(line))
                {
                    return lines.get(i + 1);
                }
            }
        }

        for (int i = lines.size() - 1; i > 0; i--)
        {
            final Line candidate = lines.get(i);
            if (candidate.equals(line))
            {
                return lines.get(i - 1);
            }
        }

        return line;
    }

    @Override
    public String toString()
    {
        return highlighting(null);
    }

    public String highlighting(Line toHighlight)
    {
        Line line = lines.get(0);
        String result = line.toString(toHighlight);

        for (int i = 1; i < lines.size(); i++)
        {
            line = lines.get(i);
            if (line.equals(toHighlight))
            {
                result += "_->_";
            }
            else
            {
                result += "->";
            }
            result += new Point(line.x2, line.y2);
        }

        return result;
    }

    public static Path parse(String path)
    {
        String[] points = path.split("->");
        final List<Line> segments = new ArrayList<>(points.length - 1);
        for (int i = 0; i < points.length - 1; i++)
        {
            String start = points[i];
            String end = points[i + 1];
            segments.add(Line.createLine(Point.parse(start), Point.parse(end)));
        }
        return new Path(segments);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Path path = (Path) o;

        return !(lines != null ? !lines.equals(path.lines) : path.lines != null);

    }

    @Override
    public int hashCode()
    {
        return lines != null ? lines.hashCode() : 0;
    }

    public double distanceTo(DoublePoint point)
    {
        Connection connection = Connection.nearestConnection(Collections.singletonList(this), point);
        return DoublePoint.distanceBetween(connection.connectionPoint, point);
    }

    public int indexOf(Line line)
    {
        return lines.indexOf(line);
    }
}
