package net.digihippo.cryptnet.server;

import io.netty.buffer.ByteBuf;
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
                clientToServer.newSession();
                return;
            case 1:
                clientToServer.resumeSession(readString(byteBuf));
                return;
            case 2:
                clientToServer.onLocation(readLatLn(byteBuf));
                return;
            case 3:
                clientToServer.requestGame();
                return;
            case 4:
                clientToServer.startGame(readString(byteBuf));
                return;
            case 5:
                clientToServer.resumeGame();
                return;
            case 6:
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
                String sessionKey = readString(byteBuf);
                boolean gameInProgress = readBoolean(byteBuf);
                serverToClient.sessionEstablished(sessionKey, gameInProgress);
                return;
            }
            case 1:
            {
                StayAliveRules stayAliveRules = readRules(byteBuf);
                serverToClient.rules(stayAliveRules);
                return;
            }
            case 2:
            {
                serverToClient.path(readPath(byteBuf));
                return;
            }
            case 3:
            {
                String gameId = readString(byteBuf);
                serverToClient.gameReady(gameId);
                return;
            }
            case 4:
            {
                serverToClient.gameStarted();
                return;
            }
            case 5:
            {
                Frame frame = readFrame(byteBuf);
                serverToClient.onFrame(frame);
                return;
            }
            case 6:
            {
                String errorCode = readString(byteBuf);
                serverToClient.error(errorCode);
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
            public void newSession()
            {
                sender.withByteBuf(byteBuf -> byteBuf.writeByte(0));
            }

            @Override
            public void resumeSession(String sessionId)
            {
                sender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(1);
                    writeString(sessionId, byteBuf);
                });
            }

            @Override
            public void onLocation(LatLn location)
            {
                sender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(2);
                    writeLatLn(location, byteBuf);
                });
            }

            @Override
            public void requestGame()
            {
                sender.withByteBuf(byteBuf -> byteBuf.writeByte(3));
            }

            @Override
            public void startGame(String gameId)
            {
                sender.withByteBuf(byteBuf ->
                {
                    byteBuf.writeByte(4);
                    writeString(gameId, byteBuf);
                });
            }

            @Override
            public void resumeGame()
            {
                sender.withByteBuf(byteBuf -> byteBuf.writeByte(5));
            }

            @Override
            public void quit()
            {
                sender.withByteBuf(byteBuf -> byteBuf.writeByte(6));
            }
        };
    }

    static ServerToClient serverToClient(MessageSender messageSender)
    {
        return new ServerToClient()
        {
            @Override
            public void sessionEstablished(String sessionKey, boolean gameInProgress)
            {
                messageSender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(0);
                    writeString(sessionKey, byteBuf);
                    writeBoolean(gameInProgress, byteBuf);
                });
            }

            @Override
            public void rules(StayAliveRules rules)
            {
                messageSender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(1);
                    writeRules(rules, byteBuf);
                });
            }

            @Override
            public void path(Path path)
            {
                messageSender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(2);
                    writePath(path, byteBuf);
                });
            }

            @Override
            public void gameReady(String gameId)
            {
                messageSender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(3);
                    writeString(gameId, byteBuf);
                });
            }

            @Override
            public void gameStarted()
            {
                messageSender.withByteBuf(byteBuf -> byteBuf.writeByte(4));
            }

            @Override
            public void onFrame(Frame frame)
            {
                messageSender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(5);
                    write(frame, byteBuf);
                });
            }

            @Override
            public void error(String errorCode)
            {
                messageSender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(6);
                    writeString(errorCode, byteBuf);
                });
            }
        };
    }


    private static boolean readBoolean(ByteBuf byteBuf)
    {
        return byteBuf.readBoolean();
    }

    private static String readString(ByteBuf byteBuf)
    {
        short gameIdLength = byteBuf.readShort();
        byte[] gameIdBytes = new byte[gameIdLength];
        byteBuf.readBytes(gameIdBytes);
        return new String(gameIdBytes, StandardCharsets.UTF_8);
    }

    private static StayAliveRules readRules(ByteBuf buffer)
    {
        String ruleName = readString(buffer);
        if (!ruleName.equals("StayAlive"))
        {
            throw new UnsupportedOperationException("Unsupported rule type: " + ruleName);
        }
        int sentryCount = buffer.readInt();
        double initialSentryDistance = buffer.readDouble();
        double sentrySpeed = buffer.readDouble();
        int gameDuration = buffer.readInt();
        return new StayAliveRules(sentryCount, initialSentryDistance, sentrySpeed, gameDuration);
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

    static Frame readFrame(ByteBuf buffer)
    {
        int frameCounter = buffer.readInt();
        Frame frame = new Frame(frameCounter);
        frame.victory = buffer.readBoolean();
        frame.gameOver = buffer.readBoolean();
        frame.playerLocation = readLatLn(buffer);
        int joinCount = buffer.readInt();
        for (int i = 0; i < joinCount; i++)
        {
            LatLn latLn = readLatLn(buffer);
            UnitVector dir = readUnitVector(buffer);
            LatLn connection = readLatLn(buffer);

            JoiningView jv = new JoiningView(latLn, dir, connection);
            frame.joining.add(jv);
        }

        int patrolCount = buffer.readInt();
        for (int i = 0; i < patrolCount; i++)
        {
            LatLn latLn = readLatLn(buffer);
            UnitVector dir = readUnitVector(buffer);

            PatrolView pv = new PatrolView(latLn, dir);
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

    static void write(Frame frame, ByteBuf byteBuf)
    {
        byteBuf.writeInt(frame.frameCounter);
        byteBuf.writeBoolean(frame.victory);
        byteBuf.writeBoolean(frame.gameOver);
        writeLatLn(frame.playerLocation, byteBuf);
        byteBuf.writeInt(frame.joining.size());
        frame.joining.forEach(jv -> {
            writeLatLn(jv.location, byteBuf);
            writeUnitVector(jv.orientation, byteBuf);
            writeLatLn(jv.connectionLocation, byteBuf);
        });
        byteBuf.writeInt(frame.patrols.size());
        frame.patrols.forEach(p -> {
            writeLatLn(p.location, byteBuf);
            writeUnitVector(p.orientation, byteBuf);
        });
    }

    private static void writePath(Path path, ByteBuf buffer)
    {
        buffer.writeInt(path.segments().size() + 1);
        Segment last = null;
        for (Segment s : path.segments())
        {
            writeLatLn(s.head.location, buffer);
            last = s;
        }
        assert last != null;
        writeLatLn(last.tail.location, buffer);
    }

    private static void writeRules(StayAliveRules rules, ByteBuf buffer)
    {
        writeString(rules.gameType(), buffer);
        buffer.writeInt(rules.sentryCount());
        buffer.writeDouble(rules.initialSentryDistance());
        buffer.writeDouble(rules.sentrySpeed());
        buffer.writeInt(rules.gameDuration());
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

    private static void writeUnitVector(UnitVector unitVector, ByteBuf byteBuf)
    {
        byteBuf.writeDouble(unitVector.dLat);
        byteBuf.writeDouble(unitVector.dLon);
    }

    private static void writeBoolean(boolean b, ByteBuf byteBuf)
    {
        byteBuf.writeBoolean(b);
    }
}
