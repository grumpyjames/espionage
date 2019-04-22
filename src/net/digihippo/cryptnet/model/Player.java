package net.digihippo.cryptnet.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import net.digihippo.cryptnet.dimtwo.*;

import java.io.IOException;

public final class Player
{
    Path path;
    Line line;
    public DoublePoint position;

    private DoublePoint delta = DoublePoint.ZERO;

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
        return direction == player.direction;

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
        return result;
    }

    private Direction direction = Direction.Forwards;

    private transient int lineIndex;

    @Override
    public String toString()
    {
        return "{\n\t" +
            "   \"path\": \"" + path.toString() + "\",\n\t" +
            "   \"line\": \"" + line.toString() + "\",\n\t" +
            "   \"delta\": \"" + delta.toString() + "\",\n\t" +
            "   \"point\": \"" + position.toString() + "\",\n\t" +
            "   \"direction\": \"" + direction.toString() + "\"\n" +
            "}";
    }

    static Player parse(String s)
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

        Player player = new Player(path, line, point);
        player.direction = direction;
        player.delta = delta;

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

    Player(
        Path path,
        Line line,
        DoublePoint position)
    {
        this.path = path;
        this.line = line;
        this.position = position;
        this.lineIndex = path.indexOf(line);
    }

    void tick(Model.Events events)
    {
        events.playerPositionChanged(position);
    }

}
