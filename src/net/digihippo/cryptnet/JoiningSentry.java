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
        this.delta = connection.connectionPoint.asDoublePoint().minus(this.point).over(50);
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
            if (pixel.equals(this.connection.connectionPoint))
            {
                this.delta = this.line.direction();
                this.point = pixel.asDoublePoint();
                // FIXME: what if we join at start/end?
                modelActions.joined(
                    this,
                    pixel,
                    this.connection.line,
                    this.connection.line.direction().toUnit());
                break;
            }
        }
    }
}
