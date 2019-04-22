package net.digihippo.cryptnet.model;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import net.digihippo.cryptnet.dimtwo.*;

import java.io.IOException;
import java.util.*;

public final class Model
{
    public final Map<Pixel, Intersection> intersections;
    public final List<JoiningSentry> joiningSentries = new ArrayList<>();
    public final List<Patrol> patrols = new ArrayList<>();
    private final List<Path> paths;
    public final List<Line> lines;
    public final int width;
    public final int height;

    private int sentryIndex = 0;
    public Player player = null;

    public interface Events
    {
        void playerPositionChanged(
            DoublePoint location);

        void sentryPositionChanged(
            String patrolIdentifier,
            DoublePoint location,
            DoublePoint orientation);

        void gameOver();

        void victory();

        void gameRejected(String message);

        void gameStarted();
    }

    public static Model createModel(
        List<Path> paths,
        int width,
        int height)
    {
        return new Model(paths, Intersection.intersections(paths), lines(paths), width, height);
    }

    @Override
    public String toString()
    {
        return "{\n\t" +
            "\"intersections\" : [\n\t\t" + jsonArray(intersections.values(), false) + "\n\t],\n\t" +
            "\"joiningSentries\" : [\n\t\t" + jsonArray(joiningSentries, false) + "\n\t],\n\t" +
            "\"patrols\" : [\n\t\t" + jsonArray(patrols, true) + "\n\t],\n\t" +
            "\"paths\" : [\n\t\t" + jsonArray(paths, false) + "\n\t],\n\t" +
            "\"player\" : " + player + "\n" +
        "}";
    }

    static Model parse(String string)
    {
        JsonFactory jfactory = new JsonFactory();
        try
        {
            JsonParser jParser = jfactory.createParser(string);

            jParser.nextToken();

            skipTo(jParser, "intersections");
            final List<Intersection> intersections = new ArrayList<>();
            while (jParser.nextToken() != JsonToken.END_ARRAY)
            {
                intersections.add(Intersection.parse(jParser.getValueAsString()));
            }


            skipTo(jParser, "joiningSentries");
            final List<JoiningSentry> joiningSentries = new ArrayList<>();
            while (jParser.nextToken() != JsonToken.END_ARRAY)
            {
                joiningSentries.add(JoiningSentry.parse(jParser.getValueAsString()));
            }

            skipTo(jParser, "patrols");
            final List<Patrol> patrols = new ArrayList<>();
            while (jParser.nextToken() != JsonToken.END_ARRAY)
            {
                patrols.add(Patrol.parse(jParser));
            }

            skipTo(jParser, "paths");
            final List<Path> paths = new ArrayList<>();
            while (jParser.nextToken() != JsonToken.END_ARRAY)
            {
                paths.add(Path.parse(jParser.getValueAsString()));
            }

            skipTo(jParser, "player");
            final Player player;
            if (jParser.getCurrentToken() == JsonToken.START_OBJECT)
            {
                player = Player.parse(jParser);
            }
            else
            {
                player = null;
            }

            Model model = new Model(paths, index(intersections), lines(paths), 256, 256);
            model.joiningSentries.addAll(joiningSentries);
            model.patrols.addAll(patrols);
            model.player = player;
            return model;

        } catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    private static Map<Pixel, Intersection> index(List<Intersection> intersections)
    {
        final Map<Pixel, Intersection> result = new HashMap<>();
        for (Intersection intersection : intersections)
        {
            result.put(intersection.point, intersection);
        }

        return result;
    }

    private static void skipTo(JsonParser jParser, String fieldName) throws IOException
    {
        while (!fieldName.equals(jParser.currentName()))
        {
            jParser.nextFieldName();
        }

        jParser.nextToken();
    }

    private String jsonArray(Collection<?> values, boolean object)
    {
        boolean first = true;
        StringBuilder result = new StringBuilder();
        for (Object value : values)
        {
            if (!first)
            {
                result.append(",\n\t\t");
            }

            if (object)
            {
                result.append(value.toString());
            }
            else
            {
                result.append("\"").append(value.toString()).append("\"");
            }
            first = false;
        }
        return result.toString();
    }

    private Model(
        List<Path> paths,
        Map<Pixel, Intersection> intersections,
        List<Line> lines,
        int width,
        int height)
    {
        this.paths = paths;
        this.intersections = intersections;
        this.lines = lines;
        this.width = width;
        this.height = height;
    }

    public boolean tick(Random random, Events events)
    {
        DeferredModelActions modelActions = new DeferredModelActions();

        for (JoiningSentry sentry : joiningSentries)
        {
            sentry.tick(modelActions, events);
        }

        for (Patrol patrol : patrols)
        {
            patrol.tick(intersections, random, events);

            if (player != null)
            {
                double distanceToPlayer = DoublePoint.distanceBetween(patrol.point, player.position);
                if (distanceToPlayer < 5)
                {
                    events.gameOver();
                }
            }
        }
        if (player != null)
        {
            player.tick(events);
        }

        modelActions.enact(this, events);
        return false;
    }

    void addSentry(int x, int y)
    {
        final Pixel clickPoint = new Pixel(x, y);
        Connection best =
            Connection.nearestConnection(paths, clickPoint.asDoublePoint());

        final Pixel point = new Pixel(x, y);
        joiningSentries.add(
            new JoiningSentry(
                "sentry-" + sentryIndex++,
                best,
                point.asDoublePoint(),
                best.connectionPoint.minus(point.asDoublePoint()).over(5)));
    }

    private static List<Line> lines(List<Path> paths)
    {
        final List<Line> lines = new ArrayList<>();

        for (Path path : paths)
        {
            lines.addAll(path.lines);
        }

        return lines;
    }

    public void setPlayerLocation(int x, int y)
    {
        final Pixel point = new Pixel(x, y);
        Connection connection =
            Connection.nearestConnection(paths, point.asDoublePoint());

        player = new Player(connection.getPath(), connection.line, connection.connectionPoint);
    }

    void removeJoining(List<JoiningSentry> outgoing)
    {
        this.joiningSentries.removeAll(outgoing);
    }

    void addPatrols(List<Patrol> incoming)
    {
        this.patrols.addAll(incoming);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        if (width != model.width) return false;
        if (height != model.height) return false;
        if (sentryIndex != model.sentryIndex) return false;
        if (intersections != null ? !intersections.equals(model.intersections) : model.intersections != null)
            return false;
        if (joiningSentries != null ? !joiningSentries.equals(model.joiningSentries) : model.joiningSentries != null)
            return false;
        if (patrols != null ? !patrols.equals(model.patrols) : model.patrols != null) return false;
        if (paths != null ? !paths.equals(model.paths) : model.paths != null) return false;
        if (lines != null ? !lines.equals(model.lines) : model.lines != null) return false;
        return !(player != null ? !player.equals(model.player) : model.player != null);

    }

    @Override
    public int hashCode()
    {
        int result = intersections != null ? intersections.hashCode() : 0;
        result = 31 * result + (joiningSentries != null ? joiningSentries.hashCode() : 0);
        result = 31 * result + (patrols != null ? patrols.hashCode() : 0);
        result = 31 * result + (paths != null ? paths.hashCode() : 0);
        result = 31 * result + (lines != null ? lines.hashCode() : 0);
        result = 31 * result + width;
        result = 31 * result + height;
        result = 31 * result + sentryIndex;
        result = 31 * result + (player != null ? player.hashCode() : 0);
        return result;
    }

    public void click(int x, int y)
    {
        if (joiningSentries.size() + patrols.size() > 3)
        {
            if (player == null)
            {
                setPlayerLocation(x, y);
            }
        }
        else
        {
            addSentry(x, y);
        }
    }
}
