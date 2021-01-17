package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import org.junit.Test;

import java.util.Arrays;

import static net.digihippo.cryptnet.model.VertexBuilder.at;
import static org.junit.Assert.*;

public class PathTest
{
    private final Segment one = new Segment(
            at(new LatLn(0.1214D, 1.224D)),
            at(new LatLn(0.1215D, 1.224D))
    );
    private final Segment two = new Segment(
            at(new LatLn(0.1215D, 1.224D)),
            at(new LatLn(0.1216D, 1.224D))
    );
    private final Segment three = new Segment(
            at(new LatLn(0.1216D, 1.224D)),
            at(new LatLn(0.1216D, 1.225D))
    );
    private final Segment four = new Segment(
            at(new LatLn(0.1216D, 1.225D)),
            at(new LatLn(0.1218D, 1.227D))
    );

    private final Path p = new Path(Arrays.asList(one, two, three, four));


    @Test
    public void findSegmentIndex() {
        assertEquals(0, p.indexOf(one));
        assertEquals(1, p.indexOf(two));
        assertEquals(2, p.indexOf(three));
        assertEquals(3, p.indexOf(four));
    }

    @Test
    public void startsAt() {
        assertTrue(
                p.startsAt(new LatLn(0.1214D, 1.224D)));
        assertFalse(
                p.startsAt(new LatLn(0.1215D, 1.224D)));
        assertFalse(
                p.startsAt(new LatLn(0.1214D, 1.2241D)));
    }

    @Test
    public void endsAt() {
        assertTrue(
                p.endsAt(new LatLn(0.1218D, 1.227D)));
        assertFalse(
                p.startsAt(new LatLn(0.1215D, 1.224D)));
        assertFalse(
                p.startsAt(new LatLn(0.1214D, 1.2241D)));
    }
}
