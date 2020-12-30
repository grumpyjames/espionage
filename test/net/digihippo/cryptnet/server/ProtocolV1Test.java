package net.digihippo.cryptnet.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.GameParameters;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.nio.charset.StandardCharsets;
import java.util.Collections;

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
        buffer.writeByte(0);
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
            oneOf(clientToServer).onLocation(new LatLn(0.45D, 0.56D));
            oneOf(clientToServer).requestGame();
            oneOf(clientToServer).startGame("foo");
            oneOf(clientToServer).quit();
        }});
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

        final FrameCollector.Frame frame = new FrameCollector.Frame(1);
        frame.playerLocation = new LatLn(0.34, 0.11);

        mockery.checking(new Expectations() {{
            oneOf(serverToClient).gameReady("foo", new GameParameters(Collections.emptyList(), new StayAliveRules(1, 2, 2.2)));
            oneOf(serverToClient).gameStarted();
            oneOf(serverToClient).onFrame(frame);
        }});

        encoder.gameReady("foo", new GameParameters(Collections.emptyList(), new StayAliveRules(1, 2, 2.2)));
        encoder.gameStarted();
        encoder.onFrame(frame);
    }

    @Test
    public void dispatchRequestGame()
    {
        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        // length, followed by header
        buffer.writeByte(1);
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
        buffer.writeByte(2);
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
        buffer.writeByte(3);

        mockery.checking(new Expectations() {{
            oneOf(clientToServer).quit();
        }});

        ProtocolV1 protocolV1 = new ProtocolV1();
        protocolV1.dispatch(buffer, clientToServer);
    }
}