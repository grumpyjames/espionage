package net.digihippo.cryptnet.model;

import java.util.List;
import java.util.Objects;

// Things that are constant for the duration of a game
public class GameParameters
{
    public final List<Path> paths;
    public final Rules rules;

    public GameParameters(List<Path> paths, Rules rules)
    {
        this.paths = paths;
        this.rules = rules;
    }

    @Override
    public String toString()
    {
        return "GameParameters{" +
                "paths=" + paths +
                ", rules=" + rules +
                '}';
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GameParameters that = (GameParameters) o;
        return Objects.equals(paths, that.paths) &&
                Objects.equals(rules, that.rules);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(paths, rules);
    }
}
