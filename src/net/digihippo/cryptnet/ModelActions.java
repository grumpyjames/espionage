package net.digihippo.cryptnet;

public interface ModelActions
{
    void joined(JoiningSentry sentry, Point pixel, DoublePoint point, Path path, Line line, DoublePoint delta, Direction direction);
}
