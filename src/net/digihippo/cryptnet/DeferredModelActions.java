package net.digihippo.cryptnet;

import java.util.ArrayList;
import java.util.List;

public class DeferredModelActions implements ModelActions
{
    private final List<Patrol> incoming = new ArrayList<>();
    private final List<JoiningSentry> outgoing = new ArrayList<>();

    @Override
    public void joined(
        JoiningSentry sentry, Point pixel, DoublePoint point,
        Path path, Line line, DoublePoint delta, Direction direction)
    {
        outgoing.add(sentry);
        incoming.add(new Patrol(point, path, line, delta, direction));
    }

    void enact(final Model model)
    {
        model.removeJoining(outgoing);
        model.addPatrols(incoming);
    }

}
