package net.digihippo.cryptnet.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class PulseClient implements Runnable
{
    private final DatagramChannel datagramChannel;
    private final SocketAddress socketAddress;

    private PulseClient(DatagramChannel datagramChannel, SocketAddress socketAddress)
    {
        this.datagramChannel = datagramChannel;
        this.socketAddress = socketAddress;
    }

    public static PulseClient defaultPulseClient() throws IOException
    {
        DatagramChannel channel = DatagramChannel.open();
        InetSocketAddress socketAddress = new InetSocketAddress(
                InetAddress.getByAddress(new byte[]{127, 0, 0, 1}),
                7891);
        return new PulseClient(channel, socketAddress);
    }

    public static void main(String[] args) throws IOException
    {
        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

        PulseClient pulseClient = defaultPulseClient();

        executor.scheduleAtFixedRate(pulseClient, 0, 25, TimeUnit.MILLISECONDS);

        Runtime.getRuntime().addShutdownHook(new Thread(executor::shutdown));
    }

    private static final ByteBuffer EMPTY = ByteBuffer.allocate(0);

    @Override
    public void run()
    {
        try
        {
            datagramChannel.send(EMPTY, socketAddress);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
