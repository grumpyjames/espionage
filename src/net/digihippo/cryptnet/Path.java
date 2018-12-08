package net.digihippo.cryptnet;

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
        return "Path{" +
            "lines=" + lines +
            '}';
    }
}
