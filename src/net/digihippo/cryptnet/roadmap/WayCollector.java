package net.digihippo.cryptnet.roadmap;

import java.util.*;
import java.util.function.Function;

final class WayCollector
{
    private final Map<Long, Node> nodes = new HashMap<>();
    private final Set<Way> ways = new HashSet<>();

    private List<Node> accumulating = null;
    private int nodeCount = 0;

    private final Map<Long, Set<Way>> edgeNodeToWay = new HashMap<>();

    void wayStart()
    {
        accumulating = new ArrayList<>();
    }

    void waypoint(final long nodeId)
    {
        final Node forPath = nodes.computeIfAbsent(nodeId, new Function<Long, Node>()
        {
            @Override
            public Node apply(Long aLong)
            {
                return new Node(nodeId);
            }
        });
        accumulating.add(forPath);
    }

    void wayEnd()
    {
        final Way way = new Way(accumulating);
        indexEdge(way, accumulating.get(0).nodeId);
        indexEdge(way, accumulating.get(accumulating.size() - 1).nodeId);
        ways.add(way);
    }

    private void indexEdge(Way way, long nodeId)
    {
        edgeNodeToWay.computeIfAbsent(nodeId, new Function<Long, Set<Way>>()
        {
            @Override
            public Set<Way> apply(Long aLong)
            {
                return new HashSet<>();
            }
        }).add(way);
    }

    boolean node(final long nodeId, final LatLn location)
    {
        nodeCount++;
        Node node = nodes.get(nodeId);
        if (node == null)
        {
            throw new IllegalStateException("Node not found for id: " + nodeId);
        }
        node.latLn = location;

        return nodeCount == nodes.keySet().size();
    }

    public Collection<Way> reducedWays()
    {
        for (Map.Entry<Long, Set<Way>> longSetEntry : edgeNodeToWay.entrySet())
        {
            Set<Way> ways = longSetEntry.getValue();
            if (ways.size() == 2)
            {
                Way[] wayArray = ways.toArray(new Way[2]);

                Way wayOne = wayArray[0];
                Way wayTwo = wayArray[1];
                this.ways.remove(wayOne);
                this.ways.remove(wayTwo);
                this.ways.add(wayOne.concat(longSetEntry.getKey(), wayTwo));
            }
        }

        return this.ways;
    }
}
