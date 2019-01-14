package net.digihippo.cryptnet;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import net.digihippo.cryptnet.dimtwo.*;

import java.io.IOException;
import java.util.*;

final class Model
{
    private final int size;

    final Map<Pixel, Intersection> intersections;
    final List<JoiningSentry> joiningSentries = new ArrayList<>();
    final List<Patrol> patrols = new ArrayList<>();
    final List<Path> paths;
    final List<Line> lines;
    Player player = null;

    public static Model createModel(List<Path> paths, int size)
    {
        return new Model(paths, Intersection.intersections(paths), lines(paths), size);
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

    public static Model parse(String string)
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

            Model model = new Model(paths, index(intersections), lines(paths), 256);
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
        String result = "";
        for (Object value : values)
        {
            if (!first)
            {
                result += ",\n\t\t";
            }

            if (object)
            {
                result += value.toString();
            }
            else
            {
                result += "\"" + value.toString() + "\"";
            }
            first = false;
        }
        return result;
    }

    private Model(
        List<Path> paths,
        Map<Pixel, Intersection> intersections,
        List<Line> lines,
        int size)
    {
        this.paths = paths;
        this.intersections = intersections;
        this.lines = lines;
        this.size = size;
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

    public void addSentry(int x, int y)
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

    static List<Line> lines(List<Path> paths)
    {
        final List<Line> lines = new ArrayList<>();

        for (Path path : paths)
        {
            lines.addAll(path.lines);
        }

        return lines;
    }

    public void addPlayer(int x, int y)
    {
        final Pixel point = new Pixel(x, y);
        Connection connection =
            Connection.nearestConnection(paths, point.asDoublePoint());

        player = new Player(connection.getPath(), connection.line, connection.connectionPoint);
    }

    public int size()
    {
        return size;
    }

    public void removeJoining(List<JoiningSentry> outgoing)
    {
        this.joiningSentries.removeAll(outgoing);
    }

    public void addPatrols(List<Patrol> incoming)
    {
        this.patrols.addAll(incoming);
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Model model = (Model) o;

        if (size != model.size) return false;
        if (!intersections.equals(model.intersections)) return false;
        if (!joiningSentries.equals(model.joiningSentries)) return false;
        if (!patrols.equals(model.patrols)) return false;
        if (!paths.equals(model.paths)) return false;
        if (!lines.equals(model.lines)) return false;
        return !(player != null ? !player.equals(model.player) : model.player != null);
    }

    @Override
    public int hashCode()
    {
        int result = size;
        result = 31 * result + intersections.hashCode();
        result = 31 * result + joiningSentries.hashCode();
        result = 31 * result + patrols.hashCode();
        result = 31 * result + paths.hashCode();
        result = 31 * result + lines.hashCode();
        result = 31 * result + (player != null ? player.hashCode() : 0);
        return result;
    }

    public void movePlayerTowards(int x, int y)
    {
        if (player != null)
        {
            player.moveTowards(new Pixel(x, y));
        }
    }

    void click(int x, int y)
    {
        if (joiningSentries.size() + patrols.size() > 3)
        {
            if (player == null)
            {
                addPlayer(x, y);
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
