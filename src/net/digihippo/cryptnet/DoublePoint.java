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

    public Point round()
    {
        return new Point((int) Math.round(x), (int) Math.round(y));
    }
}
