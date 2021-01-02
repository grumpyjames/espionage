package net.digihippo.cryptnet.server;

import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.model.Model;
import net.digihippo.cryptnet.model.Paths;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Way;

import java.io.IOException;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.function.BiConsumer;

// This is a potentially blocking operation
//   It needs to happen off event loop, and then call back to the event loop with results.
final class GamePreparationService
{
    private final ExecutorService executor;
    private final VectorSource vectorSource;
    private final StayAliveRules rules;
    private final ExecutorService workerGroup;

    GamePreparationService(
            ExecutorService offEventLoop,
            VectorSource vectorSource,
            StayAliveRules rules,
            ExecutorService eventLoop)
    {
        this.executor = offEventLoop;
        this.vectorSource = vectorSource;
        this.rules = rules;
        this.workerGroup = eventLoop;
    }

    void prepareGame(LatLn latln, BiConsumer<Model, FrameDispatcher> modelReady)
    {
        executor.execute(() ->
        {
            try
            {
                LatLn.BoundingBox boundingBox = latln.boundingBox(1_000);
                Collection<Way> ways = vectorSource.fetchWays(boundingBox);
                FrameDispatcher dispatcher = new FrameDispatcher();
                Model model = Model.createModel(
                        Paths.from(ways),
                        rules,
                        new Random(),
                        new FrameCollector(dispatcher));
                workerGroup.submit(() -> modelReady.accept(model, dispatcher));
            }
            catch (IOException e)
            {
                // FIXME: tell the event loop about the failure
                e.printStackTrace();
            }
        });
    }
}
