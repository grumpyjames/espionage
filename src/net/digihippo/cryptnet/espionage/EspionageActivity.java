package net.digihippo.cryptnet.espionage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.*;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import net.digihippo.cryptnet.dimtwo.Path;
import net.digihippo.cryptnet.dimtwo.*;
import net.digihippo.cryptnet.model.JoiningSentry;
import net.digihippo.cryptnet.model.Model;
import net.digihippo.cryptnet.model.Patrol;
import net.digihippo.cryptnet.roadmap.NormalizedWay;
import net.digihippo.cryptnet.roadmap.OsmSource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

interface TileListener
{
    void onTiles(final Map<Pixel, Bitmap> tiles);
}

interface ModelListener
{
    void onModel(final Model model);
}

public class EspionageActivity
    extends Activity
    implements TileListener, ModelListener
{
    private static final int ZOOM = 17;
    protected static final String ESPIONAGE_LOADING = "espionage-loading";
    private Model model;
    private Map<Pixel, Bitmap> tiles = new HashMap<>();
    private int xTile = 0;
    private int yTile = 0;

    private AlertDialog alertDialog;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Waiting for GPS location");
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        builder.setView(progressBar);

        alertDialog = builder.create();
        alertDialog.show();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
        {
            if (
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)
            {
                requestPermissions(new String[] {
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                }, 1);
            }
            else
            {
                requestLocation();
            }
        }
        else
        {
            requestLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        requestLocation();
    }

    @SuppressLint("MissingPermission")
    private void requestLocation()
    {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        LocationListener locationListener = new LocationListener()
        {
            public void onLocationChanged(Location location)
            {
                // Called when a new location is found by the network location provider.
                onLocation(location);
            }

            public void onStatusChanged(String provider, int status, Bundle extras)
            {
            }

            public void onProviderEnabled(String provider)
            {
            }

            public void onProviderDisabled(String provider)
            {
            }
        };

        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, 0, 0, locationListener);
    }

    private void onLocation(final Location location)
    {
        Log.w(ESPIONAGE_LOADING, "Received location " + location);
        alertDialog.setMessage("Waiting for geography...");
        if (xTile != 0)
        {
            return;
        }

        double latitude = Math.toRadians(location.getLatitude());
        double longitude = Math.toRadians(location.getLongitude());

        double x = OsmSource.x(longitude, ZOOM, 256) / 256;
        double y = OsmSource.y(latitude, ZOOM, 256) / 256;

        this.xTile = Maths.floor(x);
        this.yTile = Maths.floor(y);

        new CreateInitialModel(this, xTile, yTile, latitude, longitude).execute();
    }

    @Override
    public void onTiles(Map<Pixel, Bitmap> tiles)
    {
        Log.w(ESPIONAGE_LOADING, "Loaded " + tiles.size() + " tiles");
        this.tiles.putAll(tiles);
        onMapProgress(this.tiles.size());
        this.tryPromote();
    }

    @Override
    public void onModel(Model model)
    {
        onMapProgress(0);

        Log.w(ESPIONAGE_LOADING, "Model created");
        this.model = model;
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 4; j++) {
                new FetchTileBitmap(this).execute(new Pixel(xTile + i, yTile + j));
            }
        }
    }

    private void onMapProgress(int count)
    {
        alertDialog.setMessage("Requesting map data (" + count + "/12)");
    }

    @SuppressWarnings("SameParameterValue")
    private static Model startingModel(
        List<NormalizedWay> normalizedWays,
        int width,
        int height)
    {
        final List<Path> paths = new ArrayList<>();

        for (NormalizedWay normalizedWay : normalizedWays)
        {
            int lineCount = normalizedWay.doublePoints.size() - 1;
            final List<Line> pieces = new ArrayList<>(lineCount);
            for (int i = 0; i < lineCount; i++)
            {
                Pixel start =
                    normalizedWay.doublePoints.get(i).round();
                Pixel end = normalizedWay.doublePoints.get(i + 1).round();
                Line line = Line.createLine(start.x, end.x, start.y, end.y);
                if (!start.equals(end))
                {
                    pieces.add(line);
                }
            }
            paths.add(new Path(pieces));
        }

        return Model.createModel(paths, width, height);
    }

    private static class FetchTileBitmap extends AsyncTask<Pixel, String, Map<Pixel, Bitmap>>
    {
        private final TileListener callback;

        private FetchTileBitmap(EspionageActivity callback)
        {
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
                    URL tileUrl =
                        new URL("https://tile.osmand.net/hd/" + ZOOM + "/" + pixel.x + "/" + pixel.y + ".png");

                    Log.w(ESPIONAGE_LOADING, "Requesting " + tileUrl);
                    URLConnection urlConnection = tileUrl.openConnection();

                    result.put(pixel, BitmapFactory.decodeStream(urlConnection.getInputStream()));
                }
                catch (IOException e)
                {
                    Log.w(ESPIONAGE_LOADING, e);
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

    private static class CreateInitialModel extends AsyncTask<Void, String, Model>
    {
        private final ModelListener callback;
        private final int yTile;
        private final int xTile;
        private final double latitude;
        private final double longitude;

        private CreateInitialModel(
            ModelListener callback,
            int xTile,
            int yTile,
            double latitude,
            double longitude)
        {
            this.callback = callback;
            this.yTile = yTile;
            this.xTile = xTile;
            this.latitude = latitude;
            this.longitude = longitude;
        }

        @Override
        protected Model doInBackground(Void... voids)
        {
            // tile coords increase as latitude decreases <- really?
            double latitudeMin = OsmSource.lat((yTile + 4) * 256, ZOOM);
            double latitudeMax = OsmSource.lat(yTile * 256, ZOOM);

            // tile coords increase with longitude
            double longitudeMin = OsmSource.lon(xTile * 256, ZOOM);
            double longitudeMax = OsmSource.lon((xTile + 3) * 256, ZOOM);

            try
            {
                Model model = startingModel(
                    OsmSource.fetchWays(latitudeMin, latitudeMax, longitudeMin, longitudeMax, 512D),
                    1024,
                    1024
                );

                final double xOrigin = xTile * 512;
                final double yOrigin = yTile * 512;
                OsmSource.x(longitude, ZOOM, 512);
                OsmSource.y(latitude, ZOOM, 512);

                int playerX = (int) (OsmSource.x(longitude, ZOOM, 512) - xOrigin);
                int playerY = (int) (OsmSource.y(latitude, ZOOM, 512) - yOrigin);
                Log.w(ESPIONAGE_LOADING, "Adding player at (" + playerX + ", " + playerY + ")");
                model.addPlayer(playerX, playerY);

                return model;
            } catch (IOException e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Model model)
        {
            callback.onModel(model);
        }
    }

    private void tryPromote()
    {
        if (model != null && tiles.size() == 12)
        {
            alertDialog.hide();
            Log.w(ESPIONAGE_LOADING, "Initializing!");
            View modelView = new ModelView(this, xTile, yTile, model, tiles);
            setContentView(modelView);
            modelView.setBackgroundColor(Color.BLACK);
        }
    }

    private static class ModelView extends View implements View.OnClickListener, View.OnTouchListener
    {
        private final int xTile;
        private final int yTile;
        private final Model model;
        private final Map<Pixel, Bitmap> tiles;
        private final Random random = new Random(234234234L);
        private Paint paint;           // The paint (e.g. style, color) used for drawing

        // Constructor
        ModelView(
            Context context,
            int xTile,
            int yTile,
            Model model,
            Map<Pixel, Bitmap> tiles) {
            super(context);
            this.xTile = xTile;
            this.yTile = yTile;
            this.model = model;
            this.tiles = tiles;

            paint = new Paint();

            setOnTouchListener(this);
            setOnClickListener(this);
        }

        // Called back to draw the view. Also called by invalidate().
        @Override
        protected void onDraw(Canvas canvas) {
            model.tick(random);

            for (Map.Entry<Pixel, Bitmap> pixelBitmapEntry : tiles.entrySet())
            {
                final Pixel key = pixelBitmapEntry.getKey();
                final Bitmap value = pixelBitmapEntry.getValue();
                int tileSize = 512;
                int left = (key.x - xTile) * tileSize;
                int top = (key.y - yTile) * tileSize;
                Log.w("Render", "Drawing a tile at " + left + ", " + top);
                canvas.drawBitmap(value, left, top, paint);
            }

            for (JoiningSentry sentry : model.joiningSentries) {
                final Pixel renderable = sentry.position.round();
                final DoublePoint direction = sentry.delta;
                renderSentry(renderable, direction, canvas);

                canvas.drawLine(
                    renderable.x,
                    renderable.y,
                    Maths.round(sentry.connection.connectionPoint.x),
                    Maths.round(sentry.connection.connectionPoint.y),
                    paint);
            }

            for (Patrol patrol: model.patrols)
            {
                renderSentry(patrol.point.round(), patrol.delta, canvas);
            }

            if (model.player != null)
            {
                Pixel round = model.player.position.round();
                paint.setColor(Color.MAGENTA);
                int radius = 8;
                canvas.drawOval(
                    round.x - radius, round.y - radius, round.x + radius, round.y + radius, paint);
                paint.setColor(Color.BLACK);
            }


            try {
                Thread.sleep(30);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            invalidate();  // Force a re-draw
        }

        private void renderSentry(Pixel renderable, DoublePoint direction, Canvas g)
        {
            final double orientation = direction.orientation();
            final Pixel tView = direction.rotate(Math.PI / 12).times(10).round();
            int radius = 8;
            int tx1 = (int) Math.round(renderable.x + (radius * Math.cos(orientation + (Math.PI / 2))));
            int ty1 = (int) Math.round(renderable.y + (radius * Math.sin(orientation + (Math.PI / 2))));
            int tx2 = tView.x + tx1;
            int ty2 = tView.y + ty1;

            final Pixel uView = direction.rotate(-Math.PI / 12).times(10).round();
            int ux1 = (int) Math.round(renderable.x - (radius * Math.cos(orientation + (Math.PI / 2))));
            int uy1 = (int) Math.round(renderable.y - (radius * Math.sin(orientation + (Math.PI / 2))));
            int ux2 = uView.x + ux1;
            int uy2 = uView.y + uy1;

            drawCircle(renderable, g, radius);
            g.drawLine(tx1, ty1, tx2, ty2, paint);
            g.drawLine(ux1, uy1, ux2, uy2, paint);
        }

        private void drawCircle(Pixel renderable, Canvas g, int radius)
        {
            g.drawOval(
                renderable.x - radius,
                renderable.y - radius,
                renderable.x + radius,
                renderable.y + radius,
                paint);
        }

        float lastTouchX = 0;
        float lastTouchY = 0;

        @Override
        public void onClick(View v)
        {
            model.click(Math.round(lastTouchX), Math.round(lastTouchY));
        }

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            lastTouchX = event.getX();
            lastTouchY = event.getY();

            return false;
        }
    }
}
