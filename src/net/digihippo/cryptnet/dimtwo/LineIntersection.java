package net.digihippo.cryptnet.dimtwo;

import java.util.function.Consumer;

interface LineIntersection
{
    void visit(Consumer<Pixel> results);
}
