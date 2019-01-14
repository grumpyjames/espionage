package net.digihippo.cryptnet.dimtwo;

import net.digihippo.cryptnet.compat.Consumer;

public class Pixel implements LineIntersection
{
    public final int x, y;

    public Pixel(int x, int y)
    {
        this.x = x;
        this.y = y;
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

    @Override
    public void visit(Consumer<Pixel> results)
    {
        results.consume(this);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Pixel point = (Pixel) o;

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

    public static Pixel parse(String point)
    {
        String[] coords = point.split(",");
        return new Pixel(
            Integer.parseInt(coords[0].substring(1)),
            Integer.parseInt(coords[1].substring(0, coords[1].indexOf(")"))));
    }
}
