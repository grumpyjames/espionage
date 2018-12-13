package net.digihippo.cryptnet;

import org.junit.Test;

import java.util.Arrays;

public class IntersectionTest
{
    @Test
    public void roundTrip()
    {
        Line lineIntersectOne = Line.createLine(new Point(2, 3), new Point(4, 5));
        Line lineTwo = Line.createLine(new Point(4, 5), new Point(4, 6));
        Line lineThree = Line.createLine(new Point(4, 6), new Point(4, 7));
        Path one = new Path(Arrays.asList(lineIntersectOne, lineTwo, lineThree));

        Intersection intersection = new Intersection(new Point(3, 4));

        intersection.add(one, lineIntersectOne);

        System.out.println(Intersection.parse(intersection.toString()));
    }
}