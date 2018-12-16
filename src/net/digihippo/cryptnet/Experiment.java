package net.digihippo.cryptnet;

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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Experiment
{
    private static Model startingModel(List<NormalizedWay> normalizedWays)
    {
        int dimension = 256;

        final List<Path> paths = new ArrayList<>();

        for (NormalizedWay normalizedWay : normalizedWays)
        {
            int lineCount = normalizedWay.doublePoints.size() - 1;
            final List<Line> pieces = new ArrayList<>(lineCount);
            for (int i = 0; i < lineCount; i++)
            {
                Point start =
                    normalizedWay.doublePoints.get(i).round();
                Point end = normalizedWay.doublePoints.get(i + 1).round();
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

    private static final class Viewer extends Component
    {
        private final Model model;
        private final int offsetX = 50;
        private final int offsetY = 50;
        private final BufferedImage[][] images;

        public Dimension getPreferredSize() {
            return new Dimension(model.size() + offsetX + 100, model.size() + offsetY + 100);
        }

        private Viewer(final Model model, BufferedImage[][] images, final Random random)
        {
            this.model = model;
            this.images = images;

            addMouseListener(new MouseListener()
            {
                @Override
                public void mouseClicked(MouseEvent e)
                {
                }

                @Override
                public void mousePressed(MouseEvent e)
                {

                }

                @Override
                public void mouseReleased(MouseEvent e)
                {
                    if (e.getY() >= offsetY)
                    {
                        onClick(e.getX() - offsetX, e.getY() - offsetY);
                    }
                    else
                    {
                        model.tick(random);
                        repaint();
                        if (e.getX() > 128)
                        {
                            System.out.println(model);
                        }
                    }
                }

                @Override
                public void mouseEntered(MouseEvent e)
                {

                }

                @Override
                public void mouseExited(MouseEvent e)
                {

                }
            });
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
            model.lines.forEach(new Consumer<Line>()
            {
                @Override
                public void accept(Line line)
                {
                    Viewer.this.drawLine(g, line);
                }
            });
            model.intersections.keySet().forEach(new Consumer<Point>()
            {
                @Override
                public void accept(Point point)
                {
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
            });
            model.joiningSentries.forEach(new Consumer<JoiningSentry>()
            {
                @Override
                public void accept(JoiningSentry sentry)
                {
                    final Point renderable = sentry.position.round();
                    final DoublePoint direction = sentry.delta;
                    renderSentry(renderable, direction, g);

                    g.drawLine(
                        offsetX + renderable.x,
                        offsetY + renderable.y,
                        offsetX + Maths.round(sentry.connection.connectionPoint.x),
                        offsetY + Maths.round(sentry.connection.connectionPoint.y));
                }
            });
            model.patrols.forEach(new Consumer<Patrol>()
            {
                @Override
                public void accept(Patrol patrol)
                {
                    renderSentry(patrol.point.round(), patrol.delta, g);
                }
            });

            if (model.player != null)
            {
                Point round = model.player.round();
                drawCircle(round, g, 2);
            }
        }

        private void renderSentry(Point renderable, DoublePoint direction, Graphics g)
        {
            final double orientation = direction.orientation();
            final Point tView = direction.rotate(Math.PI / 12).times(10).round();
            int radius = 3;
            int tx1 = (int) Math.round(renderable.x + (radius * Math.cos(orientation + (Math.PI / 2))));
            int ty1 = (int) Math.round(renderable.y + (radius * Math.sin(orientation + (Math.PI / 2))));
            int tx2 = tView.x + tx1;
            int ty2 = tView.y + ty1;

            final Point uView = direction.rotate(-Math.PI / 12).times(10).round();
            int ux1 = (int) Math.round(renderable.x - (radius * Math.cos(orientation + (Math.PI / 2))));
            int uy1 = (int) Math.round(renderable.y - (radius * Math.sin(orientation + (Math.PI / 2))));
            int ux2 = uView.x + ux1;
            int uy2 = uView.y + uy1;

            drawCircle(renderable, g, radius);
            g.drawLine(offsetX + tx1, offsetY + ty1, offsetX + tx2, offsetY + ty2);
            g.drawLine(offsetX + ux1, offsetY + uy1, offsetX + ux2, offsetY + uy2);
        }

        private void drawCircle(Point renderable, Graphics g, int radius)
        {
            g.drawOval(offsetX + renderable.x - radius, offsetY + renderable.y - radius, radius * 2, radius * 2);
        }

        private void drawLine(Graphics g, Line line)
        {
            g.drawLine(offsetX + line.x1, offsetY + line.y1, offsetX + line.x2, offsetY + line.y2);
        }

        public void onClick(int x, int y)
        {
            if (model.joiningSentries.size() + model.patrols.size() > 3)
            {
                model.addPlayer(x, y);
            }
            else
            {
                model.addSentry(x, y);
            }
        }
    }

    public static void main(String[] args) throws IOException
    {
        int xTile = 65486;
        int yTile = 43583;
        // tile coords increase as latitude decreases
        double latitudeMin = OsmSource.lat((yTile + 2) * 256, 17);
        double latitudeMax = OsmSource.lat(yTile * 256, 17);

        double longitudeMin = OsmSource.lon(xTile * 256, 17);
        double longitudeMax = OsmSource.lon((xTile + 2) * 256, 17);

        final Model model = startingModel(
            OsmSource.fetchWays(
                latitudeMin,
                latitudeMax,
                longitudeMin,
                longitudeMax)
        );
        final BufferedImage[][] images =
            new BufferedImage[2][2];
        images[0][0] = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/" + xTile + "/" + yTile + ".png"));
        images[1][0] = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/" + (xTile + 1) + "/" + yTile + ".png"));
        images[0][1] = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/" + xTile + "/" + (yTile + 1) + ".png"));
        images[1][1] = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/" + (xTile + 1) + "/" + (yTile + 1) + ".png"));


        final Random random = new Random(238824982L);
        final Viewer viewer = new Viewer(model, images, random);
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
                        model.tick(random);
                        viewer.repaint();
                    }
                }, 40, 40, TimeUnit.MILLISECONDS);
            }
        });
    }

}
