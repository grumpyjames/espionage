package net.digihippo.cryptnet.roadmap;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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
        Assert.assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), nodeIds);
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
        Assert.assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), nodeIds);
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
        Assert.assertEquals(Arrays.asList(5L, 4L, 3L, 2L, 1L), nodeIds);
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
        Assert.assertEquals(Arrays.asList(1L, 2L, 3L, 4L, 5L), nodeIds);
    }
}