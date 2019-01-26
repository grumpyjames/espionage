package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.dimtwo.*;

import java.util.ArrayList;
import java.util.List;

public class DeferredModelActions implements ModelActions
{
    private final List<Patrol> incoming = new ArrayList<>();
    private final List<JoiningSentry> outgoing = new ArrayList<>();

    @Override
    public void joined(
        JoiningSentry sentry, Pixel pixel, DoublePoint point,
        Path path, Line line, DoublePoint delta, Direction direction)
    {
        outgoing.add(sentry);
        incoming.add(new Patrol(path, line, delta, point, direction));
    }

    void enact(final Model model)
    {
        model.removeJoining(outgoing);
        model.addPatrols(incoming);
    }

}
