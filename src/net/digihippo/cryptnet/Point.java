package net.digihippo.cryptnet;

class Point
{
    final int x, y;

    Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    @Override
    public String toString()
    {
        return "( " + x + ", " + y + " )";
    }

    public DoublePoint asDoublePoint()
    {
        return new DoublePoint((double) x, (double) y);
    }

    public boolean isEqualTo(Point connectionPoint)
    {
        return this.x == connectionPoint.x && this.y == connectionPoint.y;
    }
}
