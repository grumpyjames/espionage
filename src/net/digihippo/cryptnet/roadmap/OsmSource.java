package net.digihippo.cryptnet.roadmap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Collection;

import static net.digihippo.cryptnet.roadmap.WebMercator.*;

public class OsmSource
{
    /*

    curl 'http://overpass-api.de/api/interpreter'
    */
    // -H 'Accept: */*'
    /*
    -H 'Referer: http://overpass-turbo.eu/'
    -H 'Origin: http://overpass-turbo.eu'
    -H 'User-Agent: Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.110 Safari/537.36'
    -H 'DNT: 1'
    -H 'Content-Type: application/x-www-form-urlencoded; charset=UTF-8'
    --data 'data=%2F*%0AThis+has+been+generated+by+the+overpass-turbo+wizard.%0AThe+original+search+was%3A%0A%E2%80%9Chighway%3D*+and+type%3Away%E2%80%9D%0A*%2F%0A%5Bout%3Ajson%5D%5Btimeout%3A25%5D%3B%0A%2F%2F+gather+results%0A(%0A++%2F%2F+query+part+for%3A+%E2%80%9Chighway%3D*%E2%80%9D%0A++way%5B%22highway%22%5D(51.50886599379611%2C-0.14155089855194092%2C51.511026132638484%2C-0.13778507709503174)%3B%0A)%3B%0A%2F%2F+print+results%0Aout+body%3B%0A%3E%3B%0Aout+skel+qt%3B'
    --compressed

    */
    public static void main(String[] args) throws IOException
    {
        double latYOrig = 3545567.64;
        double latRads = lat(latYOrig, 17, 256D);

        System.out.printf("%s %s %s\n", latYOrig, latRads, y(latRads, 17, 256D));

        double lonXOrig = 3637774.66774;
        double lonRads = lon(lonXOrig, 17, 256D);

        System.out.printf("%s %s %s\n", lonXOrig, lonRads, x(lonRads, 17, 256D));

        System.out.println(overpassApiBody(51.51045188624859, 51.50874245880335, -0.1373291015625049, -0.13458251953125938));
        System.out.println(overpassApiBody(51.50874245880335, 51.51045188624859, -0.13458251953125938, -0.1373291015625049));
        System.out.println(overpassApiBody(51.50874245880335, 51.51045188624859, -0.1373291015625049, -0.13458251953125938));

        final byte[] bytes = new byte[1024];
        try (final InputStream is = requestVectorData(51.50874245880335, 51.51045188624859, -0.1373291015625049, -0.13458251953125938))
        {
            int read;
            while ((read = is.read(bytes, 0, bytes.length)) > 0)
            {
                System.out.write(bytes, 0, read);
            }
        }
    }

    public static Collection<Way> fetchWays(
            double latitudeMin,
            double latitudeMax,
            double longitudeMin,
            double longitudeMax) throws IOException
    {
        double latSt = Math.toDegrees(latitudeMin);
        double latEnd = Math.toDegrees(latitudeMax);
        double lonSt  = Math.toDegrees(longitudeMin);
        double lonEnd = Math.toDegrees(longitudeMax);

        try (final InputStream inputStream = requestVectorData(latSt, latEnd, lonSt, lonEnd))
        {

            JsonFactory jfactory = new JsonFactory();
            JsonParser jParser = jfactory.createParser(inputStream);

            final WayCollector wayCollector = new WayCollector();

            // FIXME: infinite loop on empty 'elements'
            while (true)
            {
                skipTo(jParser, "type");

                String type = jParser.getText();
                if ("way".equals(type))
                {
                    skipTo(jParser, "nodes");

                    wayCollector.wayStart();
                    while (jParser.nextToken() != JsonToken.END_ARRAY)
                    {
                        wayCollector.waypoint(jParser.getLongValue());
                    }
                    wayCollector.wayEnd();
                }
                else if ("node".equals(type))
                {
                    skipTo(jParser, "id");
                    long nodeId = jParser.getLongValue();
                    skipTo(jParser, "lat");
                    double lat = Math.toRadians(jParser.getDoubleValue());
                    skipTo(jParser, "lon");
                    double lng = Math.toRadians(jParser.getDoubleValue());

                    boolean done = wayCollector.node(nodeId, new LatLn(lat, lng));

                    if (done)
                    {
                        break;
                    }
                }
            }
            jParser.close();

            Collection<Way> ways = wayCollector.reducedWays();

            return ways;
        }
    }

    private static InputStream requestVectorData(double latSt, double latEnd, double lonSt, double lonEnd) throws IOException
    {
        final URLConnection urlConnection =
            new URL("http://overpass-api.de/api/interpreter").openConnection();
        final HttpURLConnection connection = (HttpURLConnection) urlConnection;
        connection.setDoOutput(true);
        connection.setDoInput(true);
        // Content-Type: application/x-www-form-urlencoded; charset=UTF-8
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestMethod("POST");

        String builder = encode("data") +
            "=" +
            encode(overpassApiBody(latSt, latEnd, lonSt, lonEnd));

        connection.getOutputStream().write(builder.getBytes());

        return connection.getInputStream();
    }

    private static String overpassApiBody(double latSt, double latEnd, double lonSt, double lonEnd)
    {
        return "[out:json][timeout:25];\n" +
            "(\n" +
            "  way[\"highway\"](" + latSt + "," + lonSt + "," + latEnd + "," + lonEnd + ");\n" +
            ");\n" +
            "out body;\n" +
            ">;\n" +
            "out skel qt;";
    }

    private static void skipTo(JsonParser jParser, String fieldName) throws IOException
    {
        while (!fieldName.equals(jParser.currentName()))
        {
            jParser.nextFieldName();
        }

        jParser.nextToken();
    }

    private static String encode(String query) throws UnsupportedEncodingException
    {
        return URLEncoder.encode(query, "UTF-8");
    }

}
