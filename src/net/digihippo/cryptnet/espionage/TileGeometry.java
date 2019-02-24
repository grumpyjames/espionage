package net.digihippo.cryptnet.espionage;

import net.digihippo.cryptnet.roadmap.OsmSource;

final class TileGeometry
{
    int xOffset;
    int yOffset;
    final int xTileOrigin;
    final int yTileOrigin;
    final int columnCount;
    final int rowCount;
    final double latitude;
    final double longitude;

    private TileGeometry(
        int xOffset,
        int yOffset,
        int xTileOrigin,
        int yTileOrigin,
        int columnCount,
        int rowCount,
        double latitude,
        double longitude)
    {
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xTileOrigin = xTileOrigin;
        this.yTileOrigin = yTileOrigin;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    static TileGeometry centeredAt(
        final int screenWidth,
        final int screenHeight,
        final int zoom,
        final int tileSize,
        final double latitude,
        final double longitude
    )
    {
        final int columnCount = (screenWidth / tileSize) + 3;
        final int rowCount = (screenHeight / tileSize) + 3;

        final int x = (int) OsmSource.x(longitude, zoom, tileSize);
        final int y = (int) OsmSource.y(latitude, zoom, tileSize);

        final int xTile = x / tileSize;
        final int yTile = y / tileSize;
        final int xPixel = (x % tileSize);
        final int yPixel = (y % tileSize);

        final int xOff = (columnCount / 2) * tileSize;
        final int xOffset = screenWidth / 2 - (xOff + xPixel);
        final int yOff = (rowCount / 2) * tileSize;
        final int yOffset = screenHeight / 2 - (yOff + yPixel);

        final int xTileOrigin = (xTile - (columnCount / 2));
        final int yTileOrigin = (yTile - (rowCount / 2));

        return new TileGeometry(
            xOffset,
            yOffset,
            xTileOrigin,
            yTileOrigin,
            columnCount,
            rowCount,
            latitude,
            longitude
        );
    }

    @Override
    public String toString()
    {
        return "TileGeometry{" +
            "xOffset=" + xOffset +
            ", yOffset=" + yOffset +
            ", xTileOrigin=" + xTileOrigin +
            ", yTileOrigin=" + yTileOrigin +
            ", columnCount=" + columnCount +
            ", rowCount=" + rowCount +
            ", latitude=" + latitude +
            ", longitude=" + longitude +
            '}';
    }

    void onDrag(float dx, float dy)
    {
        this.xOffset -= dx;
        this.yOffset -= dy;
    }
}
