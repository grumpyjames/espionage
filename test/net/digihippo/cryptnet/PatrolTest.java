package net.digihippo.cryptnet;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Random;

public class PatrolTest
{
    private final Line lineOne = Line.createLine(0, 10, 5, 5);
    private final Path pathOne = new Path(Collections.singletonList(lineOne));
    private final Line lineTwo = Line.createLine(5, 5, 5, 10);
    private final Path pathTwo = new Path(Collections.singletonList(lineTwo));
    private final Map<Point, Intersection> intersections = Model.intersections(Arrays.asList(pathOne, pathTwo));

    // FIXME: assertions, maybe?

    @Test
    public void reverseAtEndOfLine()
    {
        Patrol patrol = new Patrol(new DoublePoint(5, 8), pathTwo, lineTwo, new DoublePoint(0, -1), Direction.Forwards);

        Random random = new Random(22357L);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
    }

    @Test
    public void atTeeJunctionDoNotTakeThePhantomFourthWay()
    {
        Patrol patrol = new Patrol(new DoublePoint(3, 5), pathOne, lineOne, new DoublePoint(1, 0), Direction.Forwards);

        Random random = new Random(22353L);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
        tickPrint(intersections, patrol, random);
    }

    private void tickPrint(Map<Point, Intersection> intersections, Patrol patrol, Random random)
    {
        patrol.tick(intersections, random);
        System.out.println(patrol);
    }
}