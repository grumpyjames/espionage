package net.digihippo.cryptnet.format;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Node;
import net.digihippo.cryptnet.roadmap.UnitVector;
import net.digihippo.cryptnet.roadmap.Way;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static net.digihippo.cryptnet.format.FrameWriter.writeLatLn;
import static org.junit.Assert.assertEquals;

public class GameParametersWriterTest
{
    @Test
    public void roundTrip()
    {
        LatLn roundabout = LatLn.toRads(51.562608864387926, -0.17965266067929553);
        LatLn zebraNearPub = LatLn.toRads(51.56899086921811, -0.17475457002941547);
        LatLn whitestoneWalk = LatLn.toRads(51.56212197526693, -0.17964193184386154);
        LatLn trafficLights = LatLn.toRads(51.56113484159842, -0.17930933794540713);
        LatLn inverforthHouse = LatLn.toRads(51.56511465404501, -0.18069552644221376);
        Node rootNode = Node.node(1, roundabout);
        List<Path> paths = Paths.from(
                Way.ways(
                        Way.way(rootNode, Node.node(2, zebraNearPub)),
                        Way.way(rootNode, Node.node(3, whitestoneWalk), Node.node(4, trafficLights)),
                        Way.way(rootNode, Node.node(5, inverforthHouse))
                )
        );
        Rules rules = new StayAliveRules(2);

        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        GameParameters out = new GameParameters(paths, rules);
        writeGameParameters(out, buffer);
        GameParameters in = readGameParameters(buffer);

        assertEquals(out, in);
    }

    private GameParameters readGameParameters(ByteBuf buffer)
    {
        int pathCount = buffer.readInt();
        ArrayList<Path> paths = new ArrayList<>(pathCount);
        for (int i = 0; i < pathCount; i++)
        {
            paths.add(readPath(buffer));
        }

        int ruleNameLength = buffer.readInt();
        byte[] ruleNameBytes = new byte[ruleNameLength];
        buffer.readBytes(ruleNameBytes);
        String ruleName = new String(ruleNameBytes, StandardCharsets.UTF_8);
        if (ruleName.equals("StayAlive"))
        {
            return new GameParameters(paths, new StayAliveRules(buffer.readDouble()));
        }
        else
        {
            throw new IllegalStateException("Unsupported game type: " + ruleName);
        }
    }

    private Path readPath(ByteBuf buffer)
    {
        int vertexCount = buffer.readInt();
        ArrayList<Segment> segments = new ArrayList<>(vertexCount - 1);
        Vertex head = new Vertex(readLatLn(buffer));
        for (int i = 1; i < vertexCount; i++)
        {
            Vertex tail = new Vertex(readLatLn(buffer));
            segments.add(new Segment(head, tail));
            head = tail;
        }

        return new Path(segments);
    }

    private void writeGameParameters(GameParameters gameParameters, ByteBuf buffer)
    {
        buffer.writeInt(gameParameters.paths.size());
        gameParameters.paths.forEach(p -> {
            buffer.writeInt(p.segments().size() + 1);
            Segment last = null;
            for (Segment s : p.segments())
            {
                writeLatLn(s.head.location, buffer);
                last = s;
            }
            writeLatLn(last.tail.location, buffer);
        });
        String gameType = gameParameters.rules.gameType();
        byte[] bytes = gameType.getBytes(StandardCharsets.UTF_8);
        buffer.writeInt(bytes.length);
        buffer.writeBytes(bytes);
        buffer.writeDouble(gameParameters.rules.sentrySpeed());
    }

    private UnitVector readUnitVector(ByteBuf buffer)
    {
        double dLat = buffer.readDouble();
        double dLon = buffer.readDouble();
        return new UnitVector(dLat, dLon);
    }

    private LatLn readLatLn(ByteBuf buffer)
    {
        double lat = buffer.readDouble();
        double lon = buffer.readDouble();
        return new LatLn(lat, lon);
    }
}