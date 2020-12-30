package net.digihippo.cryptnet.model;

public interface FrameConsumer
{
    void gameStarted();
    void onFrame(FrameCollector.Frame frame);
}
