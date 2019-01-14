package net.digihippo.cryptnet.dimtwo;

import net.digihippo.cryptnet.dimtwo.Line;
import net.digihippo.cryptnet.dimtwo.Path;
import net.digihippo.cryptnet.dimtwo.Pixel;
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
            Line.createLine(new Pixel(1, 4), new Pixel(5, 6)),
            Line.createLine(new Pixel(5, 6), new Pixel(11, 15)),
            Line.createLine(new Pixel(11, 15), new Pixel(20, 14))
        ));

        assertThat(Path.parse(path.toString()), equalTo(path));
    }

    @Test
    public void sameLineInTwoDirections()
    {
        String pathStr = "(250,55)->(273,38)->(281,32)->(284,27)->(287,22)->(281,38)->(278,43)->(275,47)->(272,51)->(263,58)->(250,70)->(245,74)->(242,78)->(241,81)->(240,86)->(239,94)->(239,100)->(238,108)";
        Path path = Path.parse(pathStr);
    }
}