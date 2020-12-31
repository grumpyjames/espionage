package net.digihippo.cryptnet.server;

import io.netty.buffer.ByteBuf;
import net.digihippo.cryptnet.format.FrameWriter;
import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.function.Consumer;

public class ProtocolV1
{
    void dispatch(ByteBuf byteBuf, ClientToServer clientToServer)
    {
        byte methodIndex = byteBuf.readByte();
        switch (methodIndex)
        {
            case 0:
                clientToServer.onLocation(readLatLn(byteBuf));
                return;
            case 1:
                clientToServer.requestGame();
                return;
            case 2:
                clientToServer.startGame(readString(byteBuf));
                return;
            case 3:
                clientToServer.quit();
                return;
            default:
                throw new UnsupportedOperationException("Method not found: " + methodIndex);
        }
    }

    void dispatch(ByteBuf byteBuf, ServerToClient serverToClient)
    {
        byte methodIndex = byteBuf.readByte();
        switch (methodIndex)
        {
            case 0:
            {
                String gameId = readString(byteBuf);
                GameParameters gameParameters = readGameParameters(byteBuf);
                serverToClient.gameReady(gameId, gameParameters);
                return;
            }
            case 1:
            {
                serverToClient.gameStarted();
                return;
            }
            case 2:
            {
                FrameCollector.Frame frame = readFrame(byteBuf);
                serverToClient.onFrame(frame);
                return;
            }
            default:
                throw new UnsupportedOperationException("Method not found: " + methodIndex);
        }
    }

    interface MessageSender
    {
        void withByteBuf(final Consumer<ByteBuf> byteBuf);
    }

    static ClientToServer clientToServer(MessageSender sender)
    {
        return new ClientToServer()
        {
            @Override
            public void onLocation(LatLn location)
            {
                sender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(0);
                    writeLatLn(location, byteBuf);
                });
            }

            @Override
            public void requestGame()
            {
                sender.withByteBuf(byteBuf -> byteBuf.writeByte(1));
            }

            @Override
            public void startGame(String gameId)
            {
                sender.withByteBuf(byteBuf ->
                {
                    byteBuf.writeByte(2);
                    writeString(gameId, byteBuf);
                });
            }

            @Override
            public void quit()
            {
                sender.withByteBuf(byteBuf -> byteBuf.writeByte(3));
            }
        };
    }

    static ServerToClient serverToClient(MessageSender messageSender)
    {
        return new ServerToClient()
        {
            @Override
            public void gameReady(String gameId, GameParameters gameParameters)
            {
                messageSender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(0);
                    writeString(gameId, byteBuf);
                    writeGameParameters(gameParameters, byteBuf);
                });
            }

            @Override
            public void gameStarted()
            {
                messageSender.withByteBuf(byteBuf -> byteBuf.writeByte(1));
            }

            @Override
            public void onFrame(FrameCollector.Frame frame)
            {
                messageSender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(2);
                    FrameWriter.write(frame, byteBuf);
                });
            }
        };
    }

    private static String readString(ByteBuf byteBuf)
    {
        short gameIdLength = byteBuf.readShort();
        byte[] gameIdBytes = new byte[gameIdLength];
        byteBuf.readBytes(gameIdBytes);
        return new String(gameIdBytes, StandardCharsets.UTF_8);
    }

    private static GameParameters readGameParameters(ByteBuf buffer)
    {
        int pathCount = buffer.readInt();
        ArrayList<Path> paths = new ArrayList<>(pathCount);
        for (int i = 0; i < pathCount; i++)
        {
            paths.add(readPath(buffer));
        }

        String ruleName = readString(buffer);
        if (ruleName.equals("StayAlive"))
        {
            int sentryCount = buffer.readInt();
            double initialSentryDistance = buffer.readDouble();
            double sentrySpeed = buffer.readDouble();
            int gameDuration = buffer.readInt();
            StayAliveRules rules = new StayAliveRules(sentryCount, initialSentryDistance, sentrySpeed, gameDuration);
            return new GameParameters(paths, rules);
        }
        else
        {
            throw new IllegalStateException("Unsupported game type: " + ruleName);
        }
    }

    private static Path readPath(ByteBuf buffer)
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

    private static LatLn readLatLn(ByteBuf buffer)
    {
        double lat = buffer.readDouble();
        double lon = buffer.readDouble();
        return new LatLn(lat, lon);
    }

    private static FrameCollector.Frame readFrame(ByteBuf buffer)
    {
        int frameCounter = buffer.readInt();
        FrameCollector.Frame frame = new FrameCollector.Frame(frameCounter);
        frame.victory = buffer.readBoolean();
        frame.gameOver = buffer.readBoolean();
        frame.playerLocation = readLatLn(buffer);
        int joinCount = buffer.readInt();
        for (int i = 0; i < joinCount; i++)
        {
            LatLn latLn = readLatLn(buffer);
            UnitVector dir = readUnitVector(buffer);
            LatLn connection = readLatLn(buffer);

            FrameCollector.JoiningView jv = new FrameCollector.JoiningView(latLn, dir, connection);
            frame.joining.add(jv);
        }

        int patrolCount = buffer.readInt();
        for (int i = 0; i < patrolCount; i++)
        {
            LatLn latLn = readLatLn(buffer);
            UnitVector dir = readUnitVector(buffer);

            FrameCollector.PatrolView pv = new FrameCollector.PatrolView(latLn, dir);
            frame.patrols.add(pv);
        }

        return frame;
    }

    private static UnitVector readUnitVector(ByteBuf buffer)
    {
        double dLat = buffer.readDouble();
        double dLon = buffer.readDouble();
        return new UnitVector(dLat, dLon);
    }

    private static void writeGameParameters(GameParameters gameParameters, ByteBuf buffer)
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
            assert last != null;
            writeLatLn(last.tail.location, buffer);
        });
        writeString(gameParameters.rules.gameType(), buffer);
        buffer.writeInt(gameParameters.rules.sentryCount());
        buffer.writeDouble(gameParameters.rules.initialSentryDistance());
        buffer.writeDouble(gameParameters.rules.sentrySpeed());
        buffer.writeInt(gameParameters.rules.gameDuration());
    }

    private static void writeString(String gameId, ByteBuf byteBuf)
    {
        byte[] gameIdBytes = gameId.getBytes(StandardCharsets.UTF_8);
        byteBuf.writeShort(gameIdBytes.length);
        byteBuf.writeBytes(gameIdBytes);
    }

    private static void writeLatLn(LatLn location, ByteBuf byteBuf)
    {
        byteBuf.writeDouble(location.lat);
        byteBuf.writeDouble(location.lon);
    }
}
