package net.digihippo.cryptnet;

enum Direction
{
    Forwards,
    Backwards;

    public DoublePoint orient(DoublePoint direction)
    {
        if (this == Backwards)
        {
            return direction.flip();
        }

        return direction;
    }
}
