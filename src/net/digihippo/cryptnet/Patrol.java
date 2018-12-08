package net.digihippo.cryptnet;

import java.util.Map;
import java.util.Random;

final class Patrol
{
    Path path;
    Line line;
    DoublePoint delta;
    DoublePoint point;

    private Direction direction;
    private Intersection previous;
    private Point previousTurn;

    public Patrol(
        DoublePoint doublePoint, Path path, Line line, DoublePoint delta, Direction direction)
    {
        this.point = doublePoint;
        this.line = line;
        this.delta = delta;
        this.path = path;
        this.direction = direction;
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
                // as we move away from an intersection, we'll get 'spurious' collisions
                if (intersection.equals(previous))
                {
                    continue;
                }
                performTurn(random, pixel, intersection);
                break;
            }
            else if (this.path.startsAt(pixel) && this.direction == Direction.Backwards)
            {
                // FIXME: problems with very short lines here, I suspect...
                this.delta = this.line.direction();
                this.point = pixel.asDoublePoint();
                this.previous = null;
                this.previousTurn = null;
                this.direction = Direction.Forwards;
                System.out.printf(
                    "Start of line %s reached at %s, switching to direction %s\n", line, pixel, delta);
                break;
            }
            else if (this.path.endsAt(pixel) && this.direction == Direction.Forwards)
            {
                this.direction = Direction.Backwards;
                this.delta = this.direction.orient(this.line.direction());
                this.point = pixel.asDoublePoint();
                this.previous = null;
                this.previousTurn = null;
                System.out.printf(
                    "End of line %s reached at %s, switching to direction %s\n", line, pixel, delta);
                break;
            }
            else if (!pixel.equals(previousTurn) && this.path.turnsAt(pixel))
            {
                this.line = this.path.lineAfter(line, direction);
                this.delta = direction.orient(this.line.direction());
                this.point = pixel.asDoublePoint();
                this.previousTurn = pixel;
                System.out.printf(
                    "Turning point reached at %s, switching to %s, direction %s\n", pixel, line, delta);
                break;
            }
        }
    }

    private void performTurn(Random random, Point pixel, Intersection intersection)
    {
        System.out.printf("Performing turn at %s, intersection %s\n", pixel, intersection);
        previous = intersection;
        IntersectionEntry[] lines =
            intersection.entries.toArray(new IntersectionEntry[intersection.entries.size()]);
        IntersectionEntry entry =
            lines[random.nextInt(lines.length)];
        System.out.printf("Chose %s\n", entry);

        this.delta = entry.direction.orient(entry.line.direction());
        this.direction = entry.direction;
        this.path = entry.path;
        this.line = entry.line;
        this.point = pixel.asDoublePoint();
        this.previousTurn = null;
    }

    @Override
    public String toString()
    {
        return "Patrol{" +
            "path=" + path +
            ", line=" + line +
            ", delta=" + delta +
            ", point=" + point +
            ", direction=" + direction +
            ", previous=" + previous +
            '}';
    }
}
