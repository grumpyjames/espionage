package net.digihippo.cryptnet.roadmap;

import org.junit.Test;

import static org.junit.Assert.*;

public class LatLnTest {
    @Test
    public void distance() {
        LatLn one = new LatLn(
                Math.toRadians(53.32055555555556),
                Math.toRadians(-1.7297222222222221)
                );
        LatLn two = new LatLn(
                Math.toRadians(53.31861111111111),
                Math.toRadians(-1.6997222222222223)
        );

        assertEquals(
                2004.3678D,
                one.distanceTo(two),
                0.0001D);
    }

    @Test
    public void areTheSameAsYourself() {
        final LatLn self = new LatLn(1.45, 0.12);
        assertTrue(self.sameAs(self));
    }

    @Test
    public void areNotTheSameAsSomethingDifferentInLongitude() {
        final LatLn self = new LatLn(1.45, 0.12);
        final LatLn another = new LatLn(1.45, 0.13);
        assertFalse(self.sameAs(another));
    }

    @Test
    public void areNotTheSameAsSomethingDifferentInLatitude() {
        final LatLn self = new LatLn(1.42, 0.13);
        final LatLn another = new LatLn(1.43, 0.13);
        assertFalse(self.sameAs(another));
    }

    @Test
    public void canAddUp() {
        final LatLn location = new LatLn(1.42, 0.13);
        final LatLn velocity = new LatLn(0.00001, 0.0002);
        final LatLn expected = new LatLn(1.42001, 0.1302);
        final LatLn actual = velocity.applyTo(location);
        assertTrue(expected.sameAs(actual));
    }
}