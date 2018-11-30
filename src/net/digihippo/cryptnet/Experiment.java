package net.digihippo.cryptnet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class Experiment
{
    private static Model startingModel()
    {
        final List<Line> lines = new ArrayList<>();

        lines.add(Line.createLine(30, 40, 60, 100));
        lines.add(Line.createLine(20, 50, 100, 60));

        lines.add(Line.createLine(10, 10, 0, 250));
        lines.add(Line.createLine(60, 60, 0, 250));
        lines.add(Line.createLine(140, 140, 0, 250));
        lines.add(Line.createLine(220, 220, 0, 250));

        lines.add(Line.createLine(0, 250, 10, 10));
        lines.add(Line.createLine(0, 250, 60, 60));
        lines.add(Line.createLine(0, 250, 90, 90));
        lines.add(Line.createLine(0, 250, 180, 180));

        return new Model(lines);
    }

    static Connection nearestConnection(int x, int y, List<Line> lines)
    {
        double best = Double.MAX_VALUE;
        Point point = new Point(x, y);
        Connection result = null;
        for (Line line : lines)
        {
            Connection connection = line.connectionTo(point);
            if (connection.distance < best)
            {
                result = connection;
                best = connection.distance;
            }
        }
        return result;
    }

    private static final class Model
    {
        private final List<Point> intersections;
        private final List<Line> lines;
        private final List<Sentry> sentries = new ArrayList<>();

        public Model(List<Line> lines)
        {
            this.lines = lines;
            this.intersections = intersections(lines);
        }

        private List<Point> intersections(List<Line> lines)
        {
            final List<Point> results = new ArrayList<>();
            for (int i = 0; i < lines.size(); i++)
            {
                final Line lineOne = lines.get(i);
                for (int j = i + 1; j < lines.size(); j++)
                {
                    final Line lineTwo = lines.get(j);
                    lineOne.intersectionWith(lineTwo).visit(results);
                }
            }

            return results;
        }

        public void tick()
        {
            for (Sentry sentry : sentries)
            {
                sentry.tick();
            }
        }

        public void addSentry(int x, int y)
        {
            Connection best = nearestConnection(x, y, lines);

            sentries.add(new Sentry(new Point(x, y), best));
        }

        public int size()
        {
            return 250;
        }

        private class Sentry
        {
            private final Connection connection;
            private DoublePoint delta;
            private DoublePoint point;

            public Sentry(Point point, Connection connection)
            {
                this.point = point.asDoublePoint();
                this.delta = connection.connectionPoint.asDoublePoint().minus(this.point).over(50);
                this.connection = connection;
            }

            public void tick()
            {
                this.point = this.point.plus(delta);
                if (this.point.round().isEqualTo(this.connection.connectionPoint))
                {
                    this.delta = new DoublePoint(0, 0);
                }
            }
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
            model.lines.forEach(new Consumer<Line>()
            {
                @Override
                public void accept(Line line)
                {
                    Viewer.this.drawLine(g, line);
                }
            });
            model.intersections.forEach(new Consumer<Point>()
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
            model.sentries.forEach(new Consumer<Model.Sentry>()
            {
                @Override
                public void accept(Model.Sentry sentry)
                {
                    final Point renderable = sentry.point.round();

                    final double orientation = sentry.delta.orientation();
                    final Point tView = sentry.delta.rotate(Math.PI / 12).times(10).round();
                    int radius = 3;
                    int tx1 = (int) Math.round(renderable.x + (radius * Math.cos(orientation + (Math.PI / 2))));
                    int ty1 = (int) Math.round(renderable.y + (radius * Math.sin(orientation + (Math.PI / 2))));
                    int tx2 = tView.x + tx1;
                    int ty2 = tView.y + ty1;

                    final Point uView = sentry.delta.rotate(-Math.PI / 12).times(10).round();
                    int ux1 = (int) Math.round(renderable.x - (radius * Math.cos(orientation + (Math.PI / 2))));
                    int uy1 = (int) Math.round(renderable.y - (radius * Math.sin(orientation + (Math.PI / 2))));
                    int ux2 = uView.x + ux1;
                    int uy2 = uView.y + uy1;

                    g.drawOval(renderable.x - radius, renderable.y - radius, radius * 2, radius * 2);
                    g.drawLine(
                        renderable.x,
                        renderable.y,
                        sentry.connection.connectionPoint.x,
                        sentry.connection.connectionPoint.y);
                    g.drawLine(tx1, ty1, tx2, ty2);
                    g.drawLine(ux1, uy1, ux2, uy2);
                }
            });
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
            model.addSentry(x, y);
        }
    }

    public static void main(String[] args)
    {
        final Model model = startingModel();
        final Viewer viewer = new Viewer(model);

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
                        model.tick();
                        viewer.repaint();
                    }
                }, 100, 100, TimeUnit.MILLISECONDS);
            }
        });
    }

}
