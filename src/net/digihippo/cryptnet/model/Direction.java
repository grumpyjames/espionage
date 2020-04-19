package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

public enum Direction {
    Forwards {
        @Override
        public LatLn orient(LatLn direction)
        {
            return direction;
        }
    },
    Backwards {
        @Override
        public LatLn orient(LatLn direction)
        {
            return direction.reverse();
        }
    };

    public abstract LatLn orient(LatLn direction);

    public boolean turnsAt(Path path, int lineIndex, LatLn intersectionPoint) {
        return false;
    }
}
