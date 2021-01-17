package net.digihippo.cryptnet;

import net.digihippo.cryptnet.model.Frame;
import net.digihippo.cryptnet.model.Path;
import net.digihippo.cryptnet.model.Segment;
import net.digihippo.cryptnet.model.StayAliveRules;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.server.ServerToClient;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

final class Viewer extends Component implements ServerToClient
{
    private final int width;
    private final int height;

    private final java.util.List<Path> paths = new ArrayList<>();
    private final BufferedImage sentinel;
    private final BufferedImage player;

    private volatile Frame frame;

    private LatLn origin;
    private double xPix;
    private double yPix;

    static Viewer newViewer() throws IOException
    {
        BufferedImage sentinel = ImageIO.read(new File("sentinel.png"));
        BufferedImage player = ImageIO.read(new File("player.png"));
        return new Viewer(500, 1000, sentinel, player);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(width, height);
    }

    @Override
    public Color getBackground()
    {
        return Color.BLACK;
    }

    Viewer(
            final int width,
            final int height,
            BufferedImage sentinel,
            BufferedImage player)
    {
        this.width = width;
        this.height = height;
        this.sentinel = sentinel;
        this.player = player;
    }

    @Override
    public void paint(final Graphics g)
    {
        Frame f = frame;
        if (f == null)
        {
            return;
        }

        final double bearing = (3 * Math.PI / 2) + Math.atan((double) height/(double) width);

        LatLn centre = f.playerLocation;

        this.origin = centre.move(200, bearing);
        this.xPix = (2 * (centre.lon - origin.lon) / (double) width);
        this.yPix = (2 * (centre.lat - origin.lat) / (double) height);

        Graphics2D g2 = (Graphics2D)g;
        RenderingHints rh = new RenderingHints(
                RenderingHints.KEY_TEXT_ANTIALIASING,
                RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        g2.setRenderingHints(rh);
        g2.setBackground(Color.BLACK);

        for (Path path : paths)
        {
            for (Segment line: path.segments())
            {
                drawLine(g, line);
            }
        }

        f.patrols.forEach(sentry -> renderSentry(sentry.location, g));
        f.joining.forEach(joiner -> renderJoiningSentry(g, joiner.location, joiner.connectionLocation));

        renderPlayer(g, f.playerLocation);

        if (f.victory)
        {
            System.out.println("Victory?");
            g.drawString("Victory!", (width / 2), (height / 2));
        }
        if (f.gameOver)
        {
            System.out.println("Game: Over?");
            g.drawString("Game over!", (width / 2), (height / 2));
        }
    }

    private static final DecimalFormat format = new DecimalFormat("#.#####");

    private String toHumanCoords(LatLn topLeft)
    {
        return format.format(Math.toDegrees(topLeft.lat)) + "," + format.format(Math.toDegrees(topLeft.lon));
    }

    private void renderJoiningSentry(Graphics g, LatLn location, LatLn connectionLocation) {
        drawLine(g, location, connectionLocation, true);
        filledCircleAt(g, location);
    }

    private void renderPlayer(Graphics g, LatLn playerLocation) {

        int x1 = pix(dLon(playerLocation) / xPix);
        int y1 = pix(dLat(playerLocation) / yPix);
        g.drawImage(
                player,
                x1 - (player.getWidth() / 2),
                y1 - (player.getHeight() / 2),
                null
        );
    }

    private void filledCircleAt(Graphics g, LatLn latln)
    {
        int x1 = pix(dLon(latln) / xPix);
        int y1 = pix(dLat(latln) / yPix);

        g.drawOval(x1 - 4, y1 - 4, 4 * 2, 4 * 2);
        g.setColor(Color.BLUE);
        g.fillOval(x1 - 4, y1 - 4, 4 * 2, 4 * 2);
        g.setColor(Color.WHITE);
    }

    private void renderSentry(LatLn location, Graphics g)
    {
        int x1 = pix(dLon(location) / xPix);
        int y1 = pix(dLat(location) / yPix);
        g.drawImage(
                sentinel,
                x1 - (sentinel.getWidth() / 2),
                y1 - (sentinel.getHeight() / 2),
                null
        );
    }

    private void drawLine(Graphics g, Segment segment)
    {
        // reminder: the swing origin is also the top left.
        final LatLn head = segment.head.location;
        final LatLn tail = segment.tail.location;

        drawLine(g, head, tail, false);
    }

    double dLon(LatLn latln)
    {
        return latln.lon - origin.lon;
    }

    double dLat(LatLn latln)
    {
        return latln.lat - origin.lat;
    }

    int pix(double dPix)
    {
        return (int) Math.round(dPix);
    }

    private void drawLine(Graphics g, LatLn head, LatLn tail, boolean debug)
    {
        final int startX = pix(dLon(head) / xPix);
        final int endX = pix(dLon(tail) / xPix);
        final int startY = pix(dLat(head) / yPix);
        final int endY = pix(dLat(tail) / yPix);

        g.setColor(Color.WHITE);
        g.drawLine(startX, startY, endX, endY);

        if (debug)
        {
            g.drawString(toHumanCoords(head), startX, startY);
            g.drawString(toHumanCoords(tail), endX, endY);
        }
    }

    @Override
    public void sessionEstablished(String sessionKey)
    {

    }

    @Override
    public void rules(StayAliveRules rules)
    {

    }

    @Override
    public void path(Path path)
    {
        this.paths.add(path);
        repaint();
    }

    @Override
    public void gameReady(String gameId)
    {

    }

    @Override
    public void gameStarted()
    {

    }

    @Override
    public void onFrame(Frame frame)
    {
        this.frame = frame;
        repaint();
    }

    @Override
    public void error(String errorCode)
    {

    }
}
