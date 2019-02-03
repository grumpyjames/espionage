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
    public Player player = null;

    public static Model createModel(List<Path> paths, int width, int height)
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

    public void tick(Random random)
    {
        DeferredModelActions modelActions = new DeferredModelActions();
        for (JoiningSentry sentry : joiningSentries)
        {
            sentry.tick(modelActions);
        }
        for (Patrol patrol : patrols)
        {
            patrol.tick(intersections, random);

            if (player != null)
            {
                double distanceToPlayer = DoublePoint.distanceBetween(patrol.point, player.position);
                if (distanceToPlayer < 5)
                {
                    System.out.println("Game over man!");
                }
            }
        }
        if (player != null)
        {
            player.tick(intersections);
        }

        modelActions.enact(this);
    }

    void addSentry(int x, int y)
    {
        final Pixel point1 = new Pixel(x, y);
        Connection best =
            Connection.nearestConnection(paths, point1.asDoublePoint());

        final Pixel point = new Pixel(x, y);
        joiningSentries.add(
            new JoiningSentry(
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
        return width == model.width &&
            height == model.height &&
            Objects.equals(intersections, model.intersections) &&
            Objects.equals(joiningSentries, model.joiningSentries) &&
            Objects.equals(patrols, model.patrols) &&
            Objects.equals(paths, model.paths) &&
            Objects.equals(lines, model.lines) &&
            Objects.equals(player, model.player);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(intersections, joiningSentries, patrols, paths, lines, width, height, player);
    }

    private void movePlayerTowards(int x, int y)
    {
        if (player != null)
        {
            player.moveTowards(new Pixel(x, y));
        }
    }

    public void click(int x, int y)
    {
        if (joiningSentries.size() + patrols.size() > 3)
        {
            if (player == null)
            {
                setPlayerLocation(x, y);
            }
            else
            {
                movePlayerTowards(x, y);
            }
        }
        else
        {
            addSentry(x, y);
        }
    }
}
