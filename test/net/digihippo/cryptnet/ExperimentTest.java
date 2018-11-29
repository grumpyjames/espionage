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
        Experiment.Line yEqualsX = new Experiment.Line(0, 10, 0, 10);
        Experiment.Line yEqualsTenMinusX = new Experiment.Line(0, 10, 10, 0);

        Experiment.Connection connection =
            Experiment.nearestConnection(2, 10, Arrays.asList(yEqualsX, yEqualsTenMinusX));

        System.out.println(connection);
    }

    @Test
    public void nearestConnectionAgain()
    {
        final List<Experiment.Line> lines = new ArrayList<>();

        lines.add(new Experiment.Line(30, 40, 60, 100));
        lines.add(new Experiment.Line(20, 50, 100, 60));

        Experiment.Connection connection =
            Experiment.nearestConnection(22, 90, lines);

        System.out.println(connection);
    }
}