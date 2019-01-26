package net.digihippo.cryptnet.espionage;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import net.digihippo.cryptnet.dimtwo.*;
import net.digihippo.cryptnet.dimtwo.Path;
import net.digihippo.cryptnet.model.JoiningSentry;
import net.digihippo.cryptnet.model.Model;
import net.digihippo.cryptnet.model.Patrol;
import net.digihippo.cryptnet.roadmap.NormalizedWay;
import net.digihippo.cryptnet.roadmap.OsmSource;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.*;

public class EspionageActivity extends Activity
{
    private Model model;
    private Map<Pixel, Bitmap> tiles = new HashMap<>();

    private static final int xTile = 65480;
    private static final int yTile = 43572;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        new CreateInitialModel(this).execute();

        new FetchTileBitmap(this).execute(
            new Pixel(xTile, yTile),
            new Pixel(xTile + 1, yTile),
            new Pixel(xTile, yTile + 1),
            new Pixel(xTile + 1, yTile + 1)
        );
    }

    private static class FetchTileBitmap extends AsyncTask<Pixel, String, Map<Pixel, Bitmap>>
    {
        private final EspionageActivity callback;

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
                        new URL("https://tile.osmand.net/hd/17/" + pixel.x + "/" + pixel.y + ".png");

                    URLConnection urlConnection = tileUrl.openConnection();

                    result.put(pixel, BitmapFactory.decodeStream(urlConnection.getInputStream()));
                }
                catch (IOException e)
                {
                    return null;
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Map<Pixel, Bitmap> bitmap)
        {
            callback.tiles = bitmap;
            callback.tryPromote();
        }
    }

    private static class CreateInitialModel extends AsyncTask<Void, String, Model>
    {
        private final EspionageActivity callback;

        private CreateInitialModel(EspionageActivity callback)
        {
            this.callback = callback;
        }

        @Override
        protected Model doInBackground(Void... voids)
        {
            int xTile = 65480;
            int yTile = 43572;
            // tile coords increase as latitude decreases
            double latitudeMin = OsmSource.lat((yTile + 2) * 256, 17);
            double latitudeMax = OsmSource.lat(yTile * 256, 17);

            // tile coords increase with longitude
            double longitudeMin = OsmSource.lon(xTile * 256, 17);
            double longitudeMax = OsmSource.lon((xTile + 2) * 256, 17);

            try
            {
                return startingModel(
                    OsmSource.fetchWays(latitudeMin, latitudeMax, longitudeMin, longitudeMax, 512D),
                    1024,
                    1024
                );
            } catch (IOException e)
            {
                return null;
            }
        }

        @Override
        protected void onPostExecute(Model model)
        {
            callback.model = model;
            callback.tryPromote();
        }
    }

    private void tryPromote()
    {
        if (model != null && !tiles.isEmpty())
        {
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
