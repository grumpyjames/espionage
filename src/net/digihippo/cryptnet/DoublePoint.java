package net.digihippo.cryptnet;

class DoublePoint
{
    private double x;
    private double y;

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
        return new Point((int) Math.round(x), (int) Math.round(y));
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
}
