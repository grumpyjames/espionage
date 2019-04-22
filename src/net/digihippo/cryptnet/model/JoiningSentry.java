package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.dimtwo.Connection;
import net.digihippo.cryptnet.dimtwo.Direction;
import net.digihippo.cryptnet.dimtwo.DoublePoint;
import net.digihippo.cryptnet.dimtwo.Pixel;

public final class JoiningSentry
{
    final String identifier;
    public final Connection connection;
    public final DoublePoint delta;
    public DoublePoint position;

    JoiningSentry(
        String identifier,
        Connection connection,
        DoublePoint position,
        DoublePoint delta)
    {
        this.identifier = identifier;
        this.connection = connection;
        this.position = position;
        this.delta = delta;
    }

    @Override
    public String toString()
    {
        return identifier + " moving " + delta + " from position " + position + " to " + connection;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoiningSentry that = (JoiningSentry) o;

        if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;
        if (connection != null ? !connection.equals(that.connection) : that.connection != null) return false;
        if (delta != null ? !delta.equals(that.delta) : that.delta != null) return false;
        return !(position != null ? !position.equals(that.position) : that.position != null);

    }

    @Override
    public int hashCode()
    {
        int result = identifier != null ? identifier.hashCode() : 0;
        result = 31 * result + (connection != null ? connection.hashCode() : 0);
        result = 31 * result + (delta != null ? delta.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }

    void tick(
        final ModelActions modelActions, Model.Events events)
    {
        this.position = this.position.plus(delta);

        final Iterable<Pixel> pixels = this.position.pixelBounds();
        for (Pixel pixel : pixels)
        {
            Pixel rounded = this.connection.connectionPoint.round();
            if (pixel.equals(rounded))
            {
                this.position = pixel.asDoublePoint();
                // FIXME: what if we join at start/end?
                final Direction direction =
                    connection.line.endsAt(pixel) ?
                        Direction.Backwards : Direction.Forwards;
                modelActions.joined(
                    this,
                    pixel,
                    this.connection.connectionPoint,
                    this.connection.getPath(),
                    this.connection.line,
                    direction.orient(this.connection.line.direction()),
                    direction);
                break;
            }
        }

        events.sentryPositionChanged(identifier, this.position, this.delta);
    }

    static JoiningSentry parse(String string)
    {
        String[] parts = string.split(" from position ");
        String[] along = parts[0].split(" moving ");
        DoublePoint delta = DoublePoint.parse(along[1]);

        String[] moreParts = parts[1].split(" to ");
        DoublePoint position = DoublePoint.parse(moreParts[0]);
        return new JoiningSentry(
            along[0],
            Connection.parse(moreParts[1]),
            position,
            delta);
    }
}
