package net.digihippo.cryptnet.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.digihippo.cryptnet.roadmap.LatLn;
import org.jmock.Expectations;
import org.jmock.integration.junit4.JUnitRuleMockery;
import org.junit.Rule;
import org.junit.Test;

import java.nio.charset.StandardCharsets;

public class ProtocolV1Test
{
    @Rule
    public final JUnitRuleMockery mockery = new JUnitRuleMockery();

    private final ClientToServer clientToServer = mockery.mock(ClientToServer.class);

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
    public void roundTripLocation()
    {
        ProtocolV1 protocolV1 = new ProtocolV1();

        ClientToServer encoder = ProtocolV1.encoder(byteBuf ->
        {
            ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
            byteBuf.accept(buffer);
            protocolV1.dispatch(buffer, clientToServer);
        });

        mockery.checking(new Expectations() {{
            oneOf(clientToServer).onLocation(new LatLn(0.45D, 0.56D));
        }});
        encoder.onLocation(new LatLn(0.45D, 0.56D));
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