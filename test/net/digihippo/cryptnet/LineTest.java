package net.digihippo.cryptnet;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LineTest
{
    @Test
    public void infiniteGradient()
    {
        Line line = Line.createLine(0, 0, 10, 20);
        assertEquals(Double.POSITIVE_INFINITY, line.gradient, 0D);
        assertEquals(Double.NaN, line.intersect, 0D);
    }

    @Test
    public void zeroGradient()
    {
        Line line = Line.createLine(0, 10, 5, 5);
        assertEquals(0D, line.gradient, 0D);
        assertEquals(5D, line.intersect, 0D);
    }

    @Test
    public void intersectionOfVerticalAndHorizontal()
    {
        Line vertical = Line.createLine(5, 5, 0, 10);
        Line horizontal = Line.createLine(0, 10, 5, 5);

        Point point = (Point) vertical.intersectionWith(horizontal);

        assertEquals(5, point.x);
        assertEquals(5, point.y);
    }

    @Test
    public void intersectionOfHorizontalAndVertical()
    {
        Line vertical = Line.createLine(5, 5, 0, 10);
        Line horizontal = Line.createLine(0, 10, 5, 5);

        Point point = (Point) horizontal.intersectionWith(vertical);

        assertEquals(5, point.x);
        assertEquals(5, point.y);
    }

    @Test
    public void intersectionOfVerticalAndVertical()
    {
        Line vertical = Line.createLine(5, 5, 0, 10);
        Line parallelVertical = Line.createLine(10, 10, 0, 10);
        LineIntersection lineIntersection = vertical.intersectionWith(parallelVertical);

        assertEquals(Empty.INSTANCE, lineIntersection);
    }

    @Test
    public void intersectionOfVerticalAndItself()
    {
        Line vertical = Line.createLine(5, 5, 0, 10);
        LineIntersection point = vertical.intersectionWith(vertical);

        assertEquals(point, vertical);
    }

    @Test
    public void intersectionOfVerticalAndFlippedVertical()
    {
        Line vertical = Line.createLine(5, 5, 10, 0);
        Line flipped = Line.createLine(5, 5, 0, 10);
        LineIntersection result = vertical.intersectionWith(flipped);

        assertEquals(vertical, result);
    }

    @Test
    public void horizontalAndItself()
    {
        Line horizontal = Line.createLine(0, 10, 5, 5);
        assertEquals(horizontal, horizontal.intersectionWith(horizontal));
    }

    @Test
    public void horizontalAndParallel()
    {
        Line horizontal = Line.createLine(0, 10, 5, 5);
        Line parallel = Line.createLine(0, 10, 10, 10);
        assertEquals(Empty.INSTANCE, horizontal.intersectionWith(parallel));
    }

    @Test
    public void horizontalAndSomeOtherLine()
    {
        Line horizontal = Line.createLine(0, 10, 5, 5);
        Line xEqualsY = Line.createLine(0, 10, 0, 10);
        Point point = (Point) horizontal.intersectionWith(xEqualsY);
        assertEquals(5, point.x);
        assertEquals(5, point.y);
    }
}