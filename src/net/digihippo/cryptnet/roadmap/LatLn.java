package net.digihippo.cryptnet.roadmap;

import java.util.Objects;

public final class LatLn
{
    public static final double EARTH_RADIUS_METRES = 6_371_000D;
    // Radians!
    public final double lat;
    public final double lon;

    public LatLn(double lat, double lon)
    {
        assert(Math.abs(lat) <= Math.PI && Math.abs(lon) <= (Math.PI/2));
        this.lat = lat;
        this.lon = lon;
    }

    public static LatLn toRads(double latDegs, double lonDegs)
    {
        return new LatLn(Math.toRadians(latDegs), Math.toRadians(lonDegs));
    }

    @Override
    public String toString()
    {
        return "( lat: " + lat + ", lon: " + lon + ")";
    }

    public LatLn applyTo(LatLn location)
    {
        return new LatLn(
                location.lat + this.lat,
                location.lon + this.lon);

    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LatLn latLn = (LatLn) o;
        return Double.compare(latLn.lat, lat) == 0 &&
                Double.compare(latLn.lon, lon) == 0;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(lat, lon);
    }

    public boolean sameAs(LatLn another)
    {
        return this.lat == another.lat && this.lon == another.lon;
    }

    // Metres.
    public double distanceTo(LatLn position)
    {
        // Haversine distance
        // Assumes spherical earth
        // Will work for now
        // Better things from libraries later
        double dlon = position.lon - this.lon;
        double dlat = position.lat - this.lat;
        double a = Math.pow(Math.sin(dlat / 2), 2)
                + Math.cos(this.lat) * Math.cos(position.lat)
                * Math.pow(Math.sin(dlon / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));

        // calculate the result
        return (c * EARTH_RADIUS_METRES);
    }

    public UnitVector directionFrom(LatLn another)
    {
        double distance = this.distanceTo(another);
        return new UnitVector(
                (this.lat - another.lat) / distance,
                (this.lon - another.lon) / distance);
    }

    public LatLn move(double distance, double bearing)
    {
        double distanceRatio = distance / EARTH_RADIUS_METRES;
        double lat2 = Math.asin(
                Math.sin(lat) * Math.cos(distanceRatio) +
                Math.cos(lat) * Math.sin(distanceRatio) * Math.cos(bearing));
        double lon2 = lon +
                Math.atan2(
                        Math.sin(bearing) * Math.sin(distanceRatio) * Math.cos(lat),
                        Math.cos(distanceRatio) - Math.sin(lat) * Math.sin(lat2));

        return new LatLn(lat2, lon2);
    }

    public BoundingBox boundingBox(double distance)
    {
        return new BoundingBox(
                this.move(distance, 7 * Math.PI/4),
                this.move(distance, Math.PI/4),
                this.move(distance, 5 * Math.PI/4),
                this.move(distance, 3 * Math.PI/4));
    }

    public static final class BoundingBox
    {
        public final LatLn nw;
        public final LatLn ne;
        public final LatLn sw;
        public final LatLn se;

        private BoundingBox(LatLn nw, LatLn ne, LatLn sw, LatLn se)
        {
            this.nw = nw;
            this.ne = ne;
            this.sw = sw;
            this.se = se;
        }
    }
}
