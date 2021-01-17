package net.digihippo.cryptnet.server;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.Path;
import net.digihippo.cryptnet.model.StayAliveRules;

import java.io.*;
import java.nio.channels.FileChannel;
import java.time.Instant;

public final class Journal
{
    public static ServerToClient forGame(
            File baseDirectory,
            String gameId,
            long currentTimeMillis)
    {
        try
        {
            File file = new File(baseDirectory, Instant.ofEpochMilli(currentTimeMillis) + "-" + gameId + ".log");
            RandomAccessFile raf = new RandomAccessFile(file, "rw");
            ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.heapBuffer(65536);
            ServerToClient serverToClient = ProtocolV1.serverToClient(byteBuf ->
            {
                buffer.resetWriterIndex();
                buffer.resetReaderIndex();
                byteBuf.accept(buffer);

                writeToFile(raf, buffer);
            });

            return new CloseOnComplete(serverToClient, raf);
        }
        catch (FileNotFoundException fnfe)
        {
            throw new RuntimeException(fnfe);
        }
    }

    public static void main(String[] args)
    {
        playJournal(new File(args[0]), new ServerToClient()
        {
            @Override
            public void sessionEstablished(String sessionKey)
            {
                System.out.println(sessionKey);
            }

            @Override
            public void rules(StayAliveRules rules)
            {
                System.out.println(rules);
            }

            @Override
            public void path(Path path)
            {
                System.out.println(path);
            }

            @Override
            public void gameReady(String gameId)
            {
                System.out.println(gameId);
            }

            @Override
            public void gameStarted()
            {
                System.out.println("Game started");
            }

            @Override
            public void onFrame(FrameCollector.Frame frame)
            {
                System.out.println(frame);
            }

            @Override
            public void error(String errorCode)
            {
                System.out.println(errorCode);
            }
        });
    }

    public static void playJournal(File file, ServerToClient serverToClient)
    {
        ProtocolV1 protocolV1 = new ProtocolV1();
        ByteBuf byteBuf = UnpooledByteBufAllocator.DEFAULT.heapBuffer(2048);

        try (FileInputStream fis = new FileInputStream(file))
        {
            int position = 0;
            FileChannel channel = fis.getChannel();
            long limit = channel.size();
            while (position < limit)
            {
                byteBuf.resetReaderIndex();
                byteBuf.resetWriterIndex();
                byteBuf.writeBytes(channel, position, 4);
                position += 4;
                int length = byteBuf.readInt();

                byteBuf.resetReaderIndex();
                byteBuf.resetWriterIndex();

                byteBuf.writeBytes(channel, position, length);
                position += length;

                protocolV1.dispatch(byteBuf, serverToClient);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static void writeToFile(RandomAccessFile raf, ByteBuf buffer)
    {
        try
        {
            // We don't have netty's magic length prefixing here, so we do it ourselves
            raf.writeInt(buffer.readableBytes());
            raf.write(buffer.array(), buffer.arrayOffset(), buffer.readableBytes());
        } catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private static final class CloseOnComplete implements ServerToClient
    {
        private final ServerToClient serverToClient;
        private final RandomAccessFile raf;

        public CloseOnComplete(ServerToClient serverToClient, RandomAccessFile raf)
        {
            this.serverToClient = serverToClient;
            this.raf = raf;
        }

        @Override
        public void rules(StayAliveRules rules)
        {
            serverToClient.rules(rules);
        }

        @Override
        public void path(Path path)
        {
            serverToClient.path(path);
        }

        @Override
        public void gameReady(String gameId)
        {
            serverToClient.gameReady(gameId);
        }

        @Override
        public void gameStarted()
        {
            serverToClient.gameStarted();
        }

        @Override
        public void onFrame(FrameCollector.Frame frame)
        {
            serverToClient.onFrame(frame);
            if (frame.gameOver || frame.victory)
            {
                try
                {
                    raf.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void sessionEstablished(String sessionKey)
        {
            serverToClient.sessionEstablished(sessionKey);
        }

        @Override
        public void error(String errorCode)
        {
            serverToClient.error(errorCode);
        }
    }
}
