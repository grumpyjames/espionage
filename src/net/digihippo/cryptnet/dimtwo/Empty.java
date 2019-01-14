package net.digihippo.cryptnet.dimtwo;

import java.util.function.Consumer;

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
