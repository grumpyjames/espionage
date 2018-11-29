package net.digihippo.cryptnet;

import java.util.List;

final class Line implements LineIntersection
{
    public final int x1, x2, y1, y2;
    public final double gradient, intersect;

    private Line(int x1, int x2, int y1, int y2, double gradient, double intersect)
    {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;

        this.gradient = gradient;
        this.intersect = intersect;
    }

    static Line createLine(int x1, int x2, int y1, int y2)
    {
        double gradient = ((double) y2 - y1) / ((double) x2 - x1);
        return new Line(x1, x2, y1, y2, gradient, ((double) y1) - (gradient * x1));
    }

    public Connection connectionTo(Point point)
    {
        if (((gradient * point.x) + intersect) == point.y)
        {
            return new Connection(point, point);
        }

        // otherwise: shortest distance is length of line perpendicular to this one joining us to point.
        final double inverseGradient = -1 / gradient;
        final double inverseIntersection = (double) point.y - (inverseGradient * point.x);

        // now we need the intersection with that line and us...
        Point intersection =
            Experiment.intersection(this.intersect, inverseIntersection, this.gradient, inverseGradient);

        return new Connection(point, intersection);
    }

    @Override
    public String toString()
    {
        return "y = " + gradient + "x + " + intersect;
    }

    public LineIntersection intersectionWith(Line other)
    {
        // FIXME: could simplify be enforcing all infinity gradient lines be positive
        if (vertical() && other.vertical())
        {
            if (this.x1 == other.x1)
            {
                // same line;
                // FIXME: return overlap of y co-ordinates
                return this;
            }
            else
            {
                return Empty.INSTANCE;
            }
        }

        if (this.gradient == other.gradient)
        {
            if (this.intersect == other.intersect)
            {
                // FIXME: Want the overlapping subset
                return this;
            }
            return Empty.INSTANCE;
        }

        if (vertical())
        {
            return new Point(this.x1, other.computeY(this.x1));
        }
        else if (other.vertical())
        {
            return new Point(other.x1, computeY(other.x1));
        }

        final double x = (this.intersect - other.intersect) / (other.gradient - this.gradient);
        final int y = computeY(x);
        return new Point((int) Math.round(x), y);
    }

    private boolean vertical()
    {
        return Double.isInfinite(this.gradient);
    }

    private int computeY(double x)
    {
        return (int) Math.round((this.gradient * x) + this.intersect);
    }

    @Override
    public void visit(List<Point> results)
    {
        // Well, we're not going to send it infinite points, are we.
    }
}
