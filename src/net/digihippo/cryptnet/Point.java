package net.digihippo.cryptnet;

import java.util.List;

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
    public void visit(List<Point> results)
    {
        results.add(this);
    }
}
