package net.digihippo.cryptnet;

import java.util.ArrayList;
import java.util.List;

public class DeferredModelActions implements ModelActions
{
    private final List<Patrol> incoming = new ArrayList<>();
    private final List<JoiningSentry> outgoing = new ArrayList<>();

    @Override
    public void joined(
        JoiningSentry sentry, Point pixel, DoublePoint point, Line line, DoublePoint delta)
    {
        outgoing.add(sentry);
        incoming.add(new Patrol(point, line, delta));
    }

    void enact(final Model model)
    {
        model.removeJoining(outgoing);
        model.addPatrols(incoming);
    }

}
