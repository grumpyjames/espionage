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
        return "(" + x + "," + y + ")";
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

    @SuppressWarnings("SimplifiableIfStatement")
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

    public static Point parse(String point)
    {
        String[] coords = point.split(",");
        return new Point(
            Integer.parseInt(coords[0].substring(1)),
            Integer.parseInt(coords[1].substring(0, coords[1].indexOf(")"))));
    }
}
