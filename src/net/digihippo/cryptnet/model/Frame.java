package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public final class Frame
{
    public final int frameCounter;
    public boolean gameOver;
    public boolean victory;
    public LatLn playerLocation;
    public List<JoiningView> joining = new ArrayList<>();
    public List<PatrolView> patrols = new ArrayList<>();

    public Frame(int frameCounter)
    {
        this.frameCounter = frameCounter;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Frame frame = (Frame) o;
        return frameCounter == frame.frameCounter &&
                gameOver == frame.gameOver &&
                victory == frame.victory &&
                Objects.equals(playerLocation, frame.playerLocation) &&
                Objects.equals(joining, frame.joining) &&
                Objects.equals(patrols, frame.patrols);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(frameCounter, gameOver, victory, playerLocation, joining, patrols);
    }

    @Override
    public String toString()
    {
        return "Frame{" +
                "frameCounter=" + frameCounter +
                ", gameOver=" + gameOver +
                ", victory=" + victory +
                ", playerLocation=" + playerLocation +
                ", joining=" + joining +
                ", patrols=" + patrols +
                '}';
    }
}
