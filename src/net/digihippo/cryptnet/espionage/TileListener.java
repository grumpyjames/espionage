package net.digihippo.cryptnet.espionage;

import android.graphics.Bitmap;
import net.digihippo.cryptnet.dimtwo.Pixel;

import java.util.Map;

interface TileListener
{
    void onTiles(final Map<Pixel, Bitmap> tiles);
}
