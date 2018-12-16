package net.digihippo.cryptnet.roadmap;

import net.digihippo.cryptnet.Lists;

import java.util.*;
import java.util.function.Function;

final class WayCollector
{
    private final Map<Long, Node> nodes = new HashMap<>();
    private final Set<Way> ways = new LinkedHashSet<>();

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
                return new LinkedHashSet<>();
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
            Way reduction = iterator.next();
            iterator.remove();

            long start = reduction.firstNodeId();
            long end = reduction.lastNodeId();
            Set<Way> startWays = edgeNodeToWay.get(start);
            Set<Way> endWays = edgeNodeToWay.get(end);

            while (startWays.size() == 2 || endWays.size() == 2)
            {
                if (startWays.size() == 2)
                {
                    Way[] waysArray = startWays.toArray(new Way[2]);
                    Way other = waysArray[0] == reduction ? waysArray[1] : waysArray[0];

                    ways.remove(other);
                    startWays.remove(other);
                    startWays.remove(reduction);
                    endWays.remove(reduction);

                    reduction = reduction.concat(start, other);
                    Set<Way> ways = edgeNodeToWay.get(other.oppositeEndTo(start));

                    endWays.add(reduction);
                    ways.remove(other);
                    ways.add(reduction);
                }
                else if (endWays.size() == 2)
                {
                    Way[] waysArray = endWays.toArray(new Way[2]);
                    Way other = waysArray[0] == reduction ? waysArray[1] : waysArray[0];

                    ways.remove(other);
                    endWays.remove(reduction);
                    endWays.remove(other);
                    startWays.remove(reduction);

                    reduction = reduction.concat(end, other);

                    long otherEndNode = other.oppositeEndTo(end);
                    Set<Way> ways = edgeNodeToWay.get(otherEndNode);
                    startWays.add(reduction);
                    ways.remove(other);
                    ways.add(reduction);
                }
                start = reduction.firstNodeId();
                end = reduction.lastNodeId();
                startWays = edgeNodeToWay.get(start);
                endWays = edgeNodeToWay.get(end);
            }

            assert !Lists.palindromic(reduction.nodes);

            results.add(reduction);
        }

        return results;
    }
}
