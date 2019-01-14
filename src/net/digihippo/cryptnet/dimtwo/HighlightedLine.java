package net.digihippo.cryptnet.dimtwo;

import java.util.ArrayList;
import java.util.List;

final class HighlightedLine
{
    final Line line;
    final Path path;

    private HighlightedLine(Line line, Path path)
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
                final Pixel p = Pixel.parse(pointOne.substring(0, pointOne.length() - 1));
                final Pixel q = Pixel.parse(pointTwo.substring(1));
                Line line = Line.createLine(p, q);
                lines.add(line);
                selected = line;
            }
            else
            {
                final Pixel p = Pixel.parse(pointOne.replace("_", ""));
                final Pixel q = Pixel.parse(pointTwo);
                Line line = Line.createLine(p, q);
                lines.add(line);
            }
        }

        Path path = new Path(lines);

        return new HighlightedLine(selected, path);
    }
}
