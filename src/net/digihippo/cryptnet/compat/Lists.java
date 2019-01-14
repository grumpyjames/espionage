package net.digihippo.cryptnet.compat;

import java.util.ArrayList;
import java.util.List;

public class Lists
{
    private static <T> List<T> reverse(List<T> l)
    {
        ArrayList<T> objects = new ArrayList<>(l.size());
        for (int i = l.size() - 1; i >= 0; i--)
        {
             objects.add(l.get(i));
        }
        return objects;
    }

    public static boolean palindromic(final List<?> l)
    {
        return l.equals(reverse(l));
    }
}
