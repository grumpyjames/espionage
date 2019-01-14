package net.digihippo.cryptnet.dimtwo;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class DoublePointTest
{
    @Test
    public void roundTrip()
    {
        DoublePoint point = new DoublePoint(3453D, 46346.66);

        assertThat(DoublePoint.parse(point.toString()), equalTo(point));
    }

}