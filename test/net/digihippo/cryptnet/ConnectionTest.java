package net.digihippo.cryptnet;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class ConnectionTest
{
    @Test
    public void roundTrip()
    {
        Line lineOne = Line.createLine(new Point(44, 120), new Point(65, 210));
        Line lineTwo = Line.createLine(new Point(65, 210), new Point(66, 223));
        Line lineThree = Line.createLine(new Point(66, 223), new Point(44, 120));
        Path path = new Path(Arrays.asList(lineOne, lineTwo, lineThree));

        Connection connection = new Connection(new DoublePoint(44, 120), lineOne, path);

        assertThat(Connection.parse(connection.toString()), equalTo(connection));
    }
}