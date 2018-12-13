package net.digihippo.cryptnet;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class DoublePointTest
{
    @Test
    public void roundTrip()
    {
        DoublePoint point = new DoublePoint(3453D, 46346.66);

        assertThat(DoublePoint.parse(point.toString()), equalTo(point));
    }

}