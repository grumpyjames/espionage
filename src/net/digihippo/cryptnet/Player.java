package net.digihippo.cryptnet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import net.digihippo.cryptnet.dimtwo.*;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

public class Player
{
    Path path;
    Line line;
    DoublePoint position;

    private DoublePoint delta = DoublePoint.ZERO;
    private Direction direction = Direction.Forwards;

    private transient int lineIndex;

    private Pixel previousTurn;
    private Intersection previous;

    @Override
    public String toString()
    {
        return "{\n\t" +
            "   \"path\": \"" + path.toString() + "\",\n\t" +
            "   \"line\": \"" + line.toString() + "\",\n\t" +
            "   \"delta\": \"" + delta.toString() + "\",\n\t" +
            "   \"point\": \"" + position.toString() + "\",\n\t" +
            "   \"direction\": \"" + direction.toString() + "\",\n\t" +
            "   \"previous\": \"" + (previous == null ? "null" : previous.toString()) + "\",\n\t" +
            "   \"previousTurn\": \"" + (previousTurn == null ? "null" : previousTurn.toString()) + "\"\n" +
            "}";
    }

    public static Player parse(String s)
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

    static Player parse(JsonParser jParser) throws IOException
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

        Player player = new Player(path, line, point);
        player.direction = direction;
        player.delta = delta;
        player.previous = previous;
        player.previousTurn = previousTurn;

        jParser.nextToken();

        return player;
    }

    private static void skipTo(JsonParser jParser, String fieldName) throws IOException
    {
        while (!fieldName.equals(jParser.currentName()))
        {
            jParser.nextFieldName();
        }

        jParser.nextToken();
    }

    public Player(
        Path path,
        Line line,
        DoublePoint position)
    {
        this.path = path;
        this.line = line;
        this.position = position;
        this.lineIndex = path.indexOf(line);
    }

    public void moveTowards(Pixel pixel)
    {
        final Connection<?> connection;
        final int currentIndex;
        final int targetIndex;

        if (!stopped())
        {
            return;
        }

        if (atIntersection())
        {
            final HashSet<Path> paths = new HashSet<>();
            final HashSet<IntersectionEntry> available = new HashSet<>();
            for (IntersectionEntry entry : previous.entries)
            {
                if (paths.add(entry.path))
                {
                    available.add(entry);
                }
            }

            final Connection<IntersectionEntry> entryConnection =
                Connection.nearestConnection(available, pixel.asDoublePoint());
            IntersectionEntry entry = entryConnection.context;

            this.line = entry.line;

            currentIndex = entry.path.indexOf(entry.line);
            targetIndex = entry.path.indexOf(entryConnection.line);
            connection = entryConnection;

            this.direction = direction(connection, currentIndex, targetIndex);

            if (this.direction != entry.direction)
            {
                // Have to find the opposite side.
                // FIXME: write a test exposing this!
                for (IntersectionEntry intersectionEntry : previous.entries)
                {
                    if (intersectionEntry.path.equals(entry.path) && intersectionEntry.direction != entry.direction)
                    {
                        this.line = intersectionEntry.line;
                        break;
                    }
                }
            }

            this.delta = this.direction.orient(this.line.direction());
            this.path = entry.path;
            this.lineIndex = this.path.indexOf(this.line);
        }
        else
        {
            connection =
                Connection.nearestConnection(Collections.singletonList(this.path), pixel.asDoublePoint());
            currentIndex = lineIndex;
            targetIndex = connection.getPath().indexOf(connection.line);

            this.direction = direction(connection, currentIndex, targetIndex);
            this.delta = this.direction.orient(this.line.direction());
        }
    }

    private Direction direction(Connection<?> connection, int currentIndex, int targetIndex)
    {
        if (targetIndex > currentIndex)
        {
            return Direction.Forwards;
        }
        else if (currentIndex > targetIndex)
        {
            return Direction.Backwards;
        }
        else
        {
            DoublePoint minus = connection.connectionPoint.minus(this.position);
            DoublePoint direction = line.direction();
            // FIXME: vertical lines. write a test!
            if (Math.signum(minus.x) == Math.signum(direction.x))
            {
                return Direction.Forwards;
            }
            else
            {
                return Direction.Backwards;
            }
        }
    }

    private boolean atIntersection()
    {
        return previous != null && stopped();
    }

    private boolean stopped()
    {
        return delta.equals(DoublePoint.ZERO);
    }

    public void tick(Map<Pixel, Intersection> intersections)
    {
        try
        {
            if (stopped())
            {
                return;
            }

            this.position = this.position.plus(delta);

            final Iterable<Pixel> pixels = this.position.pixelBounds();

            for (Pixel pixel : pixels)
            {
                final Intersection intersection = intersections.get(pixel);
                if (intersection != null)
                {
                    if (intersection.equals(previous))
                    {
                        continue;
                    }
                    delta = DoublePoint.ZERO;
                    position = pixel.asDoublePoint();
                    previous = intersection;
                    previousTurn = null;
                    break;
                }
                else if (direction.turnsAt(this.path, this.lineIndex, pixel) && !pixel.equals(previousTurn))
                {
                    final Line nextLine = this.path.lines.get(direction.nextLineIndex(lineIndex));
                    turn(pixel, this.path, nextLine, this.direction);
                    break;
                }
                else if (this.path.startsAt(pixel) && this.path.endsAt(pixel) && !pixel.equals(previousTurn))
                {
                    delta = DoublePoint.ZERO;
                    position = pixel.asDoublePoint();
                    previousTurn = pixel;
                    previous = null;
                    break;
                }
                else if (this.path.startsAt(pixel) && this.direction == Direction.Backwards && !pixel.equals(previousTurn))
                {
                    delta = DoublePoint.ZERO;
                    position = pixel.asDoublePoint();
                    previousTurn = pixel;
                    previous = null;
                    break;
                }
                else if (this.path.endsAt(pixel) && this.direction == Direction.Forwards && !pixel.equals(previousTurn))
                {
                    delta = DoublePoint.ZERO;
                    position = pixel.asDoublePoint();
                    previousTurn = pixel;
                    previous = null;
                    break;
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }

    private void turn(Pixel pixel, Path path, Line line, Direction dir)
    {
        snapToLine(pixel, path, line, dir);
        turnComplete(pixel);
    }

    private void turnComplete(Pixel pixel)
    {
        this.previousTurn = pixel;
        this.previous = null;
    }

    private void snapToLine(Pixel pixel, Path path, Line line, Direction direction)
    {
        this.path = path;
        this.line = line;
        this.lineIndex = path.indexOf(line);
        this.delta = direction.orient(line.direction());
        this.position = pixel.asDoublePoint();
        this.direction = direction;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Player player = (Player) o;

        if (lineIndex != player.lineIndex) return false;
        if (path != null ? !path.equals(player.path) : player.path != null) return false;
        if (line != null ? !line.equals(player.line) : player.line != null) return false;
        if (position != null ? !position.equals(player.position) : player.position != null) return false;
        if (delta != null ? !delta.equals(player.delta) : player.delta != null) return false;
        if (direction != player.direction) return false;
        if (previousTurn != null ? !previousTurn.equals(player.previousTurn) : player.previousTurn != null)
            return false;
        return !(previous != null ? !previous.equals(player.previous) : player.previous != null);

    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (line != null ? line.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (delta != null ? delta.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        result = 31 * result + lineIndex;
        result = 31 * result + (previousTurn != null ? previousTurn.hashCode() : 0);
        result = 31 * result + (previous != null ? previous.hashCode() : 0);
        return result;
    }
}
