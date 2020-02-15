package net.digihippo.cryptnet;

import net.digihippo.cryptnet.dimtwo.*;
import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.model.Path;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.OsmSource;
import net.digihippo.cryptnet.roadmap.Way;
import net.digihippo.cryptnet.roadmap.WebMercator;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.locks.LockSupport;

public class Experiment
{
    @SuppressWarnings("SameParameterValue")
    private static Model startingModel(
            Collection<Way> ways)
    {
        return Model.createModel(ways);
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
        // FIXME: and, you know, our updates to this field aren't exactly atomic,
        // ...and happen on a completely different thread.
        private final Model model;
        private final int offsetX = 50;
        private final int offsetY = 50;
        private final BufferedImage[][] images;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(width + (2 * offsetX), height + (2 * offsetY));
        }

        private Viewer(
                final LatLn topLeft,
                final LatLn bottomRight,
                final int width,
                final int height,
                final Model model,
                final BufferedImage[][] images,
                final BlockingQueue<Event> events)
        {
            this.topLeft = topLeft;
            this.bottomRight = bottomRight;
            this.width = width;
            this.height = height;
            this.model = model;
            this.images = images;

            addMouseListener(new EventQueueListener(events));
        }

        @Override
        public void paint(final Graphics g)
        {
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

            for (Path path : model.paths)
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
            for (JoiningSentry sentry : model.joiningSentries) {
                final LatLn location = sentry.location;
                final LatLn velocity = sentry.velocity;
                renderJoiningSentry(g, sentry, location, velocity);
            }

            for (Patrol patrol: model.patrols)
            {
                renderSentry(patrol.location, patrol.velocity, g);
            }

            if (model.player != null)
            {
                LatLn playerLocation = model.player.position;
                renderPlayer(g, playerLocation);
            }
        }

        private static final DecimalFormat format = new DecimalFormat("#.#####");

        private String toHumanCoords(LatLn topLeft)
        {
            return format.format(Math.toDegrees(topLeft.lat)) + "," + format.format(Math.toDegrees(topLeft.lon));
        }

        private void renderJoiningSentry(Graphics g, JoiningSentry sentry, LatLn location, LatLn velocity) {
//            throw new UnsupportedOperationException();
//            renderSentry(location, velocity, g);
//
            drawLine(g, sentry.location, sentry.connection.location());
        }

        private void renderPlayer(Graphics g, LatLn playerLocation) {
            throw new UnsupportedOperationException();
//            g.drawOval(offsetX + playerLocation.x - 4, offsetY + playerLocation.y - 4, 4 * 2, 4 * 2);
//            g.setColor(Color.MAGENTA);
//            g.fillOval(offsetX + playerLocation.x - 4, offsetY + playerLocation.y - 4, 4 * 2, 4 * 2);
//            g.setColor(Color.BLACK);
        }

        private void renderSentry(LatLn location, LatLn velocity, Graphics g)
        {
            throw new UnsupportedOperationException();
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

            drawLine(g, head, tail);
        }

        private void drawLine(Graphics g, LatLn head, LatLn tail)
        {
            final int x1 = offsetX + toX(head.lon);
            final int y1 = offsetY + toY(head.lat);
            final int x2 = offsetX + toX(tail.lon);
            final int y2 = offsetY + toY(tail.lat);
            g.drawLine(x1, y1, x2, y2);
//            g.drawString(toHumanCoords(head), x1, y1);
//            g.drawString(toHumanCoords(tail), x2, y2);
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

            int depthRatio = y1 / height;
            final double latRads = topLeft.lat + (depthRatio * (topLeft.lat - bottomRight.lat));

            int widthRatio = x1 / width;
            final double lonRads = topLeft.lon + ((widthRatio) * (bottomRight.lon - topLeft.lon));


            return new LatLn(latRads, lonRads);
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
                    ClickEvent event = new ClickEvent(latLn(e.getX() - offsetX, e.getY() - offsetY));
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

    public static void main(String[] args) throws IOException
    {
        int xTile = 65480;
        int yTile = 43572;
        // tile coords increase as latitude decreases
        double latitudeMin = WebMercator.lat((yTile + 2) * 256, 17);
        double latitudeMax = WebMercator.lat(yTile * 256, 17);

        // tile coords increase with longitude
        double longitudeMin = WebMercator.lon(xTile * 256, 17);
        double longitudeMax = WebMercator.lon((xTile + 2) * 256, 17);

        final Model model = startingModel(
            OsmSource.fetchWays(latitudeMin, latitudeMax, longitudeMin, longitudeMax)
        );

        final LatLn topLeft = new LatLn(Math.max(latitudeMin, latitudeMax), Math.min(longitudeMin, longitudeMax));
        final LatLn bottomRight = new LatLn(Math.min(latitudeMin, latitudeMax), Math.max(longitudeMin, longitudeMax));

        final BufferedImage[][] images =
            new BufferedImage[2][2];
        images[0][0] = ImageIO.read(new URL("http://a.tile.stamen.com/toner/17/" + xTile + "/" + yTile + ".png"));
        LockSupport.parkNanos(10_000_000);
        images[1][0] = ImageIO.read(new URL("http://a.tile.stamen.com/toner/17/" + (xTile + 1) + "/" + yTile + ".png"));
        LockSupport.parkNanos(10_000_000);
        images[0][1] = ImageIO.read(new URL("http://a.tile.stamen.com/toner/17/" + xTile + "/" + (yTile + 1) + ".png"));
        LockSupport.parkNanos(10_000_000);
        images[1][1] = ImageIO.read(new URL("http://a.tile.stamen.com/toner/17/" + (xTile + 1) + "/" + (yTile + 1) + ".png"));

        final BlockingQueue<Event> events = new LinkedBlockingQueue<>();
        final Random random = new Random(238824982L);
        final Viewer viewer = new Viewer(topLeft, bottomRight, 512, 512, model, images, events);
        final ScheduledExecutorService scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();

        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                JFrame f = new JFrame("Lines and intersections");
                f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                f.add(viewer);
                f.pack();
                f.setVisible(true);

                scheduledExecutorService.scheduleWithFixedDelay(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        // Could be more Elm-like here and make model immutable?
                        Event event;
                        while ((event = events.poll()) != null)
                        {
                            event.enact(model);
                        }
                        model.tick(random, new NoOpEvents());
                        viewer.repaint();
                    }
                }, 40, 40, TimeUnit.MILLISECONDS);
            }
        });
    }

}
