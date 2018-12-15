package net.digihippo.cryptnet;

import java.util.ArrayList;
import java.util.List;

class IntersectionEntry
{
    final Path path;
    final Line line;
    final Direction direction;

    public IntersectionEntry(Path path, Line line, Direction direction)
    {
        this.path = path;
        this.line = line;
        this.direction = direction;
    }

    @SuppressWarnings("SimplifiableIfStatement")
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntersectionEntry that = (IntersectionEntry) o;

        if (path != null ? !path.equals(that.path) : that.path != null) return false;
        if (line != null ? !line.equals(that.line) : that.line != null) return false;
        return direction == that.direction;

    }

    @Override
    public int hashCode()
    {
        int result = path != null ? path.hashCode() : 0;
        result = 31 * result + (line != null ? line.hashCode() : 0);
        result = 31 * result + (direction != null ? direction.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return direction + " along " + path.highlighting(line);
    }

    public static IntersectionEntry parse(String string)
    {
        String[] parts = string.split(" along ");
        Direction direction = Direction.valueOf(parts[0]);
        HighlightedLine highlightedLine = HighlightedLine.parse(parts[1]);

        return new IntersectionEntry(highlightedLine.path, highlightedLine.line, direction);
    }


}
