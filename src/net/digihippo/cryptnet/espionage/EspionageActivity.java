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
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ProgressBar;
import net.digihippo.cryptnet.client.GameView;
import net.digihippo.cryptnet.compat.Consumer;
import net.digihippo.cryptnet.dimtwo.DoublePoint;
import net.digihippo.cryptnet.dimtwo.Path;
import net.digihippo.cryptnet.dimtwo.Pixel;
import net.digihippo.cryptnet.dimtwo.WebMercator;
import net.digihippo.cryptnet.model.Model;
import net.digihippo.cryptnet.model.TileGeometry;
import net.digihippo.cryptnet.roadmap.NormalizedWay;
import net.digihippo.cryptnet.roadmap.OsmSource;
import net.digihippo.cryptnet.server.Events;
import net.digihippo.cryptnet.server.Server;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

public class EspionageActivity
    extends Activity
    implements TileListener
{
    private static final int ZOOM = 17;
    static final String ESPIONAGE_TAG = "espionage";

    private AlertDialog alertDialog;
    private TileGeometry tileGeometry;
    private int requiredTiles = 0;
    private Model model;
    private Map<Pixel, Bitmap> tiles = new HashMap<>();
    private ModelView modelView;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        initialiseAlertDialog();
        alertDialog.setMessage("Waiting for GPS location");

        performLocationRequest();
    }

    private boolean tileFetchInProgress = false;

    private void onLocation(final Location location)
    {
        final double latitude = Math.toRadians(location.getLatitude());
        final double longitude = Math.toRadians(location.getLongitude());

        Log.w(ESPIONAGE_TAG, "Received location " + location);
        if (modelView != null)
        {
            modelView.onLocationChanged(latitude, longitude);
        }
        else if (!tileFetchInProgress)
        {
            tileFetchInProgress = true;

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            this.tileGeometry = TileGeometry.centeredAt(
                displayMetrics.widthPixels, displayMetrics.heightPixels, ZOOM, 512, latitude, longitude);

            Log.w(ESPIONAGE_TAG, "Calculated geometry: " + tileGeometry);

            fetchTiles(model);
        }
    }

    @Override
    public void onTiles(Map<Pixel, Bitmap> tiles)
    {
        this.tiles.putAll(tiles);
        onMapProgress(this.tiles.size());
        Log.w(ESPIONAGE_TAG, "Loaded " + tiles.keySet() + ". Progress: " + this.tiles.size() +
            " tiles of " + requiredTiles);

        if (this.tiles.size() == requiredTiles)
        {
            alertDialog.setMessage("Starting game...");
            Log.w(ESPIONAGE_TAG, "Initializing!");
            modelView = new ModelView(this, tileGeometry, this.tiles);
            setContentView(modelView);
            modelView.setBackgroundColor(Color.BLACK);
            startGame();
        }
    }

    private void startGame()
    {
        GameView theGame = new GameView("TheGame");
        RunGame runGame = new RunGame(theGame, tileGeometry);
        modelView.gameView = theGame;
        modelView.runGame = runGame;
        new Thread(runGame).start();
    }

    private void fetchTiles(Model model)
    {
        requiredTiles = tileGeometry.columnCount * tileGeometry.rowCount;
        onMapProgress(0);

        Log.w(ESPIONAGE_TAG, "Model created");
        this.model = model;
        for (int i = 0; i < tileGeometry.columnCount; i++) {
            for (int j = 0; j < tileGeometry.rowCount; j++) {
                new FetchTileAsBitmap(ZOOM, this)
                    .execute(
                        new Pixel(
                            tileGeometry.xTileOrigin + i,
                            tileGeometry.yTileOrigin + j));
            }
        }
    }

    private void onMapProgress(int count)
    {
        alertDialog.setMessage("Requesting map data (" + count + "/" + requiredTiles + ")");
    }

    private final class RunGame implements Runnable
    {
        private final Model.Events callback;
        private final TileGeometry geometry;

        private final BlockingQueue<Consumer<Server>> queue = new LinkedBlockingQueue<>();
        private String gameIdentifier;

        private RunGame(
            Model.Events callback,
            TileGeometry geometry)
        {
            this.callback = callback;
            this.geometry = geometry;
        }

        @Override
        public void run()
        {
            final AtomicBoolean gameComplete = new AtomicBoolean(false);
            Events events = new Events()
            {
                @Override
                public void gameRejected(String gameIdentifier, final String message)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            callback.gameRejected(message);
                            EspionageActivity.this.invalidate();
                        }
                    });
                }

                @Override
                public void gameStarted(String gameIdentifier)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            EspionageActivity.this.alertDialog.hide();
                            callback.gameStarted();
                            EspionageActivity.this.invalidate();
                        }
                    });
                }

                @Override
                public void playerPositionChanged(
                    String gameIdentifier,
                    final DoublePoint location)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            callback.playerPositionChanged(location);
                            EspionageActivity.this.invalidate();
                        }
                    });
                }

                @Override
                public void sentryPositionChanged(
                    String gameIdentifier,
                    final String patrolIdentifier,
                    final DoublePoint location,
                    final DoublePoint orientation)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            callback.sentryPositionChanged(patrolIdentifier, location, orientation);
                            EspionageActivity.this.invalidate();
                        }
                    });
                }

                @Override
                public void gameOver(String gameIdentifier)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            callback.gameOver();
                            EspionageActivity.this.invalidate();
                        }
                    });
                    gameComplete.set(true);
                }

                @Override
                public void victory(String gameIdentifier)
                {
                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            callback.victory();
                            EspionageActivity.this.invalidate();
                        }
                    });
                    gameComplete.set(true);
                }
            };


            // tile coords increase as latitude decreases <- really?
            double latitudeMin = WebMercator.lat((geometry.yTileOrigin + geometry.rowCount) * 256, ZOOM);
            double latitudeMax = WebMercator.lat(geometry.yTileOrigin * 256, ZOOM);

            // tile coords increase with longitude
            double longitudeMin = WebMercator.lon(geometry.xTileOrigin * 256, ZOOM);
            double longitudeMax = WebMercator.lon((geometry.xTileOrigin + geometry.columnCount) * 256, ZOOM);

            try
            {
                List<NormalizedWay> ways =
                    OsmSource.fetchWays(
                        latitudeMin, latitudeMax, longitudeMin, longitudeMax, 512D, 0, 0);
                final List<Path> paths = new ArrayList<>();
                for (NormalizedWay normalizedWay : ways)
                {
                    paths.add(normalizedWay.toPath());
                }

                Model model = Model.createModel(paths, 1024, 1024);

                final Pixel initialLocation =
                    computePlayerLocation(tileGeometry, tileGeometry.latitude, tileGeometry.longitude);
                model.setPlayerLocation(initialLocation.x, initialLocation.y);

                long time = System.currentTimeMillis();
                Random random = new Random(time);
                Server server = new Server(
                    events,
                    random);
                gameIdentifier = "local-" + time;
                server.startGame(time, gameIdentifier, model);

                while (!gameComplete.get())
                {
                    // FIXME: use a while.
                    Consumer<Server> polled = queue.poll();
                    if (polled != null)
                    {
                        polled.consume(server);
                    }
                    server.tick(System.currentTimeMillis());
                }
            }
            catch (IOException e)
            {
                events.gameRejected("", e.getMessage());
            }
        }

        public void setPlayerLocation(final int playerX, final int playerY)
        {
            queue.add(new Consumer<Server>()
            {
                @Override
                public void consume(Server server)
                {
                    server.onPlayerLocation(gameIdentifier, playerX, playerY);
                }
            });
        }

        public void onClick(final int x, final int y)
        {
            queue.add(new Consumer<Server>()
            {
                @Override
                public void consume(Server server)
                {
                    server.onClick(gameIdentifier, x, y);
                }
            });
        }
    }

    private void invalidate()
    {
        modelView.invalidate();
    }

    private static Pixel computePlayerLocation(
        TileGeometry geometry,
        double latitude,
        double longitude)
    {
        final double xOrigin = geometry.xTileOrigin * 512;
        final double yOrigin = geometry.yTileOrigin * 512;

        int playerX =
            (int) (WebMercator.x(longitude, ZOOM, 512) - xOrigin);
        int playerY =
            (int) (WebMercator.y(latitude, ZOOM, 512) - yOrigin);

        Log.w(ESPIONAGE_TAG, "Locating player at (" + playerX + ", " + playerY + ")");

        return new Pixel(playerX, playerY);
    }

    private static class ModelView extends View
        implements View.OnClickListener, View.OnTouchListener
    {
        private final Map<Pixel, Bitmap> tiles;
        private final TileGeometry geometry;

        private Paint paint;

        public GameView gameView;
        public RunGame runGame;

        ModelView(
            Context context,
            TileGeometry tileGeometry,
            Map<Pixel, Bitmap> tiles) {
            super(context);
            this.geometry = tileGeometry;
            this.tiles = tiles;

            paint = new Paint();

            setOnTouchListener(this);
            setOnClickListener(this);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            int tileSize = 512;
            for (Map.Entry<Pixel, Bitmap> pixelBitmapEntry : tiles.entrySet())
            {
                final Pixel key = pixelBitmapEntry.getKey();
                final Bitmap value = pixelBitmapEntry.getValue();
                int left = geometry.xOffset + (key.x - geometry.xTileOrigin) * tileSize;
                int top = geometry.yOffset + (key.y - geometry.yTileOrigin) * tileSize;
                canvas.drawBitmap(value, left, top, paint);
            }

            for (GameView.SentryView sentry : gameView.sentries()) {
                final Pixel renderable = sentry.location.round();
                final DoublePoint direction = sentry.orientation;
                renderSentry(renderable, direction, canvas);
            }

            if (gameView.playerLocation() != null)
            {
                Pixel round = gameView.playerLocation().round();
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

            if (gameView.isGameOver())
            {
                int halfHeight = geometry.screenHeight / 2;
                paint.setTextSize(64);
                paint.setTextAlign(Paint.Align.CENTER);
                paint.setTypeface(Typeface.DEFAULT_BOLD);
                int halfWidth = geometry.screenWidth / 2;
                canvas.drawText("Game Over", halfWidth, halfHeight - 32, paint);
                canvas.drawText("(bad luck)", halfWidth, halfHeight + 32, paint);
            }
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
        long touchStart;
        long lastTouchTime;
        boolean touchDrag = true;

        @Override
        public void onClick(View v)
        {
            if (!touchDrag)
            {
                runGame.onClick(
                    Math.round(lastTouchX) - geometry.xOffset,
                    Math.round(lastTouchY) - geometry.yOffset);
            }
        }

        @Override
        public boolean onTouch(View v, MotionEvent event)
        {
            switch (event.getAction())
            {
                case MotionEvent.ACTION_DOWN:
                {
                    touchStart = event.getDownTime();
                    touchDrag = false;
                    break;
                }
                case MotionEvent.ACTION_MOVE:
                {
                    if (event.getEventTime() - touchStart > 40)
                    {
                        touchDrag = true;
                        geometry.onDrag(
                            lastTouchX - event.getX(),
                            lastTouchY - event.getY()
                        );
                    }
                    break;
                }
            }

            lastTouchX = event.getX();
            lastTouchY = event.getY();
            lastTouchTime = event.getEventTime();

            return false;
        }

        void onLocationChanged(double latitude, double longitude)
        {
            Pixel pixel = computePlayerLocation(geometry, latitude, longitude);
            runGame.setPlayerLocation(pixel.x, pixel.y);
        }
    }

    private void initialiseAlertDialog()
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        ProgressBar progressBar = new ProgressBar(this);
        progressBar.setIndeterminate(true);
        builder.setMessage("Initializing");
        builder.setView(progressBar);

        alertDialog = builder.create();
        alertDialog.show();
    }

    //----------------------------------------------------------------------------
    // Location request boilerplate
    //----------------------------------------------------------------------------
    private void performLocationRequest()
    {
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

    @SuppressWarnings("NullableProblems")
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults)
    {
        requestLocation();
    }

    @SuppressWarnings("ResourceType")
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
}
