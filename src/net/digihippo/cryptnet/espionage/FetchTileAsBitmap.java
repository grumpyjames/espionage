package net.digihippo.cryptnet.espionage;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import net.digihippo.cryptnet.dimtwo.Pixel;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.Map;

final class FetchTileAsBitmap extends AsyncTask<Pixel, String, Map<Pixel, Bitmap>>
{
    private final int zoom;
    private final TileListener callback;

    FetchTileAsBitmap(
        final int zoom,
        final TileListener callback)
    {
        this.zoom = zoom;
        this.callback = callback;
    }

    @Override
    protected Map<Pixel, Bitmap> doInBackground(Pixel... pixels)
    {
        final Map<Pixel, Bitmap> result = new HashMap<>();

        for (Pixel pixel : pixels)
        {
            try
            {
                // or https://c.osm.rrze.fau.de/osmhd
                // String baseUrl = "https://tile.osmand.net/hd";
                String baseUrl = "https://c.osm.rrze.fau.de/osmhd";
                URL tileUrl =
                    new URL(baseUrl + "/" + zoom + "/" + pixel.x + "/" + pixel.y + ".png");

                Log.w(EspionageActivity.ESPIONAGE_TAG, "Requesting " + tileUrl);
                URLConnection urlConnection = tileUrl.openConnection();

                result.put(pixel, BitmapFactory.decodeStream(urlConnection.getInputStream()));
            }
            catch (IOException e)
            {
                Log.w(EspionageActivity.ESPIONAGE_TAG, e);
                return null;
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Map<Pixel, Bitmap> bitmap)
    {
        callback.onTiles(bitmap);
    }
}
