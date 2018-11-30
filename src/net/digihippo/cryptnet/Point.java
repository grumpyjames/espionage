package net.digihippo.cryptnet;

import java.util.function.Consumer;

class Point implements LineIntersection
{
    final int x, y;

    Point(int x, int y)
    {
        this.x = x;
        this.y = y;
    }

    static double distanceBetween(Point pOne, Point pTwo)
    {
        double dxSquared = Math.pow(pOne.x - pTwo.x, 2);
        double dySquared = Math.pow(pOne.y - pTwo.y, 2);
        return Math.sqrt(dxSquared + dySquared);
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

    @Override
    public void visit(Consumer<Point> results)
    {
        results.accept(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Point point = (Point) o;

        if (x != point.x) return false;
        return y == point.y;
    }

    @Override
    public int hashCode()
    {
        int result = x;
        result = 31 * result + y;
        return result;
    }
}
