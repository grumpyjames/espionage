package net.digihippo.cryptnet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

final class Patrol
{
    Path path;
    Line line;
    DoublePoint delta;
    DoublePoint point;
    private Direction direction;
    private Intersection previous;
    private Point previousTurn;

    private transient int lineIndex;

    public Patrol(
        Path path, Line line, DoublePoint delta, DoublePoint doublePoint, Direction direction)
    {
        this.path = path;
        this.line = line;
        this.lineIndex = path.indexOf(line);
        this.delta = delta;
        this.point = doublePoint;
        this.direction = direction;
    }

    public void tick(
        final Map<Point, Intersection> intersections,
        final Random random)
    {
        this.point = this.point.plus(delta);

        final Iterable<Point> pixels = this.point.pixelBounds();
        for (Point pixel : pixels)
        {
            Intersection intersection = intersections.get(pixel);
            if (intersection != null)
            {
                // as we move away from an intersection, we'll get 'spurious' collisions
                if (intersection.equals(previous))
                {
                    continue;
                }
                performTurn(random, pixel, intersection);
                break;
            }
            else if (this.path.startsAt(pixel) && this.path.endsAt(pixel) && !pixel.equals(previousTurn))
            {
                boolean forwards = random.nextBoolean();
                if (forwards)
                {
                    pickLine(pixel, this.path.lines.get(0), Direction.Forwards);
                }
                else
                {
                    pickLine(pixel, this.path.lines.get(this.path.lines.size() - 1), Direction.Backwards);
                }
                break;
            }
            else if (this.path.startsAt(pixel) && this.direction == Direction.Backwards && !pixel.equals(previousTurn))
            {
                pickLine(pixel, this.line, Direction.Forwards);
                break;
            }
            else if (this.path.endsAt(pixel) && this.direction == Direction.Forwards && !pixel.equals(previousTurn))
            {
                pickLine(pixel, this.line, Direction.Backwards);
                break;
            }
            else if (!pixel.equals(previousTurn) && direction.turnsAt(this.path, this.lineIndex, pixel))
            {
                this.lineIndex = direction.nextLineIndex(lineIndex);
                this.line = this.path.lines.get(lineIndex);
                this.delta = direction.orient(this.line.direction());
                this.point = pixel.asDoublePoint();
                this.previousTurn = pixel;

                break;
            }
        }
    }

    private void pickLine(Point pixel, Line line, Direction dir)
    {
        this.direction = dir;
        this.delta = this.direction.orient(line.direction());
        this.point = pixel.asDoublePoint();
        this.previous = null;
        this.previousTurn = pixel;
    }

    private void performTurn(Random random, Point pixel, Intersection intersection)
    {
        previous = intersection;
        IntersectionEntry[] lines =
            intersection.entries.toArray(new IntersectionEntry[intersection.entries.size()]);
        IntersectionEntry entry =
            lines[random.nextInt(lines.length)];

        this.delta = entry.direction.orient(entry.line.direction());
        this.direction = entry.direction;
        this.path = entry.path;
        this.line = entry.line;
        this.lineIndex = entry.path.indexOf(entry.line);
        this.point = pixel.asDoublePoint();
        this.previousTurn = null;
    }

    @Override
    public String toString()
    {
        return "{\n\t" +
            "   \"path\": \"" + path.toString() + "\",\n\t" +
            "   \"line\": \"" + line.toString() + "\",\n\t" +
            "   \"delta\": \"" + delta.toString() + "\",\n\t" +
            "   \"point\": \"" + point.toString() + "\",\n\t" +
            "   \"direction\": \"" + direction.toString() + "\",\n\t" +
            "   \"previous\": \"" + (previous == null ? "null" : previous.toString()) + "\",\n\t" +
            "   \"previousTurn\": \"" + (previousTurn == null ? "null" : previousTurn.toString()) + "\"\n" +
            "}";
    }

    public static Patrol parse(String s)
    {
        JsonFactory jfactory = new JsonFactory();
        try
        {
            JsonParser jParser = jfactory.createParser(s);

            return parse(jParser);

        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    static Patrol parse(JsonParser jParser) throws IOException
    {
        jParser.nextToken();
        skipTo(jParser, "path");
        final Path path = Path.parse(jParser.getValueAsString());

        skipTo(jParser, "line");
        final Line line = Line.parse(jParser.getValueAsString());

        skipTo(jParser, "delta");
        final DoublePoint delta = DoublePoint.parse(jParser.getValueAsString());

        skipTo(jParser, "point");
        final DoublePoint point = DoublePoint.parse(jParser.getValueAsString());

        skipTo(jParser, "direction");
        final Direction direction = Direction.valueOf(jParser.getValueAsString());

        skipTo(jParser, "previous");
        String maybeValue = jParser.getValueAsString();
        final Intersection previous = maybeValue.equals("null") ? null : Intersection.parse(maybeValue);

        skipTo(jParser, "previousTurn");
        maybeValue = jParser.getValueAsString();
        final Point previousTurn = maybeValue.equals("null") ? null : Point.parse(maybeValue);

        Patrol patrol = new Patrol(path, line, delta, point, direction);

        patrol.previous = previous;
        patrol.previousTurn = previousTurn;

        jParser.nextToken();

        return patrol;
    }

    private static void skipTo(JsonParser jParser, String fieldName) throws IOException
    {
        while (!fieldName.equals(jParser.currentName()))
        {
            jParser.nextFieldName();
        }

        jParser.nextToken();
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Patrol patrol = (Patrol) o;

        if (path != null ? !path.equals(patrol.path) : patrol.path != null) return false;
        if (line != null ? !line.equals(patrol.line) : patrol.line != null) return false;
        if (delta != null ? !delta.equals(patrol.delta) : patrol.delta != null) return false;
        if (point != null ? !point.equals(patrol.point) : patrol.point != null) return false;
        if (direction != patrol.direction) return false;
        if (previous != null ? !previous.equals(patrol.previous) : patrol.previous != null) return false;
        return !(previousTurn != null ? !previousTurn.equals(patrol.previousTurn) : patrol.previousTurn != null);

    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (line != null ? line.hashCode() : 0);
        result = 31 * result + (delta != null ? delta.hashCode() : 0);
        result = 31 * result + (point != null ? point.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + (previous != null ? previous.hashCode() : 0);
        result = 31 * result + (previousTurn != null ? previousTurn.hashCode() : 0);
        return result;
    }
}
