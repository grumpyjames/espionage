package net.digihippo.cryptnet;

import java.util.function.Consumer;

interface LineIntersection
{
    void visit(Consumer<Point> results);
}
