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
import android.util.DisplayMetrics;
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

class TileGeometry
{
    final int xOffset;
    final int yOffset;
    final int xTileOrigin;
    final int yTileOrigin;
    final int columnCount;
    final int rowCount;
    final double latitude;
    final double longitude;

    TileGeometry(
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

class Pieces
{
    final Model model;
    final TileGeometry tileGeometry;

    Pieces(Model model, TileGeometry tileGeometry)
    {
        this.model = model;
        this.tileGeometry = tileGeometry;
    }
}


interface ModelListener
{
    void onModel(final Pieces model);
}

public class EspionageActivity
    extends Activity
    implements TileListener, ModelListener
{
    private static final int ZOOM = 17;
    private static final String ESPIONAGE_LOADING = "espionage-loading";
    private Pieces model;
    private Map<Pixel, Bitmap> tiles = new HashMap<>();

    private AlertDialog alertDialog;
    private boolean initialized;
    private int height;
    private int width;
    private int requiredTiles = 0;
    private ModelView modelView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;

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

    interface OffsetListener
    {
        void offsetsCalculated(TileGeometry geometry);
    }

    private void calculateOffsets(
        final int screenWidth,
        final int screenHeight,
        final int tileSize,
        final double latitude,
        final double longitude,
        OffsetListener offsetListener
    )
    {
        final int columnCount = (screenWidth / tileSize) + 3;
        final int rowCount = (screenHeight / tileSize) + 3;

        final int x = (int) OsmSource.x(longitude, ZOOM, tileSize);
        final int y = (int) OsmSource.y(latitude, ZOOM, tileSize);

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

        /*
        Calculated geometry:
            TileGeometry{
                xOffset=16758670,
                yOffset=11146491,
                xTileOrigin=65466,
                yTileOrigin=43544,
                columnCount=5,
                rowCount=6,
                latitude=0.900099681656737,
                longitude=-0.003238280403491775}
         */

        offsetListener.offsetsCalculated(new TileGeometry(
            xOffset,
            yOffset,
            xTileOrigin,
            yTileOrigin,
            columnCount,
            rowCount,
            latitude,
            longitude
        ));
    }

    @SuppressWarnings("NullableProblems")
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

        assert locationManager != null;
        locationManager.requestLocationUpdates(
            LocationManager.NETWORK_PROVIDER, 60000, 100, locationListener);

        locationManager.requestLocationUpdates(
            LocationManager.GPS_PROVIDER, 5000, 5, locationListener);
    }

    private boolean locationSeen = false;

    private void onLocation(final Location location)
    {
        final double latitude = Math.toRadians(location.getLatitude());
        final double longitude = Math.toRadians(location.getLongitude());

        Log.w(ESPIONAGE_LOADING, "Received location " + location);
        if (locationSeen)
        {
            if (initialized)
            {
                modelView.onLocationChanged(latitude, longitude);
            }
            return;
        }

        locationSeen = true;
        alertDialog.setMessage("Waiting for geography...");

        calculateOffsets(width, height, 512, latitude, longitude, new OffsetListener()
        {
            @Override
            public void offsetsCalculated(TileGeometry geometry)
            {
                Log.w(ESPIONAGE_LOADING, "Calculated geometry: " + geometry);
                new CreateInitialModel(EspionageActivity.this, geometry).execute();
            }
        });
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
    public void onModel(Pieces model)
    {
        requiredTiles = model.tileGeometry.columnCount * model.tileGeometry.rowCount;
        onMapProgress(0);

        Log.w(ESPIONAGE_LOADING, "Model created");
        this.model = model;
        for (int i = 0; i < model.tileGeometry.columnCount; i++) {
            for (int j = 0; j < model.tileGeometry.rowCount; j++) {
                new FetchTileBitmap(this)
                    .execute(
                        new Pixel(
                            model.tileGeometry.xTileOrigin + i,
                            model.tileGeometry.yTileOrigin + j));
            }
        }
    }

    private void onMapProgress(int count)
    {
        alertDialog.setMessage("Requesting map data (" + count + "/" + requiredTiles + ")");
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

    private static class CreateInitialModel extends AsyncTask<Void, String, Pieces>
    {
        private final ModelListener callback;
        private final TileGeometry geometry;

        private CreateInitialModel(
            ModelListener callback, TileGeometry geometry)
        {
            this.callback = callback;
            this.geometry = geometry;
        }

        @Override
        protected Pieces doInBackground(Void... voids)
        {
            // tile coords increase as latitude decreases <- really?
            double latitudeMin = OsmSource.lat((geometry.yTileOrigin + geometry.rowCount) * 256, ZOOM);
            double latitudeMax = OsmSource.lat(geometry.yTileOrigin * 256, ZOOM);

            // tile coords increase with longitude
            double longitudeMin = OsmSource.lon(geometry.xTileOrigin * 256, ZOOM);
            double longitudeMax = OsmSource.lon((geometry.xTileOrigin + geometry.columnCount) * 256, ZOOM);

            try
            {
                Model model = startingModel(
                    OsmSource.fetchWays(latitudeMin, latitudeMax, longitudeMin, longitudeMax, 512D, 0, 0),
                    1024,
                    1024
                );

                setPlayerLocation(geometry, model, geometry.latitude, geometry.longitude);

                return new Pieces(model, geometry);
            } catch (IOException e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Pieces model)
        {
            callback.onModel(model);
        }
    }


    private static void setPlayerLocation(
        TileGeometry geometry,
        Model model,
        double latitude,
        double longitude)
    {
        final double xOrigin = geometry.xTileOrigin * 512;
        final double yOrigin = geometry.yTileOrigin * 512;

        int playerX =
            (int) (OsmSource.x(longitude, ZOOM, 512) - xOrigin);
        int playerY =
            (int) (OsmSource.y(latitude, ZOOM, 512) - yOrigin);

        Log.w(ESPIONAGE_LOADING, "Adding player at (" + playerX + ", " + playerY + ")");
        model.setPlayerLocation(playerX, playerY);
    }

    private void tryPromote()
    {
        if (model != null && tiles.size() == requiredTiles)
        {
            initialized = true;
            alertDialog.hide();
            Log.w(ESPIONAGE_LOADING, "Initializing!");
            modelView = new ModelView(this, model, tiles);
            setContentView(modelView);
            modelView.setBackgroundColor(Color.BLACK);
        }
    }

    private static class ModelView extends View implements View.OnClickListener, View.OnTouchListener
    {
        private final Model model;
        private final Map<Pixel, Bitmap> tiles;
        private final Random random = new Random(234234234L);
        private final TileGeometry geometry;
        private Paint paint;           // The paint (e.g. style, color) used for drawing

        // Constructor
        ModelView(
            Context context,
            Pieces pieces,
            Map<Pixel, Bitmap> tiles) {
            super(context);
            this.model = pieces.model;
            this.geometry = pieces.tileGeometry;
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
                int left = geometry.xOffset + (key.x - geometry.xTileOrigin) * tileSize;
                int top = geometry.yOffset + (key.y - geometry.yTileOrigin) * tileSize;
                canvas.drawBitmap(value, left, top, paint);
            }

            for (JoiningSentry sentry : model.joiningSentries) {
                final Pixel renderable = sentry.position.round();
                final DoublePoint direction = sentry.delta;
                renderSentry(renderable, direction, canvas);

                drawLine(
                    canvas,
                    renderable.x,
                    renderable.y,
                    Maths.round(sentry.connection.connectionPoint.x),
                    Maths.round(sentry.connection.connectionPoint.y));
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
                    geometry.xOffset + round.x - radius,
                    geometry.yOffset + round.y - radius,
                    geometry.xOffset + round.x + radius,
                    geometry.yOffset + round.y + radius,
                    paint);
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
            drawLine(g, tx1, ty1, tx2, ty2);
            drawLine(g, ux1, uy1, ux2, uy2);
        }

        private void drawLine(Canvas g, int tx1, int ty1, int tx2, int ty2)
        {
            g.drawLine(
                geometry.xOffset + tx1,
                geometry.yOffset + ty1,
                geometry.xOffset + tx2,
                geometry.yOffset + ty2,
                paint);
        }

        private void drawCircle(Pixel renderable, Canvas g, int radius)
        {
            g.drawOval(
                geometry.xOffset + renderable.x - radius,
                geometry.yOffset + renderable.y - radius,
                geometry.xOffset + renderable.x + radius,
                geometry.yOffset + renderable.y + radius,
                paint);
        }

        float lastTouchX = 0;
        float lastTouchY = 0;

        @Override
        public void onClick(View v)
        {
            model.click(
                Math.round(lastTouchX) - geometry.xOffset,
                Math.round(lastTouchY) - geometry.yOffset);
        }

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            lastTouchX = event.getX();
            lastTouchY = event.getY();

            return false;
        }

        void onLocationChanged(double latitude, double longitude)
        {
            setPlayerLocation(geometry, model, latitude, longitude);
        }
    }
}
