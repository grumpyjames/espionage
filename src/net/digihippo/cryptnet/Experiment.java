package net.digihippo.cryptnet;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Experiment
{
    static final class Line
    {
        private final int x1, x2, y1, y2;
        private final double gradient, intersect;

        Line(int x1, int x2, int y1, int y2)
        {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;

            this.gradient = ((double) y2 - y1) / ((double) x2 - x1);
            this.intersect = ((double) y1) - (this.gradient * x1);
        }

        public Connection connectionTo(Point point)
        {
            if (((gradient * point.x) + intersect) == point.y)
            {
                return new Connection(point, point, this);
            }

            // otherwise: shortest distance is length of line perpendicular to this one joining us to point.
            final double inverseGradient = -1 / gradient;
            final double inverseIntersection = (double) point.y - (inverseGradient * point.x);

            // now we need the intersection with that line and us...
            Point intersection =
                intersection(this.intersect, inverseIntersection, this.gradient, inverseGradient);

            return new Connection(point, intersection, this);
        }

        @Override
        public String toString()
        {
            return "y = " + gradient + "x + " + intersect;
        }
    }

    private static double distanceBetween(Point pOne, Point pTwo)
    {
        double dxSquared = Math.pow(pOne.x - pTwo.x, 2);
        double dySquared = Math.pow(pOne.y - pTwo.y, 2);
        return Math.sqrt(dxSquared + dySquared);
    }

    private static Model startingModel()
    {
        final List<Line> lines = new ArrayList<>();

        lines.add(new Line(30, 40, 60, 100));
        lines.add(new Line(20, 50, 100, 60));

        return new Model(lines);
    }

    private static class Point
    {
        private final int x, y;

        private Point(int x, int y)
        {
            this.x = x;
            this.y = y;
        }

        @Override
        public String toString()
        {
            return "( " + x + ", " + y + " )";
        }
    }

    static class Connection
    {
        private final Point point;
        private final Point connectionPoint;
        private final Line connectedTo;
        private final double distance;

        private Connection(Point point, Point connectionPoint, Line connectedTo)
        {
            this.point = point;
            this.connectionPoint = connectionPoint;
            this.connectedTo = connectedTo;
            this.distance = distanceBetween(point, connectionPoint);
        }
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

                    // TODO: Watch out for:
                    //   vertical lines
                    //   horizontal lines
                    //   parallel lines
                    //   lines where x2 < x1
                    //   points outside the bounds of the lines
                    Point intersection =
                        intersection(lineOne.intersect, lineTwo.intersect, lineOne.gradient, lineTwo.gradient);

                    results.add(intersection);
                }
            }

            return results;
        }

        public void tick()
        {

        }

        public void addSentry(int x, int y)
        {
            Connection best = nearestConnection(x, y, lines);

            sentries.add(new Sentry(new Point(x, y), best));
        }

        private class Sentry
        {
            private final Point point;
            private final Connection connection;

            public Sentry(Point point, Connection connection)
            {
                this.point = point;
                this.connection = connection;
            }
        }
    }

    private static Point intersection(
        double intersectOne, double intersectTwo, double gradientOne, double gradientTwo)
    {
        final double x = (intersectOne - intersectTwo) / (gradientTwo - gradientOne);
        final double y = (gradientOne * x) + intersectOne;
        return new Point((int) Math.round(x), (int) Math.round(y));
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
            model.tick();

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
                    Viewer.this.drawPoint(g, point);
                }
            });
            model.sentries.forEach(new Consumer<Model.Sentry>()
            {
                @Override
                public void accept(Model.Sentry sentry)
                {
                    Viewer.this.drawPoint(g, sentry.point);
                    g.drawLine(
                        sentry.point.x, sentry.point.y,
                        sentry.connection.connectionPoint.x, sentry.connection.connectionPoint.y);
                }
            });

            SwingUtilities.invokeLater(new Runnable()
            {
                @Override
                public void run()
                {
                    Viewer.this.repaint();
                }
            });
        }

        private void drawLine(Graphics g, Line line)
        {
            g.drawLine(line.x1, line.y1, line.x2, line.y2);
        }

        private void drawPoint(Graphics g, Point point)
        {
            g.drawOval(point.x - 5, point.y - 5, 10, 10);
        }

        public Dimension getPreferredSize() {
            return new Dimension(250,250);
        }

        public void onClick(int x, int y)
        {
            model.addSentry(x, y);
        }
    }

    public static void main(String[] args)
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                final Viewer viewer = new Viewer(startingModel());
                JFrame f = new JFrame("Lines and intersections");
                f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
                f.add(viewer);
                f.pack();
                f.setVisible(true);
            }
        });
    }

}
