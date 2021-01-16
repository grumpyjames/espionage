package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.ArrayList;
import java.util.List;

class DeferredModelActions implements ModelActions
{
    private final List<Patrol> incoming = new ArrayList<>();
    private final List<JoiningSentry> outgoing = new ArrayList<>();

    void enact(final Model model, Model.Events events)
    {
        model.removeJoining(outgoing);
        model.addPatrols(incoming);
    }

    @Override
    public void joined(
            JoiningSentry sentry,
            LatLn location,
            Vertex.Link link)
    {
        outgoing.add(sentry);
        incoming.add(new Patrol(
                sentry.identifier,
                sentry.speed,
                link.path,
                link.segment,
                link.segment.direction(),
                location,
                link.end == Vertex.End.Head ? Direction.Forwards : Direction.Backwards
        ));
    }
}
