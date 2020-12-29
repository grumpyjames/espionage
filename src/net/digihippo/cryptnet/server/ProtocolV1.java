package net.digihippo.cryptnet.server;

import io.netty.buffer.ByteBuf;
import net.digihippo.cryptnet.roadmap.LatLn;

import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class ProtocolV1
{
    void dispatch(ByteBuf byteBuf, ClientToServer clientToServer)
    {
        byte methodIndex = byteBuf.readByte();
        switch (methodIndex)
        {
            case 0:
                double lat = byteBuf.readDouble();
                double lon = byteBuf.readDouble();
                clientToServer.onLocation(new LatLn(lat, lon));
                return;
            case 1:
                clientToServer.requestGame();
                return;
            case 2:
                short gameIdLength = byteBuf.readShort();
                byte[] gameIdBytes = new byte[gameIdLength];
                byteBuf.readBytes(gameIdBytes);
                String gameId = new String(gameIdBytes, StandardCharsets.UTF_8);
                clientToServer.startGame(gameId);
                return;
            case 3:
                clientToServer.quit();
                return;
            default:
                throw new UnsupportedOperationException("Method not found: " + methodIndex);
        }
    }

    interface MessageSender
    {
        void withByteBuf(final Consumer<ByteBuf> byteBuf);
    }

    static ClientToServer encoder(MessageSender sender)
    {
        return new ClientToServer()
        {
            @Override
            public void onLocation(LatLn location)
            {
                sender.withByteBuf(byteBuf -> {
                    byteBuf.writeByte(0);
                    byteBuf.writeDouble(location.lat);
                    byteBuf.writeDouble(location.lon);
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
                    byte[] gameIdBytes = gameId.getBytes(StandardCharsets.UTF_8);
                    byteBuf.writeByte(3);
                    byteBuf.writeShort(gameIdBytes.length);
                    byteBuf.writeBytes(gameIdBytes);
                });
            }

            @Override
            public void quit()
            {
                sender.withByteBuf(byteBuf -> byteBuf.writeByte(3));
            }
        };
    }
}
