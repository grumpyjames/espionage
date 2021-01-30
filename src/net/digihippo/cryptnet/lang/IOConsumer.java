package net.digihippo.cryptnet.lang;

import java.io.IOException;

public interface IOConsumer<T>
{
    void accept(T t) throws IOException;
}