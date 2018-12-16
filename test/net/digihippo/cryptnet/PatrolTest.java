package net.digihippo.cryptnet;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeDiagnosingMatcher;
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

    @Test
    public void doNotBeDistractedByTurnsElsewhereOnTheLine()
    {
        String serialised = "{\n" +
            "\t   \"path\": \"(250,55)->(273,38)->(281,32)->(284,27)->(287,22)->(281,38)->(278,43)->(275,47)->(272,51)->(263,58)->(250,70)->(245,74)->(242,78)->(241,81)->(240,86)->(239,94)->(239,100)->(238,108)\",\n" +
            "\t   \"line\": \"(284,27)->(287,22)\",\n" +
            "\t   \"delta\": \"(0.5144957554275265, -0.8574929257125441)\",\n" +
            "\t   \"point\": \"(284.5294117647059, 26.11764705882348)\",\n" +
            "\t   \"direction\": \"Forwards\",\n" +
            "\t   \"previous\": \"null\",\n" +
            "\t   \"previousTurn\": \"null\"\n" +
            "}";
        Patrol patrol = Patrol.parse(serialised);
        Random random = new Random(53454334L);
        Map<Point, Intersection> none = Collections.emptyMap();
        for (int i = 0; i < 50; i++)
        {
            patrol.tick(none, random);
            assertThat(patrol.path.distanceTo(patrol.point), lessThan(1.0D));
        }
    }

    private Matcher<? super Double> lessThan(final double d)
    {
        return new TypeSafeDiagnosingMatcher<Double>()
        {
            @Override
            protected boolean matchesSafely(Double aDouble, Description description)
            {
                boolean success = aDouble < d;

                if (!success)
                {
                    description.appendText("was " + aDouble);
                }

                return success;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("A double less than " + d);
            }
        };
    }

    private void tickPrint(Map<Point, Intersection> intersections, Patrol patrol, Random random)
    {
        patrol.tick(intersections, random);
        System.out.println(patrol);
    }
}