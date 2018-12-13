package net.digihippo.cryptnet;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.*;

public class PathTest
{
    @Test
    public void roundTripTest()
    {
        Path path = new Path(Arrays.asList(
            Line.createLine(new Point(1, 4), new Point(5, 6)),
            Line.createLine(new Point(5, 6), new Point(11, 15)),
            Line.createLine(new Point(11, 15), new Point(20, 14))
        ));

        assertThat(Path.parse(path.toString()), equalTo(path));
    }
}