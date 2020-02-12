package net.digihippo.cryptnet.api;

public class Geobox {
    public final Location nw;
    public final Location se;

    public Geobox(Location nw, Location se) {
        this.nw = nw;
        this.se = se;
    }
}
