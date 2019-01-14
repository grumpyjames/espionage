package net.digihippo.cryptnet.dimtwo;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class IntersectionEntryTest
{
    @Test
    public void roundTrip()
    {
        Line lineOne = Line.createLine(new Pixel(3, 4), new Pixel(11, 10));
        Line lineTwo = Line.createLine(new Pixel(11, 10), new Pixel(20, 10));
        Line lineThree = Line.createLine(new Pixel(20, 10), new Pixel(20, 15));

        IntersectionEntry entry =
            new IntersectionEntry(new Path(Arrays.asList(lineOne, lineTwo, lineThree)), lineTwo, Direction.Forwards);

        assertThat(IntersectionEntry.parse(entry.toString()), equalTo(entry));
    }
}