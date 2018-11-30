package net.digihippo.cryptnet;

import java.util.Map;
import java.util.Random;

final class Sentry
{
    enum SentryState
    {
        Joining,
        Patrolling
    }

    final Connection connection;

    Line line;
    SentryState sentryState;
    DoublePoint delta;
    DoublePoint point;

    private Intersection previous;

    public Sentry(Point point, Connection connection)
    {
        this.point = point.asDoublePoint();
        this.delta = connection.connectionPoint.asDoublePoint().minus(this.point).over(50);
        this.connection = connection;
        this.line = connection.line;
        sentryState = SentryState.Joining;
    }

    public void tick(
        final Map<Point, Intersection> intersections,
        final Random random)
    {
        this.point = this.point.plus(delta);

        final Iterable<Point> pixels = this.point.pixelBounds();
        for (Point pixel : pixels)
        {
            // FIXME: We may have joined at an end.
            if (sentryState == SentryState.Joining && pixel.isEqualTo(this.connection.connectionPoint))
            {
                this.delta = this.line.direction();
                this.sentryState = SentryState.Patrolling;
                this.point = pixel.asDoublePoint();
                break;
            }
            else if (sentryState == SentryState.Patrolling)
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
                    // problems with very short lines here...
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
}
