package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static net.digihippo.cryptnet.model.VertexBuilder.at;
import static net.digihippo.cryptnet.roadmap.LatLn.toRads;
import static org.junit.Assert.assertThat;

public class PatrolTest
{
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
                p.initialSegment(),
                p.initialSegment().direction(),
                start,
                Direction.Forwards);
        Random random = new Random(22525252L);

        long requiredMoves = (long) Math.ceil(distanceToVertex / metresPerTick);
        for (int i = 0; i < requiredMoves; i++)
        {
            pat.tick(random);
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
}
