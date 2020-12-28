package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;

public class StayAliveRules implements Rules
{
    private final double sentrySpeed;

    public StayAliveRules(double sentrySpeed)
    {
        this.sentrySpeed = sentrySpeed;
    }

    @Override
    public double sentrySpeed()
    {
        return sentrySpeed;
    }

    @Override
    public State gameState(
            long gameDurationMillis,
            LatLn playerLocation,
            List<LatLn> sentryLocations)
    {
        if (gameDurationMillis >= 30_000)
        {
            return State.Victory;
        }
        for (LatLn sentryLocation : sentryLocations)
        {
            double v = sentryLocation.distanceTo(playerLocation);
            if (v <= 2)
            {
                return State.GameOver;
            }
        }

        return State.Continue;
    }
}
