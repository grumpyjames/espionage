package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import org.junit.Test;

import static org.junit.Assert.assertTrue;

public class SegmentTest {
    @Test
    public void direction() {
        final Segment seg = new Segment(
                VertexBuilder.at(new LatLn(1.334, 0.556)),
                VertexBuilder.at(new LatLn(1.3332, 0.5572))
                );

        final LatLn expected = new LatLn(-0.0008, 0.0012);
        final LatLn actual = seg.direction();

        assertTrue(actual.distanceTo(expected) < 0.0001);
    }
}