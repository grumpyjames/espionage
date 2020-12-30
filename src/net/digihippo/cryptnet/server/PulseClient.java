package net.digihippo.cryptnet.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class PulseClient
{
    public static void main(String[] args) throws IOException
    {
        DatagramChannel channel = DatagramChannel.open();
        InetSocketAddress socketAddress = new InetSocketAddress(
                InetAddress.getByAddress(new byte[]{127, 0, 0, 1}),
                7891);

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(
                () -> sendHeartbeat(channel, socketAddress),
                0, 25, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }

    private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);

    private static void sendHeartbeat(DatagramChannel ch, InetSocketAddress recipient)
    {
        try
        {
            ch.send(EMPTY, recipient);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
