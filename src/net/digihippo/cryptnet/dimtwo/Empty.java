package net.digihippo.cryptnet.dimtwo;

import net.digihippo.cryptnet.compat.Consumer;

public enum Empty implements LineIntersection
{
    INSTANCE
        {
            @Override
            public void visit(Consumer<Pixel> results)
            {

            }
        }
}
