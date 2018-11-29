package net.digihippo.cryptnet;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ExperimentTest
{
    @Test
    public void nearestConnection()
    {
        Line yEqualsX = new Line(0, 10, 0, 10);
        Line yEqualsTenMinusX = new Line(0, 10, 10, 0);

        Connection connection =
            Experiment.nearestConnection(2, 10, Arrays.asList(yEqualsX, yEqualsTenMinusX));

        System.out.println(connection);
    }

    @Test
    public void nearestConnectionAgain()
    {
        final List<Line> lines = new ArrayList<>();

        lines.add(new Line(30, 40, 60, 100));
        lines.add(new Line(20, 50, 100, 60));

        Connection connection =
            Experiment.nearestConnection(22, 90, lines);

        System.out.println(connection);
    }
}