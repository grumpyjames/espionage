package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;

public class Connection {
    public static Connection nearestConnection(
            List<Path> paths,
            LatLn location) {
        throw new UnsupportedOperationException();
    }

    public LatLn snapVelocityFrom(LatLn location) {
        // best.connectionPoint.minus(point.asDoublePoint()).over(5))
        throw new UnsupportedOperationException();
    }

    public boolean endsNear(LatLn location) {
        throw new UnsupportedOperationException();
    }

    public LatLn location() {
        throw new UnsupportedOperationException();
    }

    public Path path() {
        throw new UnsupportedOperationException();
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
