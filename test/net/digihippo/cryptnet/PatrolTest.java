package net.digihippo.cryptnet;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PatrolTest
{
    private final Line lineOne = Line.createLine(0, 10, 5, 5);
    private final Path pathOne = new Path(Collections.singletonList(lineOne));
    private final Line lineTwo = Line.createLine(5, 5, 5, 10);
    private final Path pathTwo = new Path(Collections.singletonList(lineTwo));
    private final Map<Point, Intersection> intersections = Intersection.intersections(Arrays.asList(pathOne, pathTwo));


    @Test
    public void somePathsAreCircuits()
    {
        Line one = Line.createLine(new Point(0, 0), new Point(0, 1));
        Line two = Line.createLine(new Point(0, 1), new Point(1, 1));
        Line three = Line.createLine(new Point(1, 1), new Point(1, 0));
        Line four = Line.createLine(new Point(1, 0), new Point(0, 0));

        Path circuit = new Path(Arrays.asList(one, two, three, four));

        Patrol patrol = new Patrol(circuit, one, one.direction(), new DoublePoint(0, 0), Direction.Forwards);
        Random random = new Random(22357L);
        tickPrint(Collections.<Point, Intersection>emptyMap(), patrol, random);
        tickPrint(Collections.<Point, Intersection>emptyMap(), patrol, random);
        tickPrint(Collections.<Point, Intersection>emptyMap(), patrol, random);
        tickPrint(Collections.<Point, Intersection>emptyMap(), patrol, random);
        tickPrint(Collections.<Point, Intersection>emptyMap(), patrol, random);
        tickPrint(Collections.<Point, Intersection>emptyMap(), patrol, random);
    }

    @Test
    public void reverseAtEndOfLine()
    {
        Patrol patrol = new Patrol(pathTwo, lineTwo, new DoublePoint(0, -1), new DoublePoint(5, 8), Direction.Forwards);

        Random random = new Random(22357L);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
    }

    @Test
    public void atTeeJunctionDoNotTakeThePhantomFourthWay()
    {
        Patrol patrol = new Patrol(pathOne, lineOne, new DoublePoint(1, 0), new DoublePoint(3, 5), Direction.Forwards);

        Random random = new Random(22353L);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
    }

    @Test
    public void roundTrip()
    {
        Patrol patrol = new Patrol(pathOne, lineOne, new DoublePoint(1, 0), new DoublePoint(3, 5), Direction.Forwards);

        assertThat(Patrol.parse(patrol.toString()), equalTo(patrol));
    }

    private void tickPrint(Map<Point, Intersection> intersections, Patrol patrol, Random random)
    {
        patrol.tick(intersections, random);
        System.out.println(patrol);
    }
}