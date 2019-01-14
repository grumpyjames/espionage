package net.digihippo.cryptnet.dimtwo;

import net.digihippo.cryptnet.dimtwo.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

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
    public void intersectionOutOfBounds()
    {
        Line one = Line.createLine(0, 10, 0, 10);
        Line two = Line.createLine(0, 4, 10, 6);
        assertEquals(Empty.INSTANCE, one.intersectionWith(two));
    }

    @Test
    public void intersectionOfVerticalAndHorizontal()
    {
        Line vertical = Line.createLine(5, 5, 0, 10);
        Line horizontal = Line.createLine(0, 10, 5, 5);

        Pixel point = (Pixel) vertical.intersectionWith(horizontal);

        assertEquals(5, point.x);
        assertEquals(5, point.y);
    }

    @Test
    public void intersectionOfHorizontalAndVertical()
    {
        Line vertical = Line.createLine(5, 5, 0, 10);
        Line horizontal = Line.createLine(0, 10, 5, 5);

        Pixel point = (Pixel) horizontal.intersectionWith(vertical);

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
        Pixel point = (Pixel) horizontal.intersectionWith(xEqualsY);
        assertEquals(5, point.x);
        assertEquals(5, point.y);
    }

    @Test
    public void nearestConnection()
    {
        Line yEqualsX = Line.createLine(0, 10, 0, 10);
        Line yEqualsTenMinusX = Line.createLine(0, 10, 10, 0);

        final Pixel point = new Pixel(2, 10);
        Connection connection =
            Connection.nearestConnection(onePath(Arrays.asList(yEqualsX, yEqualsTenMinusX)), point.asDoublePoint());

        assertEquals(1, connection.connectionPoint.x, 0);
        assertEquals(9, connection.connectionPoint.y, 0);
    }

    private Iterable<Path> onePath(List<Line> lines)
    {
        return Collections.singletonList(new Path(lines));
    }

    @Test
    public void nearestConnectionWhenPerpendicularLineJoinPointIsOutsideOfBounds()
    {
        Line yEqualsX = Line.createLine(0, 10, 0, 10);

        final Pixel point = new Pixel(11, 15);
        Connection connection =
            Connection.nearestConnection(onePath(Collections.singletonList(yEqualsX)), point.asDoublePoint());

        assertEquals(10, connection.connectionPoint.x, 0);
        assertEquals(10, connection.connectionPoint.y, 0);
    }

    @Test
    public void nearestConnectionWhenPointIsOnLineButOutsideOfBounds()
    {
        Line yEqualsX = Line.createLine(0, 10, 0, 10);

        final Pixel point = new Pixel(11, 11);
        Connection connection =
            Connection.nearestConnection(onePath(Collections.singletonList(yEqualsX)), point.asDoublePoint());

        assertEquals(10, connection.connectionPoint.x, 0);
        assertEquals(10, connection.connectionPoint.y, 0);
    }

    @Test
    public void nearestConnectionToVerticalLine()
    {
        Line vertical = Line.createLine(5, 5, 0, 100);
        Connection connection = vertical.connectionTo(null, new DoublePoint(10, 50));
        assertEquals(5, connection.connectionPoint.x, 0);
        assertEquals(50, connection.connectionPoint.y, 0);
    }

    @Test
    public void nearestConnectionToHorizontalLine()
    {
        Line vertical = Line.createLine(0, 100, 10, 10);
        Connection connection = vertical.connectionTo(null, new DoublePoint(10, 50));
        assertEquals(10, connection.connectionPoint.x, 0);
        assertEquals(10, connection.connectionPoint.y, 0);
    }

    @Test
    public void nearestConnectionToHorizontalLineWhenPerpendicularIntersectOutOfBounds()
    {
        Line vertical = Line.createLine(0, 8, 10, 10);
        Connection connection = vertical.connectionTo(null, new DoublePoint(10, 50));
        assertEquals(8, connection.connectionPoint.x, 0);
        assertEquals(10, connection.connectionPoint.y, 0);
    }

    @Test
    public void roundTripTest()
    {
        Line line = Line.createLine(1, 3, 5, 7);

        assertThat(Line.parse(line.toString()), equalTo(line));
    }
}