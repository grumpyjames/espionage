package net.digihippo.cryptnet;

import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.OsmSource;
import net.digihippo.cryptnet.roadmap.Way;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class Experiment
{
    public static void main(String[] args) throws IOException
    {
        LatLn playerCentre = LatLn.toRads(51.56341665913728, -0.16248070596880343);
        Random random = new Random(238824982L);
        Collection<Way> ways = OsmSource.fetchWays(playerCentre.boundingBox(500));
        List<Path> paths = Paths.from(ways);
        Viewer viewer = Viewer.newViewer();
        Model model = Model.createModel(
                paths,
                new StayAliveRules(7, 150, 10, 30_000),
                random,
                new FrameCollector(viewer));

        SwingUtilities.invokeLater(() ->
        {
            JFrame f = new JFrame("Lines and intersections");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.getContentPane().setBackground(Color.BLACK);
            f.add(viewer);
            f.pack();
            f.setVisible(true);
        });

        SwingUtilities.invokeLater(() ->
        {
            model.setPlayerLocation(playerCentre);
            model.transmitGameReady("experimental");
            model.startGame(System.currentTimeMillis());
        });

        //noinspection InfiniteLoopStatement
        while (true)
        {
            model.time(System.currentTimeMillis());
            LockSupport.parkNanos(10_000_000);
        }
    }

}
