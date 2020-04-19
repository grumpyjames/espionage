package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

public enum Direction {
    Forwards {
        @Override
        public UnitVector orient(UnitVector direction)
        {
            return direction;
        }
    },
    Backwards {
        @Override
        public UnitVector orient(UnitVector direction)
        {
            return direction.reverse();
        }
    };

    public abstract UnitVector orient(UnitVector direction);

    public boolean turnsAt(Path path, int lineIndex, LatLn intersectionPoint) {
        return false;
    }
}
