package net.digihippo.cryptnet;

final class Line
{
    public final int x1, x2, y1, y2;
    public final double gradient, intersect;

    Line(int x1, int x2, int y1, int y2)
    {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;

        // FIXME: constructor work
        // FIXME: / 0
        this.gradient = ((double) y2 - y1) / ((double) x2 - x1);
        this.intersect = ((double) y1) - (this.gradient * x1);
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
}
