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
    private static Model startingModel()
    {
        try
        {
            int dimension = 256;
            List<NormalizedWay> normalizedWays =
                OsmSource.fetchWays(
                    OsmSource.lat(43583 * 256, 17),
                    OsmSource.lat((43583 - 1) * 256, 17),
                    OsmSource.lon(65486 * 256, 17),
                    OsmSource.lon((65486 + 1) * 256, 17));

            final List<Line> lines = new ArrayList<>();

            for (NormalizedWay normalizedWay : normalizedWays)
            {
                for (int i = 0; i < normalizedWay.doublePoints.size() - 1; i++)
                {
                    Point start =
                        normalizedWay.doublePoints.get(i).round();
                    Point end = normalizedWay.doublePoints.get(i + 1).round();
                    lines.add(Line.createLine(start.x, end.x, start.y, end.y));
                }
            }

            // FIXME: there's an off by one here.
            final BufferedImage image = ImageIO.read(new URL("http://c.tile.openstreetmap.org/17/65486/43582.png"));

            return Model.createModel(lines, dimension, image);

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static final class Viewer extends Component
    {
        private final Model model;

        private Viewer(Model model)
        {
            this.model = model;

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
                    onClick(e.getX(), e.getY());
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
            g.drawImage(model.image, 0, 0, null);
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
                        new int[] {point.x - 2, point.x + 2, point.x + 2, point.x - 2},
                        new int[] {point.y + 2, point.y + 2, point.y - 2, point.y - 2},
                        4);
                }
            });
            model.joiningSentries.forEach(new Consumer<JoiningSentry>()
            {
                @Override
                public void accept(JoiningSentry sentry)
                {
                    final Point renderable = sentry.point.round();
                    final DoublePoint direction = sentry.delta;
                    renderSentry(renderable, direction, g);

                    g.drawLine(
                        renderable.x,
                        renderable.y,
                        Maths.round(sentry.connection.connectionPoint.x),
                        Maths.round(sentry.connection.connectionPoint.y));
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
            g.drawLine(tx1, ty1, tx2, ty2);
            g.drawLine(ux1, uy1, ux2, uy2);
        }

        private void drawCircle(Point renderable, Graphics g, int radius)
        {
            g.drawOval(renderable.x - radius, renderable.y - radius, radius * 2, radius * 2);
        }

        private void drawLine(Graphics g, Line line)
        {
            g.drawLine(line.x1, line.y1, line.x2, line.y2);
        }

        public Dimension getPreferredSize() {
            return new Dimension(model.size(), model.size());
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

    public static void main(String[] args)
    {
        final Model model = startingModel();
        final Viewer viewer = new Viewer(model);
        final Random random = new Random(238824982L);

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
