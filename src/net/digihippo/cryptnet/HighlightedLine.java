package net.digihippo.cryptnet;

import java.util.ArrayList;
import java.util.List;

final class HighlightedLine
{
    final Line line;
    final Path path;

    HighlightedLine(Line line, Path path)
    {
        this.line = line;
        this.path = path;
    }

    public static HighlightedLine parse(String string)
    {
        String[] points = string.split("->");
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

        Path path = new Path(lines);

        return new HighlightedLine(selected, path);
    }
}
