package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.List;
import java.util.Objects;

public class StayAliveRules implements Rules
{
    private final int sentryCount;
    private final double initialSentryDistance;
    private final double sentrySpeed;
    private final int gameDurationMillis;

    public StayAliveRules(
            int sentryCount,
            double initialSentryDistance,
            double sentrySpeed,
            int gameDurationMillis)
    {
        this.sentryCount = sentryCount;
        this.initialSentryDistance = initialSentryDistance;
        this.sentrySpeed = sentrySpeed;
        this.gameDurationMillis = gameDurationMillis;
    }

    @Override
    public int sentryCount()
    {
        return sentryCount;
    }

    @Override
    public double initialSentryDistance()
    {
        return initialSentryDistance;
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
        if (gameDurationMillis >= this.gameDurationMillis)
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
    public int gameDuration()
    {
        return gameDurationMillis;
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
                "sentryCount=" + sentryCount +
                ", initialSentryDistance=" + initialSentryDistance +
                ", sentrySpeed=" + sentrySpeed +
                ", gameDurationMillis=" + gameDurationMillis +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StayAliveRules that = (StayAliveRules) o;
        return sentryCount == that.sentryCount &&
                Double.compare(that.initialSentryDistance, initialSentryDistance) == 0 &&
                Double.compare(that.sentrySpeed, sentrySpeed) == 0 &&
                gameDurationMillis == that.gameDurationMillis;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(sentryCount, initialSentryDistance, sentrySpeed, gameDurationMillis);
    }

}
