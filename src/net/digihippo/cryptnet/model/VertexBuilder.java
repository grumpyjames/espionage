package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.ArrayList;
import java.util.List;

final class VertexBuilder
{
    static Vertex at(LatLn location)
    {
        return new Vertex(location);
    }
}
