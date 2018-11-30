package net.digihippo.cryptnet;

class IntersectionEntry
{
    final Line line;
    final boolean startsHere;
    final boolean endsHere;

    IntersectionEntry(Line line, boolean startsHere, boolean endsHere)
    {
        this.line = line;
        this.startsHere = startsHere;
        this.endsHere = endsHere;
    }

    @SuppressWarnings("SimplifiableIfStatement") // you generated it, intellij!
    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        IntersectionEntry that = (IntersectionEntry) o;

        if (startsHere != that.startsHere) return false;
        if (endsHere != that.endsHere) return false;
        return !(line != null ? !line.equals(that.line) : that.line != null);

    }

    @Override
    public int hashCode()
    {
        int result = line != null ? line.hashCode() : 0;
        result = 31 * result + (startsHere ? 1 : 0);
        result = 31 * result + (endsHere ? 1 : 0);
        return result;
    }
}
