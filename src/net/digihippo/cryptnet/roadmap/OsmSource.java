package net.digihippo.cryptnet.roadmap;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import net.digihippo.cryptnet.DoublePoint;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

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
        fetchWays(600);
    }

    private static final class LatLn
    {
        final double lat, lon;

        LatLn(double lat, double lon)
        {
            this.lat = lat;
            this.lon = lon;
        }

        @Override
        public String toString()
        {
            return "(" + lon + ", " + lat + ")";
        }
    }

    private static final class Node
    {
        LatLn latLn;

        @Override
        public String toString()
        {
            return latLn != null ? latLn.toString() : "()";
        }
    }

    private static final class Way
    {
        private final List<Node> nodes;

        Way(List<Node> nodes)
        {
            this.nodes = nodes;
        }

        @Override
        public String toString()
        {
            return nodes.toString();
        }

        public NormalizedWay translate(double originX, double originLonRads, double originY, double originLatRads, int zoomLevel)
        {
            final List<DoublePoint> result = new ArrayList<>(nodes.size());
            for (Node node : nodes)
            {
                double ourXPixel = x(node.latLn.lon, zoomLevel);
                double x = ourXPixel - originX;
                double ourYPixel = y(node.latLn.lat, zoomLevel);
                double y = ourYPixel - originY;
                result.add(new DoublePoint(x, y));
            }

            return new NormalizedWay(result);
        }
    }

    public static List<NormalizedWay> fetchWays(int pixels) throws IOException
    {
        URLConnection urlConnection =
            new URL("http://overpass-api.de/api/interpreter").openConnection();
        HttpURLConnection connection = (HttpURLConnection) urlConnection;
        connection.setDoOutput(true);
        connection.setDoInput(true);
        // Content-Type: application/x-www-form-urlencoded; charset=UTF-8
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        connection.setRequestMethod("POST");

        // 51.50568402624203,-0.14539718627929685,51.513670243545285
        double latSt  = 51.50568402624203;
        double latEnd = 51.513670243545285;
        double latDiff = latEnd - latSt;
        double lonSt  = -0.14539718627929685;
        double lonEnd = lonSt + latDiff;

        System.out.println(latEnd - latSt);
        System.out.println(lonEnd - lonSt);

        String builder = encode("data") +
            "=" +
            encode("[out:json][timeout:25];\n" +
                "(\n" +
                "  way[\"highway\"](" + latSt + "," + lonSt + "," + latEnd + "," + lonEnd + ");\n" +
                ");\n" +
                "out body;\n" +
                ">;\n" +
                "out skel qt;");

        connection.getOutputStream()
            .write(
                builder.getBytes(StandardCharsets.UTF_8));

        InputStream inputStream = connection.getInputStream();

        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser(inputStream);

        final Map<Long, Node> nodes = new HashMap<>();


        final List<Way> ways = new ArrayList<>();
        int nodeCount = 0;
        while (true) {
            skipTo(jParser, "type");

            String type = jParser.getText();
            if ("way".equals(type))
            {
                skipTo(jParser, "nodes");

                final List<Node> ids = new ArrayList<>();
                while (jParser.nextToken() != JsonToken.END_ARRAY)
                {
                    long nodeId = jParser.getLongValue();
                    final Node forPath = nodes.computeIfAbsent(nodeId, new Function<Long, Node>()
                    {
                        @Override
                        public Node apply(Long aLong)
                        {
                            return new Node();
                        }
                    });
                    ids.add(forPath);
                }

                ways.add(new Way(ids));
            }
            else if ("node".equals(type))
            {
                skipTo(jParser, "id");
                long nodeId = jParser.getLongValue();
                skipTo(jParser, "lat");
                double lat = Math.toRadians(jParser.getDoubleValue());
                skipTo(jParser, "lon");
                double lng = Math.toRadians(jParser.getDoubleValue());

                Node node = nodes.get(nodeId);
                if (node == null)
                {
                    throw new IllegalStateException("Node not found for id: " + nodeId);
                }
                node.latLn = new LatLn(lat, lng);

                nodeCount++;

                if (nodeCount == nodes.keySet().size())
                {
                    break;
                }
            }
        }
        jParser.close();

        final List<NormalizedWay> normalizedWays = new ArrayList<>(ways.size());

        final double originLonRads = Math.toRadians(lonSt);
        final double originX = x(originLonRads, 17);
        final double originLatRads = Math.toRadians(latEnd);
        final double originY = y(originLatRads, 17);

        for (Way way: ways)
        {
            NormalizedWay translate = way.translate(originX, originLonRads, originY, originLatRads, 17);
            System.out.println(translate);
            normalizedWays.add(translate);
        }

        return normalizedWays;
    }

    private static double y(double latRads, int zoomLevel)
    {
        return multiplier(zoomLevel) * (Math.PI - Math.log(Math.tan((Math.PI / 4) + (latRads / 2))));
    }

    private static double x(double lonRads, int zoomLevel)
    {
        return multiplier(zoomLevel) * (lonRads + Math.PI);
    }

    private static double multiplier(int zoomLevel)
    {
        return (256D / (2 * Math.PI)) * Math.pow(2, zoomLevel);
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
        return URLEncoder.encode(query, StandardCharsets.UTF_8.name());
    }

}
