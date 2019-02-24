package net.digihippo.cryptnet.roadmap;

import net.digihippo.cryptnet.dimtwo.DoublePoint;
import net.digihippo.cryptnet.dimtwo.Line;
import net.digihippo.cryptnet.dimtwo.Path;
import net.digihippo.cryptnet.dimtwo.Pixel;

import java.util.ArrayList;
import java.util.List;

public final class NormalizedWay
{
    public final List<DoublePoint> doublePoints;

    NormalizedWay(List<DoublePoint> doublePoints)
    {
        this.doublePoints = doublePoints;
    }

    @Override
    public String toString()
    {
        return doublePoints.toString();
    }

    public Path toPath()
    {
        int lineCount = doublePoints.size() - 1;
        final List<Line> pieces = new ArrayList<>(lineCount);
        for (int i = 0; i < lineCount; i++)
        {
            Pixel start =
                doublePoints.get(i).round();
            Pixel end = doublePoints.get(i + 1).round();
            Line line = Line.createLine(start.x, end.x, start.y, end.y);
            if (!start.equals(end))
            {
                pieces.add(line);
            }
        }
        return new Path(pieces);
    }
}
