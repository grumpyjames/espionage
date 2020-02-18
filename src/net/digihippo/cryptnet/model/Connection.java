package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.Collections;
import java.util.List;

public class Connection {
    public final Path path;

    private Connection(Path path)
    {
        this.path = path;
    }

    static Connection nearestConnection(
            List<Path> paths,
            LatLn location) {

        double bestDistance = Double.MAX_VALUE;
        Vertex bestVertex = null;

        for (Path path : paths)
        {
            for (Segment segment : path.segments())
            {
                double distance = segment.head.location.distanceTo(location);
                if (distance < bestDistance)
                {
                    bestDistance = distance;
                    bestVertex = segment.head;
                }
            }
        }

        assert bestVertex != null;
        final Path path = new Path(Collections.singletonList(new Segment(new Vertex(location), bestVertex)));
        // DON'T DO THIS!
        // path.visitVertices();
        // Or the joining path becomes part of the map.

        return new Connection(path);
    }

    public LatLn snapVelocityFrom(LatLn location) {
        // best.connectionPoint.minus(point.asDoublePoint()).over(5))
        throw new UnsupportedOperationException();
    }

    public boolean endsNear(LatLn location) {
        throw new UnsupportedOperationException();
    }

    public LatLn location() {
        return path.lastSegment().tail.location;
    }

    public Path path() {
        return path;
    }

    public Segment line() {
        throw new UnsupportedOperationException();
    }

    public LatLn joinVelocity() {
        throw new UnsupportedOperationException();
    }

    public Direction joinDirection() {
        throw new UnsupportedOperationException();
    }
}
