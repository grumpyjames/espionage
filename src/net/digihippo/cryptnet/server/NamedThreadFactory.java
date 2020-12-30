package net.digihippo.cryptnet.server;

import java.util.concurrent.ThreadFactory;

public class NamedThreadFactory implements ThreadFactory
{
    private final String name;

    public NamedThreadFactory(String name)
    {
        this.name = name;
    }

    @Override
    public Thread newThread(Runnable runnable)
    {
        Thread thread = new Thread(runnable);
        thread.setName(name);
        return thread;
    }
}
