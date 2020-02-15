package net.digihippo.cryptnet.roadmap;

public final class LatLn
{
    public final double lat;
    public final double lon;

    public LatLn(double lat, double lon)
    {
        this.lat = lat;
        this.lon = lon;
    }

    @Override
    public String toString()
    {
        return "( lat: " + lat + ", lon: " + lon + ")";
    }

    public LatLn applyTo(LatLn location) {
        return new LatLn(
                location.lat + this.lat,
                location.lon + this.lon);

}
    public boolean sameAs(LatLn another) {
        return this.lat == another.lat && this.lon == another.lon;
    }

    // Metres.
    public double distanceTo(LatLn position) {
        // Haversine distance
        // Assumes spherical earth
        // Will work for now
        // Better things from libraries later
        double dlon = position.lon - this.lon;
        double dlat = position.lat - this.lat;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(this.lat) * Math.cos(position.lat)
                * Math.pow(Math.sin(dlon / 2),2);

        double c = 2 * Math.asin(Math.sqrt(a));

        double r = 6_371_000;

        // calculate the result
        return(c * r);
    }

    public LatLn directionFrom(LatLn another) {
        return new LatLn(
                this.lat - another.lat,
                this.lon - another.lon);
    }
}
