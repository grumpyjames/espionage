package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.dimtwo.WebMercator;

public final class TileGeometry
{
    private final int tileSize;
    public final int screenWidth;
    public final int screenHeight;
    public int xOffset;
    public int yOffset;
    public final int xTileOrigin;
    public final int yTileOrigin;
    public final int columnCount;
    public final int rowCount;
    public final double latitude;
    public final double longitude;

    private TileGeometry(
        int tileSize,
        int screenWidth,
        int screenHeight,
        int xOffset,
        int yOffset,
        int xTileOrigin,
        int yTileOrigin,
        int columnCount,
        int rowCount,
        double latitude,
        double longitude)
    {
        this.tileSize = tileSize;
        this.screenWidth = screenWidth;
        this.screenHeight = screenHeight;
        this.xOffset = xOffset;
        this.yOffset = yOffset;
        this.xTileOrigin = xTileOrigin;
        this.yTileOrigin = yTileOrigin;
        this.columnCount = columnCount;
        this.rowCount = rowCount;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public static TileGeometry centeredAt(
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

        final int x = (int) WebMercator.x(longitude, zoom, tileSize);
        final int y = (int) WebMercator.y(latitude, zoom, tileSize);

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
            tileSize,
            screenWidth,
            screenHeight,
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

    public void onDrag(float dx, float dy)
    {
        // offsets are the origin of where we start drawing map tiles.
        // if they become positive, we have non tile covered area in the left or top edges

        // For a screen sized (x, y), with a rendered tile area of (w, h),
        // can allow offsets down to... (x - w, y - h)
        this.xOffset = Math.max(screenWidth - (tileSize * columnCount), Math.min(0, this.xOffset - (int) dx));
        this.yOffset = Math.max(screenHeight - (tileSize * rowCount), Math.min(0, this.yOffset - (int) dy));
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
}
