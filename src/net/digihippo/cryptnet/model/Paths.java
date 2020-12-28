package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.Node;
import net.digihippo.cryptnet.roadmap.Way;

import java.util.*;

public final class Paths {
    public static List<Path> from(Collection<Way> ways) {

        final Map<Long, Vertex> vertexBuilderMap
                = new HashMap<>();

        final List<Path> paths = new ArrayList<>(ways.size());

        for (Way way : ways) {
            final List<Node> nodes = way.nodes;
            final List<Segment> segments =
                    new ArrayList<>(way.nodes.size() - 1);

            for (int i = 1; i < nodes.size(); i++) {

                final Vertex head =
                        vertexIfAbsent(vertexBuilderMap, nodes.get(i - 1));
                final Vertex tail =
                        vertexIfAbsent(vertexBuilderMap, nodes.get(i));

                segments.add(new Segment(head, tail));
            }

            Path path = new Path(segments);
            path.visitVertices();
            paths.add(path);
        }


        return Collections.unmodifiableList(paths);
    }

    private static Vertex vertexIfAbsent(
            Map<Long, Vertex> vertexBuilderMap,
            Node node) {
        Vertex vertex = vertexBuilderMap.get(node.nodeId);
        if (vertex == null) {
            vertex = new Vertex(node.latLn);
            vertexBuilderMap.put(
                    node.nodeId,
                    vertex);
        }

        return vertex;
    }

    private Paths() {}
}
