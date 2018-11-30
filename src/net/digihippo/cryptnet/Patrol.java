package net.digihippo.cryptnet;

import java.util.Map;
import java.util.Random;

final class Patrol
{
    Line line;
    DoublePoint delta;
    DoublePoint point;

    private Intersection previous;

    public Patrol(Point point, Line line, DoublePoint delta)
    {
        this.point = point.asDoublePoint();
        this.delta = delta;
        this.line = line;
    }

    public void tick(
        final Map<Point, Intersection> intersections,
        final Random random)
    {
        this.point = this.point.plus(delta);

        final Iterable<Point> pixels = this.point.pixelBounds();
        for (Point pixel : pixels)
        {
            Intersection intersection = intersections.get(pixel);
            if (intersection != null)
            {
                if (intersection.equals(previous))
                {
                    continue;
                }
                previous = intersection;
                IntersectionEntry[] lines =
                    intersection.entries.toArray(new IntersectionEntry[intersection.entries.size()]);
                IntersectionEntry entry =
                    lines[random.nextInt(lines.length)];
                if (entry.startsHere && entry.endsHere)
                {
                    // wtf is this, a point road?
                    throw new UnsupportedOperationException();
                }
                else if (entry.startsHere)
                {
                    this.delta = entry.line.direction();
                }
                else if (entry.endsHere)
                {
                    this.delta = entry.line.direction().flip();
                }
                else
                {
                    if (random.nextBoolean())
                    {
                        this.delta = entry.line.direction();
                    }
                    else
                    {
                        this.delta = entry.line.direction().flip();
                    }
                }

                this.line = entry.line;
                this.point = pixel.asDoublePoint();
                break;
            }
            else
            {
                // FIXME: problems with very short lines here, I suspect...
                if (this.line.startsAt(pixel) && !this.delta.equals(this.line.direction()))
                {
                    this.delta = this.line.direction();
                    this.point = pixel.asDoublePoint();
                    break;
                }
                else if (this.line.endsAt(pixel) && !this.delta.equals(this.line.direction().flip()))
                {
                    this.delta = this.line.direction().flip();
                    this.point = pixel.asDoublePoint();
                    break;
                }
            }
        }
    }
}
