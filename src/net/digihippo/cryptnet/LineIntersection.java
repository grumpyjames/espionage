package net.digihippo.cryptnet;

import java.util.function.Consumer;

public interface LineIntersection
{
    void visit(Consumer<Point> results);
}
