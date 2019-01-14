package net.digihippo.cryptnet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import net.digihippo.cryptnet.dimtwo.*;

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
    private Pixel previousTurn;

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

    private void snapToLine(Pixel pixel, Path path, Line line, Direction direction)
    {
        this.path = path;
        this.line = line;
        this.lineIndex = path.indexOf(line);
        this.delta = direction.orient(line.direction());
        this.point = pixel.asDoublePoint();
        this.direction = direction;
    }

    public void tick(
        final Map<Pixel, Intersection> intersections,
        final Random random)
    {
        this.point = this.point.plus(delta);

        final Iterable<Pixel> pixels = this.point.pixelBounds();
        for (Pixel pixel : pixels)
        {
            Intersection intersection = intersections.get(pixel);
            if (intersection != null)
            {
                // as we move away from an intersection, we'll get 'spurious' collisions
                if (intersection.equals(previous))
                {
                    continue;
                }
                intersection(random, pixel, intersection);
                break;
            }
            else if (this.path.startsAt(pixel) && this.path.endsAt(pixel) && !pixel.equals(previousTurn))
            {
                boolean forwards = random.nextBoolean();
                if (forwards)
                {
                    turn(pixel, this.path, this.path.lines.get(0), Direction.Forwards);
                }
                else
                {
                    turn(pixel, this.path, this.path.lines.get(this.path.lines.size() - 1), Direction.Backwards);
                }
                break;
            }
            else if (this.path.startsAt(pixel) && this.direction == Direction.Backwards && !pixel.equals(previousTurn))
            {
                turn(pixel, this.path, this.line, Direction.Forwards);
                break;
            }
            else if (this.path.endsAt(pixel) && this.direction == Direction.Forwards && !pixel.equals(previousTurn))
            {
                turn(pixel, this.path, this.line, Direction.Backwards);
                break;
            }
            else if (direction.turnsAt(this.path, this.lineIndex, pixel) && !pixel.equals(previousTurn))
            {
                final Line nextLine = this.path.lines.get(direction.nextLineIndex(lineIndex));
                turn(pixel, this.path, nextLine, this.direction);
                break;
            }
        }
    }

    private void turn(Pixel pixel, Path path, Line line, Direction dir)
    {
        snapToLine(pixel, path, line, dir);
        turnComplete(pixel);
    }

    private void turnComplete(Pixel pixel)
    {
        this.previous = null;
        this.previousTurn = pixel;
    }

    private void intersection(Random random, Pixel pixel, Intersection intersection)
    {
        IntersectionEntry[] lines =
            intersection.entries.toArray(new IntersectionEntry[intersection.entries.size()]);
        IntersectionEntry entry =
            lines[random.nextInt(lines.length)];

        snapToLine(pixel, entry.path, entry.line, entry.direction);

        this.previous = intersection;
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
        final Pixel previousTurn = maybeValue.equals("null") ? null : Pixel.parse(maybeValue);

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
