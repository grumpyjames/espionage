package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SegmentTest {
    @Test
    public void direction() {
        LatLn from = new LatLn(1.334, 0.556);
        Segment seg = new Segment(
                VertexBuilder.at(from),
                VertexBuilder.at(new LatLn(1.3332, 0.5572))
                );

        UnitVector actual = seg.direction();

        assertEquals(1, actual.applyTo(from).distanceTo(from), 0.01);
    }
}