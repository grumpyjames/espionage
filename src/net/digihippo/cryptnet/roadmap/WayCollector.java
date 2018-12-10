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
        final List<Way> results = new ArrayList<>();
        while (!ways.isEmpty())
        {
            Iterator<Way> iterator = ways.iterator();
            Way originalWay = iterator.next();
            iterator.remove();

            long start = originalWay.firstNodeId();
            long end = originalWay.lastNodeId();
            Set<Way> startWays = edgeNodeToWay.get(start);
            Set<Way> endWays = edgeNodeToWay.get(end);
            while (startWays.size() == 2 || endWays.size() == 2)
            {
                boolean startCondition = startWays.size() == 2;
                boolean endCondition = endWays.size() == 2;
                if (startCondition)
                {
                    Way[] waysArray = startWays.toArray(new Way[2]);
                    Way other = waysArray[0] == originalWay ? waysArray[1] : waysArray[0];
                    ways.remove(other);
                    originalWay = originalWay.concat(start, other);
                    Set<Way> ways = edgeNodeToWay.get(other.oppositeEndTo(start));
                    ways.remove(other);
                    ways.add(originalWay);
                }
                else if (endCondition)
                {
                    Way[] waysArray = endWays.toArray(new Way[2]);
                    Way other = waysArray[0] == originalWay ? waysArray[1] : waysArray[0];
                    ways.remove(other);
                    originalWay = originalWay.concat(end, other);
                    Set<Way> ways = edgeNodeToWay.get(other.oppositeEndTo(start));
                    ways.remove(other);
                    ways.add(originalWay);
                }
                start = originalWay.firstNodeId();
                end = originalWay.lastNodeId();
                startWays = edgeNodeToWay.get(start);
                endWays = edgeNodeToWay.get(end);
            }

            results.add(originalWay);
        }

        return results;
    }
}
