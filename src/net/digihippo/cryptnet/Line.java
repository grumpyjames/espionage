package net.digihippo.cryptnet;

import java.util.function.Consumer;

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
        if (x1 > x2)
        {
            double gradient = ((double) y1 - y2) / ((double) x1 - x2);
            return new Line(x2, x1, y2, y1, gradient, ((double) y2) - (gradient * x2));
        }
        else
        {
            double gradient = ((double) y2 - y1) / ((double) x2 - x1);
            return new Line(x1, x2, y1, y2, gradient, ((double) y1) - (gradient * x1));
        }
    }

    public Connection connectionTo(Point point)
    {
        final Point perpendicularIntersection;
        if (vertical())
        {
            perpendicularIntersection = new Point(x1, point.y);
        }
        else if (gradient == 0)
        {
            perpendicularIntersection = new Point(point.x, y1);
        }
        else if (computeY(point.x) == point.y)
        {
            perpendicularIntersection = point;
        }
        else
        {
            // otherwise: shortest distance is length of line perpendicular to this one joining us to point.
            final double inverseGradient = -1 / gradient;
            final double inverseIntersection = (double) point.y - (inverseGradient * point.x);

            // now we need the intersection with that line and us...
            perpendicularIntersection =
                intersection(this.intersect, inverseIntersection, this.gradient, inverseGradient);
        }

        if (withinBounds(perpendicularIntersection))
        {
            return new Connection(point, perpendicularIntersection, this);
        }

        final Point start = new Point(x1, y1);
        final Point end = new Point(x2, y2);
        final double distanceOne = Point.distanceBetween(point, start);
        final double distanceTwo = Point.distanceBetween(point, end);
        if (distanceOne <= distanceTwo)
        {
            return new Connection(point, start, this);
        }
        else
        {
            return new Connection(point, end, this);
        }
    }

    private boolean withinBounds(Point point)
    {
        return x1 <= point.x && point.x <= x2 && Math.min(y1, y2) <= point.y && point.y <= Math.max(y1, y2);
    }

    @Override
    public String toString()
    {
        return "y = " + gradient + "x + " + intersect;
    }

    public LineIntersection intersectionWith(Line other)
    {
        // FIXME: could simplify by enforcing all infinity gradient lines be positive
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

        final Point candidate;
        if (vertical())
        {
            candidate = new Point(this.x1, other.computeY(this.x1));
        }
        else if (other.vertical())
        {
            candidate = new Point(other.x1, computeY(other.x1));
        }
        else
        {
            candidate = intersection(this.intersect, other.intersect, this.gradient, other.gradient);
        }

        if (withinBounds(candidate) && other.withinBounds(candidate))
        {
            return candidate;
        }
        else
        {
            return Empty.INSTANCE;
        }
    }

    private static Point intersection(
        double intersectOne, double intersectTwo, double gradientOne, double gradientTwo)
    {
        final double x = (intersectOne - intersectTwo) / (gradientTwo - gradientOne);
        final double y = (gradientOne * x) + intersectOne;
        return new Point(Maths.round(x), Maths.round(y));
    }


    private boolean vertical()
    {
        return Double.isInfinite(this.gradient);
    }

    private int computeY(double x)
    {
        return Maths.round((this.gradient * x) + this.intersect);
    }

    @Override
    public void visit(Consumer<Point> results)
    {
        // Well, we're not going to send it infinite points, are we.
    }

    public DoublePoint direction()
    {
        return new DoublePoint(x2 - x1, y2 - y1).toUnit();
    }

    public boolean isLineEnding(Point point)
    {
        return startsAt(point) || endsAt(point);
    }

    public boolean endsAt(Point point)
    {
        return point.x == x2 && point.y == y2;
    }

    public boolean startsAt(Point point)
    {
        return (point.x == x1 && point.y == y1);
    }
}
