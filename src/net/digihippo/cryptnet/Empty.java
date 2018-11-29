package net.digihippo.cryptnet;

import java.util.List;

public enum Empty implements LineIntersection
{
    INSTANCE
        {
            @Override
            public void visit(List<Point> results)
            {

            }
        }
}
