package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.dimtwo.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PlayerTest
{
    private final Line lineOne = Line.createLine(new Pixel(0, 0), new Pixel(10, 10));
    private final Line lineTwo = Line.createLine(new Pixel(10, 10), new Pixel(20, 20));
    private final Line lineThree = Line.createLine(new Pixel(20, 20), new Pixel(30, 30));
    private final Line lineFour = Line.createLine(new Pixel(30, 30), new Pixel(40, 40));
    private final Path path = new Path(Arrays.asList(lineOne, lineTwo, lineThree, lineFour));

    @Test
    public void roundTrip()
    {
        Player player = new Player(path, lineTwo, new DoublePoint(13, 13));

        assertThat(Player.parse(player.toString()), equalTo(player));
    }
}