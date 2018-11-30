package net.digihippo.cryptnet;

class Connection
{
    final Point connectionPoint;
    final double distance;

    Connection(Point point, Point connectionPoint)
    {
        this.connectionPoint = connectionPoint;
        this.distance = Point.distanceBetween(point, connectionPoint);
    }
}
