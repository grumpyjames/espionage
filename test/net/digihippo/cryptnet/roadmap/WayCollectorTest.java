package net.digihippo.cryptnet.roadmap;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Assert;
import org.junit.Test;

import java.util.*;

import static net.digihippo.cryptnet.Lists.palindromic;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.fail;

public class WayCollectorTest
{
    @Test
    public void anIntersectionOfTwoNormalizedWaysShouldReduceToASingleWay()
    {
        WayCollector wayCollector = new WayCollector();

        wayCollector.wayStart();
        wayCollector.waypoint(1);
        wayCollector.waypoint(2);
        wayCollector.waypoint(3);
        wayCollector.wayEnd();

        wayCollector.wayStart();
        wayCollector.waypoint(3);
        wayCollector.waypoint(4);
        wayCollector.waypoint(5);
        wayCollector.wayEnd();

        wayCollector.node(1, new LatLn(1, 2));
        wayCollector.node(2, new LatLn(1, 3));
        wayCollector.node(3, new LatLn(1, 4));
        wayCollector.node(4, new LatLn(1, 5));
        wayCollector.node(5, new LatLn(1, 6));

        Collection<Way> ways = wayCollector.reducedWays();

        Assert.assertEquals(1, ways.size());
        Way[] waysArray = ways.toArray(new Way[1]);
        List<Node> nodes = waysArray[0].nodes;
        List<Long> nodeIds = new ArrayList<>();
        for (Node node : nodes)
        {
            nodeIds.add(node.nodeId);
        }

        assertThat(nodeIds, isOrIsReverseOf(Arrays.asList(1L, 2L, 3L, 4L, 5L)));
    }

    private Matcher<? super List<Long>> isOrIsReverseOf(final List<Long> list)
    {
        final List<Long> reversed = new ArrayList<>();
        for (int i = list.size() - 1; i >= 0; i--)
        {
            reversed.add(list.get(i));
        }

        return new TypeSafeMatcher<List<Long>>()
        {
            @Override
            protected boolean matchesSafely(List<Long> longs)
            {
                return longs.equals(list) || longs.equals(reversed);
            }

            @Override
            public void describeTo(Description description)
            {
                description.appendText("List equal to ").appendValue(list).appendText(" or ").appendValue(reversed);
            }
        };
    }

    @Test
    public void anIntersectionOfTwoNormalizedWaysShouldReduceToASingleWayPartTwo()
    {
        WayCollector wayCollector = new WayCollector();

        wayCollector.wayStart();
        wayCollector.waypoint(1);
        wayCollector.waypoint(2);
        wayCollector.waypoint(3);
        wayCollector.wayEnd();

        wayCollector.wayStart();
        wayCollector.waypoint(5);
        wayCollector.waypoint(4);
        wayCollector.waypoint(3);
        wayCollector.wayEnd();

        wayCollector.node(1, new LatLn(1, 2));
        wayCollector.node(2, new LatLn(1, 3));
        wayCollector.node(3, new LatLn(1, 4));
        wayCollector.node(4, new LatLn(1, 5));
        wayCollector.node(5, new LatLn(1, 6));

        Collection<Way> ways = wayCollector.reducedWays();

        Assert.assertEquals(1, ways.size());
        Way[] waysArray = ways.toArray(new Way[1]);
        List<Node> nodes = waysArray[0].nodes;
        List<Long> nodeIds = new ArrayList<>();
        for (Node node : nodes)
        {
            nodeIds.add(node.nodeId);
        }
        assertThat(nodeIds, isOrIsReverseOf(Arrays.asList(1L, 2L, 3L, 4L, 5L)));
    }

    @Test
    public void anIntersectionOfTwoNormalizedWaysShouldReduceToASingleWayPartThree()
    {
        WayCollector wayCollector = new WayCollector();

        wayCollector.wayStart();
        wayCollector.waypoint(3);
        wayCollector.waypoint(2);
        wayCollector.waypoint(1);
        wayCollector.wayEnd();

        wayCollector.wayStart();
        wayCollector.waypoint(5);
        wayCollector.waypoint(4);
        wayCollector.waypoint(3);
        wayCollector.wayEnd();

        wayCollector.node(1, new LatLn(1, 2));
        wayCollector.node(2, new LatLn(1, 3));
        wayCollector.node(3, new LatLn(1, 4));
        wayCollector.node(4, new LatLn(1, 5));
        wayCollector.node(5, new LatLn(1, 6));

        Collection<Way> ways = wayCollector.reducedWays();

        Assert.assertEquals(1, ways.size());
        Way[] waysArray = ways.toArray(new Way[1]);
        List<Node> nodes = waysArray[0].nodes;
        List<Long> nodeIds = new ArrayList<>();
        for (Node node : nodes)
        {
            nodeIds.add(node.nodeId);
        }
        assertThat(nodeIds, isOrIsReverseOf(Arrays.asList(1L, 2L, 3L, 4L, 5L)));
    }

    @Test
    public void anIntersectionOfTwoNormalizedWaysShouldReduceToASingleWayPartFour()
    {
        WayCollector wayCollector = new WayCollector();

        wayCollector.wayStart();
        wayCollector.waypoint(3);
        wayCollector.waypoint(2);
        wayCollector.waypoint(1);
        wayCollector.wayEnd();

        wayCollector.wayStart();
        wayCollector.waypoint(3);
        wayCollector.waypoint(4);
        wayCollector.waypoint(5);
        wayCollector.wayEnd();

        wayCollector.node(1, new LatLn(1, 2));
        wayCollector.node(2, new LatLn(1, 3));
        wayCollector.node(3, new LatLn(1, 4));
        wayCollector.node(4, new LatLn(1, 5));
        wayCollector.node(5, new LatLn(1, 6));

        Collection<Way> ways = wayCollector.reducedWays();

        Assert.assertEquals(1, ways.size());
        Way[] waysArray = ways.toArray(new Way[1]);
        List<Node> nodes = waysArray[0].nodes;
        List<Long> nodeIds = new ArrayList<>();
        for (Node node : nodes)
        {
            nodeIds.add(node.nodeId);
        }
        assertThat(nodeIds, isOrIsReverseOf(Arrays.asList(1L, 2L, 3L, 4L, 5L)));
    }

    @Test
    public void anotherFiddlyCase()
    {
        WayCollector wayCollector = new WayCollector();

        wayCollector.wayStart();
        wayCollector.waypoint(1);
        wayCollector.waypoint(2);
        wayCollector.wayEnd();

        wayCollector.wayStart();
        wayCollector.waypoint(4);
        wayCollector.waypoint(3);
        wayCollector.waypoint(2);
        wayCollector.wayEnd();


        wayCollector.wayStart();
        wayCollector.waypoint(1);
        wayCollector.waypoint(5);
        wayCollector.wayEnd();


        wayCollector.wayStart();
        wayCollector.waypoint(5);
        wayCollector.waypoint(6);
        wayCollector.wayEnd();


        wayCollector.wayStart();
        wayCollector.waypoint(5);
        wayCollector.waypoint(7);
        wayCollector.wayEnd();


        wayCollector.wayStart();
        wayCollector.waypoint(5);
        wayCollector.waypoint(8);
        wayCollector.wayEnd();

        Collection<Way> ways = wayCollector.reducedWays();
        for (Way way : ways)
        {
            if (palindromic(nodeIds(way)))
            {
                fail("Way " + way + " is palindromic");
            }
        }
    }

    @Test
    public void oneJoinOnlyVassili()
    {
        WayCollector wayCollector = new WayCollector();

        wayCollector.wayStart();
        wayCollector.waypoint(1);
        wayCollector.waypoint(2);
        wayCollector.waypoint(3);
        wayCollector.wayEnd();

        wayCollector.wayStart();
        wayCollector.waypoint(3);
        wayCollector.waypoint(4);
        wayCollector.wayEnd();

        wayCollector.wayStart();
        wayCollector.waypoint(4);
        wayCollector.waypoint(5);
        wayCollector.waypoint(6);
        wayCollector.wayEnd();

        wayCollector.node(1, new LatLn(1, 2));
        wayCollector.node(2, new LatLn(1, 3));
        wayCollector.node(3, new LatLn(1, 4));
        wayCollector.node(4, new LatLn(1, 5));
        wayCollector.node(5, new LatLn(1, 6));
        wayCollector.node(6, new LatLn(1, 7));

        Collection<Way> ways = wayCollector.reducedWays();

        assertThat(ways.size(), equalTo(1));
        Way first = ways.iterator().next();
        assertThat(nodeIds(first), equalTo(Arrays.asList(1L, 2L, 3L, 4L, 5L, 6L)));
    }

    List<Long> nodeIds(final Way way)
    {
        final List<Long> nodeIds = new ArrayList<>();
        for (Node node : way.nodes)
        {
            nodeIds.add(node.nodeId);
        }
        return nodeIds;
    }
}