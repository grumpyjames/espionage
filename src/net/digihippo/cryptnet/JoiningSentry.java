package net.digihippo.cryptnet;

final class JoiningSentry
{
    final Connection connection;

    Line line;
    DoublePoint delta;
    DoublePoint point;

    public JoiningSentry(Point point, Connection connection)
    {
        this.point = point.asDoublePoint();
        this.delta = connection.connectionPoint.minus(this.point).over(50);
        this.connection = connection;
        this.line = connection.line;
    }

    public void tick(
        final ModelActions modelActions)
    {
        this.point = this.point.plus(delta);

        final Iterable<Point> pixels = this.point.pixelBounds();
        for (Point pixel : pixels)
        {
            Point rounded = this.connection.connectionPoint.round();
            if (pixel.equals(rounded))
            {
                this.delta = this.line.direction();
                this.point = pixel.asDoublePoint();
                // FIXME: what if we join at start/end?
                final Direction direction =
                    line.endsAt(pixel) ? Direction.Backwards : line.startsAt(pixel) ? Direction.Forwards : Direction.Backwards;
                modelActions.joined(
                    this,
                    pixel,
                    this.connection.connectionPoint,
                    this.connection.path,
                    this.connection.line,
                    direction.orient(this.connection.line.direction()),
                    direction);
                break;
            }
        }
    }
}
