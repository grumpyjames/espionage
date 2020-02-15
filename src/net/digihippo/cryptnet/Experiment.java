package net.digihippo.cryptnet;

import net.digihippo.cryptnet.dimtwo.*;
import net.digihippo.cryptnet.model.*;
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
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.*;

public class Experiment
{
    @SuppressWarnings("SameParameterValue")
    private static Model startingModel(
            Collection<Way> ways,
            int width,
            int height)
    {
        return Model.createModel(ways, width, height);
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
        // FIXME: and, you know, our updates to this field aren't exactly atomic,
        // ...and happen on a completely different thread.
        private final Model model;
        private final int offsetX = 50;
        private final int offsetY = 50;
        private final BufferedImage[][] images;

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(model.width + (2 * offsetX), model.height + (2 * offsetY));
        }

        private Viewer(
            final Model model,
            final BufferedImage[][] images,
            final BlockingQueue<Event> events)
        {
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
            for (Segment line: model.segments)
            {
                drawLine(g, line);
            }
            for (LatLn latLn: model.intersections.locations()) {
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
            }
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

        private void renderJoiningSentry(Graphics g, JoiningSentry sentry, LatLn location, LatLn velocity) {
            throw new UnsupportedOperationException();
//            renderSentry(location, velocity, g);
//
//            g.drawLine(
//                offsetX + renderable.x,
//                offsetY + renderable.y,
//                offsetX + Maths.round(sentry.connection.connectionPoint.x),
//                offsetY + Maths.round(sentry.connection.connectionPoint.y));
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

        private void drawLine(Graphics g, Segment line)
        {
            throw new UnsupportedOperationException();
//            g.drawLine(offsetX + line.x1, offsetY + line.y1, offsetX + line.x2, offsetY + line.y2);
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

            private LatLn latLn(int x, int y)
            {
                throw new UnsupportedOperationException();
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
            OsmSource.fetchWays(latitudeMin, latitudeMax, longitudeMin, longitudeMax),
            512,
            512
        );
        final BufferedImage[][] images =
            new BufferedImage[2][2];
        images[0][0] = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/" + xTile + "/" + yTile + ".png"));
        images[1][0] = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/" + (xTile + 1) + "/" + yTile + ".png"));
        images[0][1] = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/" + xTile + "/" + (yTile + 1) + ".png"));
        images[1][1] = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/" + (xTile + 1) + "/" + (yTile + 1) + ".png"));

        final BlockingQueue<Event> events = new LinkedBlockingQueue<>();
        final Random random = new Random(238824982L);
        final Viewer viewer = new Viewer(model, images, events);
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
