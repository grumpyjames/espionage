package net.digihippo.cryptnet;

public interface ModelActions
{
    void joined(JoiningSentry sentry, Pixel pixel, DoublePoint point, Path path, Line line, DoublePoint delta, Direction direction);
}
