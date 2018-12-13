package net.digihippo.cryptnet;

import java.util.Arrays;

public class DoublePoint
{
    double x;
    double y;

    public DoublePoint(double x, double y)
    {
        this.x = x;
        this.y = y;
    }

    public DoublePoint over(int i)
    {
        return new DoublePoint(x / i, y / i);
    }

    public DoublePoint plus(DoublePoint delta)
    {
        return new DoublePoint(delta.x + x, delta.y + y);
    }

    public DoublePoint minus(DoublePoint delta)
    {
        return new DoublePoint(x - delta.x, y - delta.y);
    }

    public DoublePoint toUnit()
    {
        double size = size();

        return new DoublePoint(x / size, y / size);
    }

    private double size()
    {
        return Math.sqrt(Math.pow(x, 2) + Math.pow(y, 2));
    }

    public Point round()
    {
        return new Point(Maths.round(x), Maths.round(y));
    }

    // w.r.t the x axis, in radians, anticlockwise being the positive direction.
    public double orientation()
    {
        return Math.atan2(y, x);
    }

    public DoublePoint times(int i)
    {
        return new DoublePoint(x * i, y * i);
    }

    public DoublePoint rotate(double angle)
    {
        double orientation = orientation();
        double newOrientation = angle + orientation;
        return new DoublePoint(Math.cos(newOrientation), Math.sin(newOrientation));
    }

    public DoublePoint flip()
    {
        return new DoublePoint(-x, -y);
    }

    @Override
    public String toString()
    {
        return "(" + x + ", " + y + ")";
    }

    public Iterable<Point> pixelBounds()
    {
        return Arrays.asList(
            new Point(Maths.floor(x), Maths.floor(y)),
            new Point(Maths.floor(x), Maths.ceil(y)),
            new Point(Maths.ceil(x), Maths.floor(y)),
            new Point(Maths.ceil(x), Maths.ceil(y)));
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DoublePoint that = (DoublePoint) o;

        if (Double.compare(that.x, x) != 0) return false;
        return Double.compare(that.y, y) == 0;

    }

    @Override
    public int hashCode()
    {
        int result;
        long temp;
        temp = Double.doubleToLongBits(x);
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(y);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    public static double distanceBetween(DoublePoint one, DoublePoint two)
    {
        return new DoublePoint(one.x - two.x, one.y - two.y).size();
    }

    public static DoublePoint parse(String point)
    {
        String[] coords = point.split(",");
        return new DoublePoint(
            Double.parseDouble(coords[0].substring(1)),
            Double.parseDouble(coords[1].substring(0, coords[1].indexOf(")"))));
    }
}
