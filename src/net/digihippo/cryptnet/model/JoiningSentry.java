package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.dimtwo.Connection;
import net.digihippo.cryptnet.dimtwo.Direction;
import net.digihippo.cryptnet.dimtwo.DoublePoint;
import net.digihippo.cryptnet.dimtwo.Pixel;

public final class JoiningSentry
{
    public final Connection connection;
    public DoublePoint position;
    public final DoublePoint delta;

    JoiningSentry(
        Connection connection,
        DoublePoint position,
        DoublePoint delta)
    {
        this.connection = connection;
        this.position = position;
        this.delta = delta;
    }

    @Override
    public String toString()
    {
        return "Moving " + delta + " from position " + position + " to " + connection;
    }

    @SuppressWarnings({"SimplifiableIfStatement", "EqualsReplaceableByObjectsCall"})
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        JoiningSentry that = (JoiningSentry) o;

        if (connection != null ? !connection.equals(that.connection) : that.connection != null) return false;
        if (position != null ? !position.equals(that.position) : that.position != null) return false;
        return !(delta != null ? !delta.equals(that.delta) : that.delta != null);

    }

    @Override
    public int hashCode()
    {
        int result = connection != null ? connection.hashCode() : 0;
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (delta != null ? delta.hashCode() : 0);
        return result;
    }

    void tick(
        final ModelActions modelActions)
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
    }

    static JoiningSentry parse(String string)
    {
        String[] parts = string.split(" from position ");
        DoublePoint delta = DoublePoint.parse(parts[0].substring("Moving ".length()));

        String[] moreParts = parts[1].split(" to ");
        DoublePoint position = DoublePoint.parse(moreParts[0]);
        return new JoiningSentry(
            Connection.parse(moreParts[1]),
            position,
            delta);
    }
}
