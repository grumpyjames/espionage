package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.Objects;

public class StayAliveRules
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

    public int sentryCount()
    {
        return sentryCount;
    }

    public double initialSentryDistance()
    {
        return initialSentryDistance;
    }

    public double sentrySpeed()
    {
        return sentrySpeed;
    }

    public State gameState(
            long gameDurationMillis,
            LatLn playerLocation,
            Iterable<Patrol> patrols)
    {
        if (gameDurationMillis >= this.gameDurationMillis)
        {
            return State.Victory;
        }
        for (Patrol patrol : patrols)
        {
            double v = patrol.location.distanceTo(playerLocation);
            if (v <= 2)
            {
                return State.GameOver;
            }
        }

        return State.Continue;
    }

    public int gameDuration()
    {
        return gameDurationMillis;
    }

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
