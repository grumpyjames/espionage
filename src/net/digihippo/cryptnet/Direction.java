package net.digihippo.cryptnet;

enum Direction
{
    Forwards {
        @Override
        public DoublePoint orient(DoublePoint direction)
        {
            return direction;
        }

        @Override
        public boolean turnsAt(Path path, int lineIndex, Point pixel)
        {
            final int nextLineIndex = lineIndex + 1;
            if (nextLineIndex >= path.lines.size())
            {
                return false;
            }

            Line line = path.lines.get(nextLineIndex);
            return line.startsAt(pixel);
        }

        @Override
        public int nextLineIndex(int lineIndex)
        {
            return lineIndex + 1;
        }
    },
    Backwards {
        @Override
        public DoublePoint orient(DoublePoint direction)
        {
            return direction.flip();
        }

        @Override
        public boolean turnsAt(Path path, int lineIndex, Point pixel)
        {
            final int nextLineIndex = lineIndex - 1;
            if (nextLineIndex < 0)
            {
                return false;
            }

            Line line = path.lines.get(nextLineIndex);
            return line.endsAt(pixel);
        }

        @Override
        public int nextLineIndex(int lineIndex)
        {
            return lineIndex - 1;
        }
    };

    public abstract DoublePoint orient(DoublePoint direction);

    public abstract boolean turnsAt(Path path, int lineIndex, Point pixel);

    public abstract int nextLineIndex(int lineIndex);
}
