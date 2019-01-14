package net.digihippo.cryptnet.dimtwo;

import net.digihippo.cryptnet.compat.Consumer;

interface LineIntersection
{
    void visit(Consumer<Pixel> results);
}
