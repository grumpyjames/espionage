package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Way;

import java.io.IOException;
import java.util.Collection;

@FunctionalInterface
interface VectorSource
{
    Collection<Way> fetchWays(LatLn.BoundingBox boundingBox) throws IOException;
}
