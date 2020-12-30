package net.digihippo.cryptnet;

import net.digihippo.cryptnet.dimtwo.Pixel;
import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class Experiment
{
    private static Model startingModel(
            Random random,
            Model.Events events,
            List<Path> paths,
            Rules rules)
    {
        return Model.createModel(paths, rules, random, events);
    }

    interface Event
    {
        void enact(Model model);
    }

    private static class ClickEvent implements Event
    {
        private final LatLn location;

        private ClickEvent(LatLn location)
        {
            this.location = location;
        }

        @Override
        public void enact(Model model)
        {
            model.click(location);
        }
    }

    private static class PrintEvent implements Event
    {
        @Override
        public void enact(Model model)
        {
            System.out.println(model.toString());
        }
    }

    private static final class Viewer extends Component
    {
        private final LatLn topLeft;
        private final LatLn bottomRight;
        private final int width;
        private final int height;

//        private final Model model;
        private final int offsetX = 50;
        private final int offsetY = 50;
        private final BufferedImage[][] images;
        private final BlockingQueue<FrameCollector.Frame> frames = new ArrayBlockingQueue<>(32);
        private final List<Path> paths;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width + (2 * offsetX), height + (2 * offsetY));
        }

        private Viewer(
                final LatLn topLeft,
                final LatLn bottomRight,
                final int width,
                final int height,
                final BufferedImage[][] images,
                final BlockingQueue<Event> events,
                List<Path> paths)
        {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
            this.width = width;
            this.height = height;
            this.images = images;
            this.paths = paths;

            addMouseListener(new EventQueueListener(events));
        }

        @Override
        public void paint(final Graphics g)
        {
            FrameCollector.Frame f = frames.poll();

            for (int i = 0; i < images.length; i++)
            {
                BufferedImage[] image = images[i];
                for (int j = 0; j < image.length; j++)
                {
                    BufferedImage bufferedImage = image[j];

                    g.drawImage(bufferedImage, offsetX + (i * 256), offsetY + (j * 256), null);
                }
            }

            Graphics2D g2 = (Graphics2D)g;
            RenderingHints rh = new RenderingHints(
                    RenderingHints.KEY_TEXT_ANTIALIASING,
                    RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            g2.setRenderingHints(rh);
            g.drawString(toHumanCoords(topLeft), offsetX, offsetY);
            g.drawString(toHumanCoords(bottomRight), offsetX + width, offsetY + height);

            for (Path path : paths)
            {
                for (Segment line: path.segments())
                {
                    drawLine(g, line);
                }
            }
//            for (LatLn latLn: model.paths.locations()) {
//                g.drawPolygon(
//                    new int[] {
//                        offsetX + point.x - 2,
//                        offsetX + point.x + 2,
//                        offsetX + point.x + 2,
//                        offsetX + point.x - 2},
//                    new int[] {
//                        offsetY + point.y + 2,
//                        offsetY + point.y + 2,
//                        offsetY + point.y - 2,
//                        offsetY + point.y - 2},
//                    4);
//            }
            if (f != null)
            {
                f.patrols.forEach(sentry -> renderSentry(sentry.location, sentry.orientation, g));
                f.joining.forEach(joiner -> renderJoiningSentry(g, joiner.location, joiner.connectionLocation));

                renderPlayer(g, f.playerLocation);

                if (f.victory)
                {
                    g.drawString("Victory!", offsetX + (width / 2), offsetY + (height / 2));
                }
                if (f.gameOver)
                {
                    g.drawString("Game over!", offsetX + (width / 2), offsetY + (height / 2));
                }
            }
        }

        private static final DecimalFormat format = new DecimalFormat("#.#####");

        private String toHumanCoords(LatLn topLeft)
        {
            return format.format(Math.toDegrees(topLeft.lat)) + "," + format.format(Math.toDegrees(topLeft.lon));
        }

        private void renderJoiningSentry(Graphics g, LatLn location, LatLn connectionLocation) {
            drawLine(g, location, connectionLocation, true);
            filledCircleAt(g, location, Color.BLUE);
        }

        private void renderPlayer(Graphics g, LatLn playerLocation) {

            filledCircleAt(g, playerLocation, Color.MAGENTA);
        }

        private void filledCircleAt(Graphics g, LatLn latln, Color color)
        {
            final int x1 = offsetX + toX(latln.lon);
            final int y1 = offsetY + toY(latln.lat);

            g.drawOval(x1 - 4, y1 - 4, 4 * 2, 4 * 2);
            g.setColor(color);
            g.fillOval(x1 - 4, y1 - 4, 4 * 2, 4 * 2);
            g.setColor(Color.BLACK);
        }

        private void renderSentry(LatLn location, UnitVector velocity, Graphics g)
        {
            filledCircleAt(g, location, Color.CYAN);
//            throw new UnsupportedOperationException();
//            final double orientation = velocity.orientation();
//            final Pixel tView = velocity.rotate(Math.PI / 12).times(10).round();
//            int radius = 3;
//            int tx1 = (int) Math.round(location.x + (radius * Math.cos(orientation + (Math.PI / 2))));
//            int ty1 = (int) Math.round(location.y + (radius * Math.sin(orientation + (Math.PI / 2))));
//            int tx2 = tView.x + tx1;
//            int ty2 = tView.y + ty1;
//
//            final Pixel uView = velocity.rotate(-Math.PI / 12).times(10).round();
//            int ux1 = (int) Math.round(location.x - (radius * Math.cos(orientation + (Math.PI / 2))));
//            int uy1 = (int) Math.round(location.y - (radius * Math.sin(orientation + (Math.PI / 2))));
//            int ux2 = uView.x + ux1;
//            int uy2 = uView.y + uy1;
//
//            drawCircle(location, g, radius);
//            g.drawLine(offsetX + tx1, offsetY + ty1, offsetX + tx2, offsetY + ty2);
//            g.drawLine(offsetX + ux1, offsetY + uy1, offsetX + ux2, offsetY + uy2);
        }

        private void drawCircle(Pixel renderable, Graphics g, int radius)
        {
            g.drawOval(offsetX + renderable.x - radius, offsetY + renderable.y - radius, radius * 2, radius * 2);
        }

        private void drawLine(Graphics g, Segment segment)
        {
            // reminder: the swing origin is also the top left.
            final LatLn head = segment.head.location;
            final LatLn tail = segment.tail.location;

            drawLine(g, head, tail, false);
        }

        private void drawLine(Graphics g, LatLn head, LatLn tail, boolean debug)
        {
            final int x1 = offsetX + toX(head.lon);
            final int y1 = offsetY + toY(head.lat);
            final int x2 = offsetX + toX(tail.lon);
            final int y2 = offsetY + toY(tail.lat);
            g.drawLine(x1, y1, x2, y2);

            if (debug)
            {
                g.drawString(toHumanCoords(head), x1, y1);
                g.drawString(toHumanCoords(tail), x2, y2);
            }
        }

        private int toY(double latRads)
        {
            double y = WebMercator.y(latRads, 17, 256);
            double originY = WebMercator.y(topLeft.lat, 17, 256);
            return (int) (y - originY);
        }

        private int toX(double lonRads)
        {
            double x = WebMercator.x(lonRads, 17, 256);
            double originX = WebMercator.x(topLeft.lon, 17, 256);

            return (int) (x - originX);
        }

        private LatLn latLn(int x, int y)
        {
            int x1 = x - offsetX;
            int y1 = y - offsetY;

            double originY = WebMercator.y(topLeft.lat, 17, 256);
            double clickY = originY + y1;
            final double latRads = WebMercator.lat(clickY, 17, 256);

            double originX = WebMercator.x(topLeft.lon, 17, 256);
            double clickX = originX + x1;
            final double lonRads = WebMercator.lon(clickX, 17, 256D);

            return new LatLn(latRads, lonRads);
        }

        public void enqueueFrame(FrameCollector.Frame frame)
        {
            try
            {
                frames.put(frame);
            } catch (InterruptedException e)
            {
                e.printStackTrace();
            }
        }

        private class EventQueueListener implements MouseListener
        {
            private final BlockingQueue<Event> events;

            EventQueueListener(BlockingQueue<Event> events)
            {
                this.events = events;
            }

            @Override
            public void mouseReleased(MouseEvent e)
            {
                if (e.getY() >= offsetY)
                {
                    ClickEvent event = new ClickEvent(latLn(e.getX(), e.getY()));
                    pushEvent(event, events);
                }
                else
                {
                    pushEvent(new PrintEvent(), events);
                }
            }

            private void pushEvent(Event event, BlockingQueue<Event> events)
            {
                try
                {
                    events.put(event);
                } catch (InterruptedException e1)
                {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Unable to enqueue event - interrupted");
                }
            }

            @Override public void mouseClicked(MouseEvent e) {}
            @Override public void mousePressed(MouseEvent e) {}
            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException
    {
        int xTile = 65480;
        int yTile = 43572;
        // tile coords increase as latitude decreases
        double latitudeMin = WebMercator.lat((yTile + 2) * 256, 17, 256D);
        double latitudeMax = WebMercator.lat(yTile * 256, 17, 256D);

        // tile coords increase with longitude
        double longitudeMin = WebMercator.lon(xTile * 256, 17, 256D);
        double longitudeMax = WebMercator.lon((xTile + 2) * 256, 17, 256D);

        final Random random = new Random(238824982L);

        final LatLn topLeft = new LatLn(Math.max(latitudeMin, latitudeMax), Math.min(longitudeMin, longitudeMax));
        final LatLn bottomRight = new LatLn(Math.min(latitudeMin, latitudeMax), Math.max(longitudeMin, longitudeMax));

        final BufferedImage[][] images =
            new BufferedImage[2][2];
        images[0][0] = readTile(xTile, yTile);
        LockSupport.parkNanos(10_000_000);
        images[1][0] = readTile(xTile + 1, yTile);
        LockSupport.parkNanos(10_000_000);
        images[0][1] = readTile(xTile, yTile + 1);
        LockSupport.parkNanos(10_000_000);
        images[1][1] = readTile(xTile + 1, yTile + 1);

        final BlockingQueue<Event> events = new LinkedBlockingQueue<>();

        Collection<Way> ways = OsmSource.fetchWays(latitudeMin, latitudeMax, longitudeMin, longitudeMax);
        List<Path> paths = Paths.from(ways);
        final Viewer viewer = new Viewer(topLeft, bottomRight, 512, 512, images, events, paths);
        final Model model = startingModel(
                random,
                new FrameCollector(frame -> {
                    viewer.enqueueFrame(frame);
                    SwingUtilities.invokeLater(viewer::repaint);
                }),
                paths,
                new StayAliveRules(4, 100, 1.2)
        );

        SwingUtilities.invokeLater(() ->
        {
            JFrame f = new JFrame("Lines and intersections");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.add(viewer);
            f.pack();
            f.setVisible(true);
        });

        // Accept one click to set the player location...
        Event event = events.take();
        event.enact(model);

        model.startGame(System.currentTimeMillis());
        while (true)
        {
            model.time(System.currentTimeMillis());
        }
    }

    private static BufferedImage readTile(int xTile, int yTile) throws IOException
    {
        String fileName = "osm/17/" + xTile + "/" + yTile + ".png";

        return ImageIO.read(new File(fileName));
    }

}
