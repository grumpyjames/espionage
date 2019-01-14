package net.digihippo.cryptnet;

import net.digihippo.cryptnet.dimtwo.*;
import net.digihippo.cryptnet.roadmap.NormalizedWay;
import net.digihippo.cryptnet.roadmap.OsmSource;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;

public class Experiment
{
    @SuppressWarnings("SameParameterValue")
    private static Model startingModel(List<NormalizedWay> normalizedWays, int dimension)
    {
        final List<Path> paths = new ArrayList<>();

        for (NormalizedWay normalizedWay : normalizedWays)
        {
            int lineCount = normalizedWay.doublePoints.size() - 1;
            final List<Line> pieces = new ArrayList<>(lineCount);
            for (int i = 0; i < lineCount; i++)
            {
                Pixel start =
                    normalizedWay.doublePoints.get(i).round();
                Pixel end = normalizedWay.doublePoints.get(i + 1).round();
                Line line = Line.createLine(start.x, end.x, start.y, end.y);
                if (!start.equals(end))
                {
                    pieces.add(line);
                }
            }
            paths.add(new Path(pieces));
        }

        return Model.createModel(paths, dimension);
    }

    interface Event
    {
        void enact(Model model);
    }

    private static class ClickEvent implements Event
    {
        private final int x, y;

        ClickEvent(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public void enact(Model model)
        {
            model.click(x, y);
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
            return new Dimension(model.size() + (2 * offsetX), model.size() + (2 * offsetY));
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
            for (Line line: model.lines)
            {
                drawLine(g, line);
            }
            for (Pixel point: model.intersections.keySet()) {
                g.drawPolygon(
                    new int[] {
                        offsetX + point.x - 2,
                        offsetX + point.x + 2,
                        offsetX + point.x + 2,
                        offsetX + point.x - 2},
                    new int[] {
                        offsetY + point.y + 2,
                        offsetY + point.y + 2,
                        offsetY + point.y - 2,
                        offsetY + point.y - 2},
                    4);
            }
            for (JoiningSentry sentry : model.joiningSentries) {
                final Pixel renderable = sentry.position.round();
                final DoublePoint direction = sentry.delta;
                renderSentry(renderable, direction, g);

                g.drawLine(
                    offsetX + renderable.x,
                    offsetY + renderable.y,
                    offsetX + Maths.round(sentry.connection.connectionPoint.x),
                    offsetY + Maths.round(sentry.connection.connectionPoint.y));
            }

            for (Patrol patrol: model.patrols)
            {
                renderSentry(patrol.point.round(), patrol.delta, g);
            }

            if (model.player != null)
            {
                Pixel round = model.player.position.round();
                g.drawOval(offsetX + round.x - 4, offsetY + round.y - 4, 4 * 2, 4 * 2);
                g.setColor(Color.MAGENTA);
                g.fillOval(offsetX + round.x - 4, offsetY + round.y - 4, 4 * 2, 4 * 2);
                g.setColor(Color.BLACK);
            }
        }

        private void renderSentry(Pixel renderable, DoublePoint direction, Graphics g)
        {
            final double orientation = direction.orientation();
            final Pixel tView = direction.rotate(Math.PI / 12).times(10).round();
            int radius = 3;
            int tx1 = (int) Math.round(renderable.x + (radius * Math.cos(orientation + (Math.PI / 2))));
            int ty1 = (int) Math.round(renderable.y + (radius * Math.sin(orientation + (Math.PI / 2))));
            int tx2 = tView.x + tx1;
            int ty2 = tView.y + ty1;

            final Pixel uView = direction.rotate(-Math.PI / 12).times(10).round();
            int ux1 = (int) Math.round(renderable.x - (radius * Math.cos(orientation + (Math.PI / 2))));
            int uy1 = (int) Math.round(renderable.y - (radius * Math.sin(orientation + (Math.PI / 2))));
            int ux2 = uView.x + ux1;
            int uy2 = uView.y + uy1;

            drawCircle(renderable, g, radius);
            g.drawLine(offsetX + tx1, offsetY + ty1, offsetX + tx2, offsetY + ty2);
            g.drawLine(offsetX + ux1, offsetY + uy1, offsetX + ux2, offsetY + uy2);
        }

        private void drawCircle(Pixel renderable, Graphics g, int radius)
        {
            g.drawOval(offsetX + renderable.x - radius, offsetY + renderable.y - radius, radius * 2, radius * 2);
        }

        private void drawLine(Graphics g, Line line)
        {
            g.drawLine(offsetX + line.x1, offsetY + line.y1, offsetX + line.x2, offsetY + line.y2);
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
                    ClickEvent event = new ClickEvent(e.getX() - offsetX, e.getY() - offsetY);
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
        double latitudeMin = OsmSource.lat((yTile + 2) * 256, 17);
        double latitudeMax = OsmSource.lat(yTile * 256, 17);

        // tile coords increase with longitude
        double longitudeMin = OsmSource.lon(xTile * 256, 17);
        double longitudeMax = OsmSource.lon((xTile + 2) * 256, 17);

        final Model model = startingModel(
            OsmSource.fetchWays(latitudeMin, latitudeMax, longitudeMin, longitudeMax),
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
                        model.tick(random);
                        viewer.repaint();
                    }
                }, 40, 40, TimeUnit.MILLISECONDS);
            }
        });
    }

}
