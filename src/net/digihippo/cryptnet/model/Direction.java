package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.UnitVector;

enum Direction {
    Forwards {
        @Override
        public UnitVector orient(UnitVector direction)
        {
            return direction;
        }

        @Override
        public Vertex pickBound(Segment segment)
        {
            return segment.tail;
        }
    },
    Backwards {
        @Override
        public UnitVector orient(UnitVector direction)
        {
            return direction.reverse();
        }

        @Override
        public Vertex pickBound(Segment segment)
        {
            return segment.head;
        }
    };

    public abstract UnitVector orient(UnitVector direction);

    public abstract Vertex pickBound(Segment segment);
}
