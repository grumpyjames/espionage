package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static net.digihippo.cryptnet.model.VertexBuilder.at;
import static net.digihippo.cryptnet.roadmap.LatLn.toRads;
import static org.junit.Assert.*;

public class PathTest
{
    private final Segment one = new Segment(
            at(new LatLn(0.1214D, 1.224D)),
            at(new LatLn(0.1215D, 1.224D))
    );
    private final Segment two = new Segment(
            at(new LatLn(0.1215D, 1.224D)),
            at(new LatLn(0.1216D, 1.224D))
    );
    private final Segment three = new Segment(
            at(new LatLn(0.1216D, 1.224D)),
            at(new LatLn(0.1216D, 1.225D))
    );
    private final Segment four = new Segment(
            at(new LatLn(0.1216D, 1.225D)),
            at(new LatLn(0.1218D, 1.227D))
    );

    private final Path p = new Path(Arrays.asList(one, two, three, four));


    @Test
    public void findSegmentIndex() {
        assertEquals(0, p.indexOf(one));
        assertEquals(1, p.indexOf(two));
        assertEquals(2, p.indexOf(three));
        assertEquals(3, p.indexOf(four));
    }

    @Test
    public void startsAt() {
        assertTrue(
                p.startsAt(new LatLn(0.1214D, 1.224D)));
        assertFalse(
                p.startsAt(new LatLn(0.1215D, 1.224D)));
        assertFalse(
                p.startsAt(new LatLn(0.1214D, 1.2241D)));
    }

    @Test
    public void endsAt() {
        assertTrue(
                p.endsAt(new LatLn(0.1218D, 1.227D)));
        assertFalse(
                p.startsAt(new LatLn(0.1215D, 1.224D)));
        assertFalse(
                p.startsAt(new LatLn(0.1214D, 1.2241D)));
    }

    @Test
    public void snapToNextSegmentAtVertexToAvoidContinuingAlongTangentOfPreviousSegment()
    {
        LatLn start = toRads(51.5624490612535, -0.19040314188250956);
        LatLn vertex = toRads(51.562694259489994, -0.1899911866444164);
        Path p = path(
                start,
                vertex,
                toRads(51.56260707804629, -0.1890971561277036)
        );
        p.visitVertices();

        double distanceToVertex = vertex.distanceTo(start);
        double metresPerTick = 3d; // this is fast; 75m/s
        Patrol pat = new Patrol(
                "tp",
                metresPerTick,
                p,
                p.initialSegment(),
                p.initialSegment().direction(),
                start,
                Direction.Forwards);
        Random random = new Random(22525252L);

        long requiredMoves = (long) Math.ceil(distanceToVertex / metresPerTick);
        for (int i = 0; i < requiredMoves; i++)
        {
            pat.tick(random, new NoOp());
        }
        assertThat(pat.location.distanceTo(vertex), lessThan(metresPerTick));
        assertThat(pat.location.distanceTo(vertex), greaterThan(0));
    }

    private Matcher<Double> lessThan(double upperBoundExclusive)
    {
        return new TypeSafeMatcher<>()
        {
            @Override
            protected boolean matchesSafely(Double d)
            {
                return d < upperBoundExclusive;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("x < ").appendValue(upperBoundExclusive);
            }
        };
    }

    @SuppressWarnings("SameParameterValue")
    private Matcher<Double> greaterThan(double lowerBoundExclusive)
    {
        return new TypeSafeMatcher<>()
        {
            @Override
            protected boolean matchesSafely(Double d)
            {
                return lowerBoundExclusive < d;
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendValue(lowerBoundExclusive).appendText(" < x");
            }
        };
    }


    Path path(LatLn... vertices)
    {
        List<Segment> segmentList = new ArrayList<>();
        for (int i = 1; i < vertices.length; i++)
        {
            LatLn head = vertices[i - 1];
            LatLn tail = vertices[i];

            segmentList.add(new Segment(at(head), at(tail)));
        }

        return new Path(segmentList);
    }

    private static class NoOp implements Model.Events
    {
        @Override
        public void rules(StayAliveRules rules)
        {

        }

        @Override
        public void path(Path path)
        {

        }

        @Override
        public void gameReady(String gameId)
        {

        }

        @Override
        public void gameStarted()
        {

        }

        @Override
        public void frameStart(int frameCounter)
        {

        }

        @Override
        public void playerPositionChanged(LatLn location)
        {

        }

        @Override
        public void patrolPositionChanged(String patrolIdentifier, LatLn location, UnitVector orientation)
        {

        }

        @Override
        public void joiningPatrolPositionChanged(String identifier, LatLn movedTo, UnitVector direction, LatLn joiningLocation)
        {

        }

        @Override
        public void gameOver()
        {

        }

        @Override
        public void victory()
        {

        }

        @Override
        public void frameEnd(int frameCounter)
        {

        }
    }
}
