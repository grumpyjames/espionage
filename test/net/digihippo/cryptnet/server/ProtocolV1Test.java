package net.digihippo.cryptnet.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.digihippo.cryptnet.model.*;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Ignore;
import org.junit.Rule;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

import static org.junit.Assert.assertEquals;

public class ProtocolV1Test
{
    @Rule
    public final JUnitRuleMockery mockery = new JUnitRuleMockery();

    private final ClientToServer clientToServer = mockery.mock(ClientToServer.class);
    private final ServerToClient serverToClient = mockery.mock(ServerToClient.class);

    @Test
    public void dispatchLocation()
    {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeByte(2);
        buffer.writeDouble(0.45D);
        buffer.writeDouble(0.56D);

        mockery.checking(new Expectations() {{
            oneOf(clientToServer).onLocation(new LatLn(0.45D, 0.56D));
        }});

        ProtocolV1 protocolV1 = new ProtocolV1();
        protocolV1.dispatch(buffer, clientToServer);
    }

    @Test
    public void roundTripClientToServer()
    {
        ProtocolV1 protocolV1 = new ProtocolV1();

        ClientToServer encoder = ProtocolV1.clientToServer(byteBuf ->
        {
            ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
            byteBuf.accept(buffer);
            protocolV1.dispatch(buffer, clientToServer);
        });

        mockery.checking(new Expectations() {{
            oneOf(clientToServer).newSession();
            oneOf(clientToServer).resumeSession("fiddlydee");
            oneOf(clientToServer).onLocation(new LatLn(0.45D, 0.56D));
            oneOf(clientToServer).requestGame();
            oneOf(clientToServer).startGame("foo");
            oneOf(clientToServer).quit();
        }});
        encoder.newSession();
        encoder.resumeSession("fiddlydee");
        encoder.onLocation(new LatLn(0.45D, 0.56D));
        encoder.requestGame();
        encoder.startGame("foo");
        encoder.quit();
    }

    @Test
    public void roundTripServerToClient()
    {
        ProtocolV1 protocolV1 = new ProtocolV1();

        ServerToClient encoder = ProtocolV1.serverToClient(byteBuf ->
        {
            ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
            byteBuf.accept(buffer);
            protocolV1.dispatch(buffer, serverToClient);
        });

        final Frame frame = new Frame(1);
        frame.playerLocation = new LatLn(0.34, 0.11);

        Path path = new Path(Collections.singletonList(
                new Segment(new Vertex(LatLn.toRads(45.2323, 32.22)), new Vertex(LatLn.toRads(34.222, 44.333)))));

        mockery.checking(new Expectations() {{
            oneOf(serverToClient).sessionEstablished("wooohooo", true);
            oneOf(serverToClient).rules(new StayAliveRules(1, 2, 4, 5));
            oneOf(serverToClient).path(path);
            oneOf(serverToClient).gameReady("foo");
            oneOf(serverToClient).gameStarted();
            oneOf(serverToClient).onFrame(frame);
        }});

        encoder.sessionEstablished("wooohooo", true);
        encoder.rules(new StayAliveRules(1, 2, 4, 5));
        encoder.path(path);
        encoder.gameReady("foo");
        encoder.gameStarted();
        encoder.onFrame(frame);
    }

    @Test
    public void dispatchRequestGame()
    {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeByte(3);
        mockery.checking(new Expectations() {{
            oneOf(clientToServer).requestGame();
        }});

        ProtocolV1 protocolV1 = new ProtocolV1();
        protocolV1.dispatch(buffer, clientToServer);
    }

    @Test
    public void dispatchStartGame()
    {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        String gameId = "game11";
        byte[] gameIdBytes = gameId.getBytes(StandardCharsets.UTF_8);
        buffer.writeByte(4);
        buffer.writeShort(gameIdBytes.length);
        buffer.writeBytes(gameIdBytes);

        mockery.checking(new Expectations() {{
            oneOf(clientToServer).startGame(gameId);
        }});

        ProtocolV1 protocolV1 = new ProtocolV1();
        protocolV1.dispatch(buffer, clientToServer);
    }

    @Test
    public void dispatchQuit()
    {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        buffer.writeByte(6);

        mockery.checking(new Expectations() {{
            oneOf(clientToServer).quit();
        }});

        ProtocolV1 protocolV1 = new ProtocolV1();
        protocolV1.dispatch(buffer, clientToServer);
    }

    @Test
    @Ignore("this is handy for generating hex data for tests in other projects")
    public void hexy()
    {
        Frame frame = makeFrame();

        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        ProtocolV1.write(frame, buffer);

        System.out.println(ByteBufUtil.hexDump(buffer));
    }

    @Test
    public void roundTrip()
    {
        Frame frame = makeFrame();

        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        ProtocolV1.write(frame, buffer);

        Frame read = ProtocolV1.readFrame(buffer);

        assertEquals(read, frame);
    }

    private Frame makeFrame()
    {
        Frame frame = new Frame(2342);
        double latDegs = 43.5635644D;
        double lonDegs = 12.23453436D;
        frame.playerLocation = LatLn.toRads(latDegs, lonDegs);
        frame.gameOver = true;
        frame.victory = false;
        frame.joining.add(new JoiningView(
                LatLn.toRads(46.243466D, 45.5253443D),
                new UnitVector(0.004D, 0.0001D),
                LatLn.toRads(16.243466D, 15.53D)
        ));
        frame.joining.add(new JoiningView(
                LatLn.toRads(76.273766D, 75.5253743D),
                new UnitVector(0.001D, 0.0004D),
                LatLn.toRads(26.243466D, 25.53D)
        ));
        frame.patrols.add(new PatrolView(
                LatLn.toRads(86.283866D, 85.5253843D),
                new UnitVector(0.003D, 0.0002D)
        ));
        frame.patrols.add(new PatrolView(
                LatLn.toRads(66.263666D, 65.5253643D),
                new UnitVector(0.002D, 0.0003D)
        ));
        frame.patrols.add(new PatrolView(
                LatLn.toRads(66.269666D, 65.5259649D),
                new UnitVector(0.001D, 0.0009D)
        ));
        return frame;
    }
}