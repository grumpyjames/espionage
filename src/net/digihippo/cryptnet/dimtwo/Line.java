package net.digihippo.cryptnet.dimtwo;

import java.util.Collections;
import java.util.function.Consumer;

public final class Line implements LineIntersection, HasLines
{
    public final int x1, x2, y1, y2;
    final double gradient;
    final double intersect;

    private Line(int x1, int x2, int y1, int y2, double gradient, double intersect)
    {
        this.x1 = x1;
        this.x2 = x2;
        this.y1 = y1;
        this.y2 = y2;

        this.gradient = gradient;
        this.intersect = intersect;
    }

    public static Line createLine(int x1, int x2, int y1, int y2)
    {
        double gradient = ((double) y2 - y1) / ((double) x2 - x1);
        return new Line(x1, x2, y1, y2, gradient, ((double) y1) - (gradient * (double) x1));
    }

    public static Line createLine(Pixel start, Pixel finish)
    {
        return createLine(start.x, finish.x, start.y, finish.y);
    }

    <T extends Connection.HasPath> Connection<T> connectionTo(T t, DoublePoint point)
    {
        final DoublePoint perpendicularIntersection;
        if (vertical())
        {
            perpendicularIntersection = new DoublePoint(x1, point.y);
        }
        else if (gradient == 0)
        {
            perpendicularIntersection = new DoublePoint(point.x, y1);
        }
        else if (computeY(point.x) == point.y)
        {
            perpendicularIntersection = point;
        }
        else
        {
            // otherwise: shortest distance is length of line perpendicular to this one joining us to point.
            final double inverseGradient = -1D / gradient;
            final double inverseIntersection = point.y - (inverseGradient * point.x);

            // now we need the intersection with that line and us...
            perpendicularIntersection =
                intersection(this.intersect, inverseIntersection, this.gradient, inverseGradient);
        }

        if (withinBounds(perpendicularIntersection))
        {
            return new Connection<>(perpendicularIntersection, this, t);
        }

        final DoublePoint start = new DoublePoint(x1, y1);
        final DoublePoint end = new DoublePoint(x2, y2);
        final double distanceOne = DoublePoint.distanceBetween(point, start);
        final double distanceTwo = DoublePoint.distanceBetween(point, end);
        if (distanceOne <= distanceTwo)
        {
            return new Connection<>(start, this, t);
        }
        else
        {
            return new Connection<>(end, this, t);
        }
    }

    private boolean withinBounds(Pixel point)
    {
        return Math.min(x1, x2) <= point.x && point.x <= Math.max(x1, x2) && Math.min(y1, y2) <= point.y && point.y <= Math.max(y1, y2);
    }

    private boolean withinBounds(DoublePoint point)
    {
        return Math.min(x1, x2) <= point.x && point.x <= Math.max(x1, x2) && Math.min(y1, y2) <= point.y && point.y <= Math.max(y1, y2);
    }

    LineIntersection intersectionWith(Line other)
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

        final Pixel candidate;
        if (vertical())
        {
            candidate = new Pixel(this.x1, other.computeY(this.x1));
        }
        else if (other.vertical())
        {
            candidate = new Pixel(other.x1, computeY(other.x1));
        }
        else
        {
            candidate =
                intersection(
                    this.intersect, other.intersect,
                    this.gradient, other.gradient).round();
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

    private static DoublePoint intersection(
        double intersectOne, double intersectTwo, double gradientOne, double gradientTwo)
    {
        final double x = (intersectOne - intersectTwo) / (gradientTwo - gradientOne);
        final double y = (gradientOne * x) + intersectOne;
        return new DoublePoint(x, y);
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
    public void visit(Consumer<Pixel> results)
    {
        // Well, we're not going to send it infinite points, are we.
    }

    public DoublePoint direction()
    {
        return new DoublePoint(x2 - x1, y2 - y1).toUnit();
    }

    public boolean endsAt(Pixel point)
    {
        return point.x == x2 && point.y == y2;
    }

    boolean startsAt(Pixel point)
    {
        return (point.x == x1 && point.y == y1);
    }

    @Override
    public Iterable<Line> lines()
    {
        return Collections.singletonList(this);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Line line = (Line) o;

        if (x1 != line.x1) return false;
        if (x2 != line.x2) return false;
        if (y1 != line.y1) return false;
        if (y2 != line.y2) return false;
        if (Double.compare(line.gradient, gradient) != 0) return false;
        return Double.compare(line.intersect, intersect) == 0;

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        result = x1;
        result = 31 * result + x2;
        result = 31 * result + y1;
        result = 31 * result + y2;
        temp = Double.doubleToLongBits(gradient);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(intersect);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public String toString()
    {
        return new Pixel(x1, y1).toString() + "->" + new Pixel(x2, y2).toString();
    }

    public static Line parse(String asString)
    {
        String[] points = asString.split("->");
        return Line.createLine(Pixel.parse(points[0]), Pixel.parse(points[1]));
    }

    public String toString(Line toHighlight)
    {
        String sep = this.equals(toHighlight) ? "_->_" : "->";
        return new Pixel(x1, y1).toString() + sep + new Pixel(x2, y2).toString();
    }
}
