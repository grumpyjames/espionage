package net.digihippo.cryptnet;

class IntersectionEntry
{
    final Path path;
    final Line line;
    final Direction direction;
    final boolean startsHere;
    final boolean endsHere;

    public IntersectionEntry(Path path, Line line, Direction direction, boolean startsAt, boolean endsAt)
    {
        this.path = path;
        this.line = line;
        this.direction = direction;
        this.startsHere = startsAt;
        this.endsHere = endsAt;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntersectionEntry that = (IntersectionEntry) o;

        if (startsHere != that.startsHere) return false;
        if (endsHere != that.endsHere) return false;
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
        result = 31 * result + (startsHere ? 1 : 0);
        result = 31 * result + (endsHere ? 1 : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return "IntersectionEntry{" +
            "path=" + path +
            ", line=" + line +
            ", startsHere=" + startsHere +
            ", endsHere=" + endsHere +
            '}';
    }
}
