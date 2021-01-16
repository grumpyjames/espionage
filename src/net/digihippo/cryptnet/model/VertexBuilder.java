package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

final class VertexBuilder
{
    static Vertex at(LatLn location)
    {
        return new Vertex(location);
    }
}
