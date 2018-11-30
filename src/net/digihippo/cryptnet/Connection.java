package net.digihippo.cryptnet;

class Connection
{
    final Point connectionPoint;
    final Line line;
    final double distance;

    Connection(Point point, Point connectionPoint, Line line)
    {
        this.connectionPoint = connectionPoint;
        this.line = line;
        this.distance = Point.distanceBetween(point, connectionPoint);
    }
}
