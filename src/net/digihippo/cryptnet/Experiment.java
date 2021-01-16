package net.digihippo.cryptnet;

import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.OsmSource;
import net.digihippo.cryptnet.roadmap.Way;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.locks.LockSupport;

public class Experiment
{

    private static final class Viewer extends Component
    {
        private final int width;
        private final int height;

        private final List<Path> paths;
        private final BufferedImage sentinel;
        private final BufferedImage player;

        private FrameCollector.Frame frame;

        private LatLn origin;
        private double xPix;
        private double yPix;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width, height);
        }

        @Override
        public Color getBackground()
        {
            return Color.BLACK;
        }

        private Viewer(
                final int width,
                final int height,
                List<Path> paths,
                BufferedImage sentinel,
                BufferedImage player)
        {
            this.width = width;
            this.height = height;
            this.paths = paths;
            this.sentinel = sentinel;
            this.player = player;
        }

        @Override
        public void paint(final Graphics g)
        {
            FrameCollector.Frame f = frame;
            if (f == null)
            {
                return;
            }

            final double bearing = (3 * Math.PI / 2) + Math.atan((double) height/(double) width);

            LatLn centre = f.playerLocation;

            this.origin = centre.move(200, bearing);
            this.xPix = (2 * (centre.lon - origin.lon) / (double) width);
            this.yPix = (2 * (centre.lat - origin.lat) / (double) height);

            Graphics2D g2 = (Graphics2D)g;
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHints(rh);
            g2.setBackground(Color.BLACK);

            for (Path path : paths)
            {
                for (Segment line: path.segments())
                {
                    drawLine(g, line);
                }
            }

            f.patrols.forEach(sentry -> renderSentry(sentry.location, g));
            f.joining.forEach(joiner -> renderJoiningSentry(g, joiner.location, joiner.connectionLocation));

            renderPlayer(g, f.playerLocation);

            if (f.victory)
            {
                System.out.println("Victory?");
                g.drawString("Victory!", (width / 2), (height / 2));
            }
            if (f.gameOver)
            {
                System.out.println("Game: Over?");
                g.drawString("Game over!", (width / 2), (height / 2));
            }
        }

        private static final DecimalFormat format = new DecimalFormat("#.#####");

        private String toHumanCoords(LatLn topLeft)
        {
            return format.format(Math.toDegrees(topLeft.lat)) + "," + format.format(Math.toDegrees(topLeft.lon));
        }

        private void renderJoiningSentry(Graphics g, LatLn location, LatLn connectionLocation) {
            drawLine(g, location, connectionLocation, true);
            filledCircleAt(g, location);
        }

        private void renderPlayer(Graphics g, LatLn playerLocation) {

            int x1 = pix(dLon(playerLocation) / xPix);
            int y1 = pix(dLat(playerLocation) / yPix);
            g.drawImage(
                    player,
                    x1 - (player.getWidth() / 2),
                    y1 - (player.getHeight() / 2),
                    null
            );
        }

        private void filledCircleAt(Graphics g, LatLn latln)
        {
            int x1 = pix(dLon(latln) / xPix);
            int y1 = pix(dLat(latln) / yPix);

            g.drawOval(x1 - 4, y1 - 4, 4 * 2, 4 * 2);
            g.setColor(Color.BLUE);
            g.fillOval(x1 - 4, y1 - 4, 4 * 2, 4 * 2);
            g.setColor(Color.WHITE);
        }

        private void renderSentry(LatLn location, Graphics g)
        {
            int x1 = pix(dLon(location) / xPix);
            int y1 = pix(dLat(location) / yPix);
            g.drawImage(
                    sentinel,
                    x1 - (sentinel.getWidth() / 2),
                    y1 - (sentinel.getHeight() / 2),
                    null
            );

//            g.drawOval(x1 - 4, y1 - 4, 4 * 2, 4 * 2);
//            g.setColor(Color.CYAN);
//            g.fillOval(x1 - 4, y1 - 4, 4 * 2, 4 * 2);
//            g.setColor(Color.WHITE);
        }

        private void drawLine(Graphics g, Segment segment)
        {
            // reminder: the swing origin is also the top left.
            final LatLn head = segment.head.location;
            final LatLn tail = segment.tail.location;

            drawLine(g, head, tail, false);
        }

        double dLon(LatLn latln)
        {
            return latln.lon - origin.lon;
        }

        double dLat(LatLn latln)
        {
            return latln.lat - origin.lat;
        }

        int pix(double dPix)
        {
            return (int) Math.round(dPix);
        }

        private void drawLine(Graphics g, LatLn head, LatLn tail, boolean debug)
        {
            final int startX = pix(dLon(head) / xPix);
            final int endX = pix(dLon(tail) / xPix);
            final int startY = pix(dLat(head) / yPix);
            final int endY = pix(dLat(tail) / yPix);

            g.setColor(Color.WHITE);
            g.drawLine(startX, startY, endX, endY);

            if (debug)
            {
                g.drawString(toHumanCoords(head), startX, startY);
                g.drawString(toHumanCoords(tail), endX, endY);
            }
        }

        public void frame(FrameCollector.Frame frame)
        {
            this.frame = frame;
            repaint();
        }
    }

    public static void main(String[] args) throws IOException
    {
        LatLn playerCentre = LatLn.toRads(54.77683523328153, -1.575421697852002);
        Random random = new Random(238824982L);
        Collection<Way> ways = OsmSource.fetchWays(playerCentre.boundingBox(500));
        List<Path> paths = Paths.from(ways);
        BufferedImage sentinel = ImageIO.read(new File("sentinel.png"));
        BufferedImage player = ImageIO.read(new File("player.png"));
        Viewer viewer = new Viewer(500, 1000, paths, sentinel, player);
        Model model = Model.createModel(
                paths,
                new StayAliveRules(4, 100, 1.2, 30_000),
                random,
                new FrameCollector(new FrameConsumer()
        {
            @Override
            public void gameStarted()
            {

            }

            @Override
            public void onFrame(FrameCollector.Frame frame)
            {
                viewer.frame(frame);
            }
        }));

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
            model.startGame(System.currentTimeMillis());
        });

        while(true)
        {
            SwingUtilities.invokeLater(() -> model.time(System.currentTimeMillis()));
            LockSupport.parkNanos(10_000_000);
        }


    }
}
