package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;
import java.util.Objects;

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

    @Override
    public String gameType()
    {
        return "StayAlive";
    }

    @Override
    public String toString()
    {
        return "StayAliveRules{" +
                "sentrySpeed=" + sentrySpeed +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StayAliveRules that = (StayAliveRules) o;
        return Double.compare(that.sentrySpeed, sentrySpeed) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sentrySpeed);
    }
}
