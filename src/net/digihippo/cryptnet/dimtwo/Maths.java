package net.digihippo.cryptnet.dimtwo;

public final class Maths
{
    static int floor(double d)
    {
        return (int) java.lang.Math.floor(d);
    }

    static int ceil(double d)
    {
        return (int) java.lang.Math.ceil(d);
    }

    public static int round(double d)
    {
        return (int) java.lang.Math.round(d);
    }

    private Maths() {}
}
