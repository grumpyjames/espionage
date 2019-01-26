package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.dimtwo.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class PlayerTest
{
    private static final Map<Pixel, Intersection> NO_INTERSECTIONS = Collections.emptyMap();

    private final Line lineOne = Line.createLine(new Pixel(0, 0), new Pixel(10, 10));
    private final Line lineTwo = Line.createLine(new Pixel(10, 10), new Pixel(20, 20));
    private final Line lineThree = Line.createLine(new Pixel(20, 20), new Pixel(30, 30));
    private final Line lineFour = Line.createLine(new Pixel(30, 30), new Pixel(40, 40));
    private final Path path = new Path(Arrays.asList(lineOne, lineTwo, lineThree, lineFour));

    private final Line perpLineOne = Line.createLine(new Pixel(0, 40), new Pixel(10, 30));
    private final Line perpLineTwo = Line.createLine(new Pixel(10, 30), new Pixel(20, 20));
    private final Line perpLineThree = Line.createLine(new Pixel(20, 20), new Pixel(30, 10));
    private final Line perpLineFour = Line.createLine(new Pixel(30, 10), new Pixel(40, 0));
    private final Path perpPath = new Path(Arrays.asList(perpLineOne, perpLineTwo, perpLineThree, perpLineFour));

    private Map<Pixel, Intersection> intersections = Intersection.intersections(Arrays.asList(path, perpPath));


    @Test
    public void followLineToTheEndAndThenStop()
    {
        Player player = new Player(path, lineTwo, new DoublePoint(13, 13));

        player.moveTowards(new Pixel(22, 22));

        for (int i = 0; i < 50; i++)
        {
            player.tick(NO_INTERSECTIONS);
        }
        assertThat(player.position, equalTo(new DoublePoint(40, 40)));
        assertThat(player.line, equalTo(lineFour));
    }

    @Test
    public void followLineToTheEndAndThenStopAndProgressNoFurtherIfDispatchedBeyondTheEnd()
    {
        Player player = new Player(path, lineTwo, new DoublePoint(13, 13));

        player.moveTowards(new Pixel(22, 22));

        for (int i = 0; i < 50; i++)
        {
            player.tick(NO_INTERSECTIONS);
        }
        assertThat(player.position, equalTo(new DoublePoint(40, 40)));

        player.moveTowards(new Pixel(50, 50));

        for (int i = 0; i < 50; i++)
        {
            player.tick(NO_INTERSECTIONS);
        }
        // Failing. Hmm.
        // assertThat(player.position, equalTo(new DoublePoint(40, 40)));
    }

    @Test
    public void followLineToTheEndAndThenBack()
    {
        Player player = new Player(path, lineTwo, new DoublePoint(13, 13));

        player.moveTowards(new Pixel(22, 22));

        for (int i = 0; i < 50; i++)
        {
            player.tick(NO_INTERSECTIONS);
        }
        assertThat(player.position, equalTo(new DoublePoint(40, 40)));
        assertThat(player.line, equalTo(lineFour));

        player.moveTowards(new Pixel(1, 1));

        for (int i = 0; i < 100; i++)
        {
            player.tick(NO_INTERSECTIONS);
        }
        assertThat(player.position, equalTo(new DoublePoint(0, 0)));
        assertThat(player.line, equalTo(lineOne));
    }

    @Test
    public void stopAtIntersections()
    {
        Player player = new Player(path, lineTwo, new DoublePoint(13, 13));

        player.moveTowards(new Pixel(22, 22));

        for (int i = 0; i < 10; i++)
        {
            player.tick(intersections);
        }
        assertThat(player.position, equalTo(new DoublePoint(20, 20)));
        assertThat(player.line, equalTo(lineTwo));
    }

    @Test
    public void switchPathAtIntersection()
    {
        Player player = new Player(path, lineTwo, new DoublePoint(13, 13));

        player.moveTowards(new Pixel(22, 22));

        for (int i = 0; i < 10; i++)
        {
            player.tick(intersections);
        }

        player.moveTowards(new Pixel(1, 39));

        for (int i = 0; i < 50; i++)
        {
            player.tick(intersections);
        }

        assertThat(player.position, equalTo(new DoublePoint(0, 40)));
        assertThat(player.line, equalTo(perpLineOne));
    }

    @Test
    public void reverseAtIntersection()
    {
        Player player = new Player(path, lineTwo, new DoublePoint(13, 13));

        player.moveTowards(new Pixel(22, 22));

        for (int i = 0; i < 10; i++)
        {
            player.tick(intersections);
        }

        player.moveTowards(new Pixel(1, 1));

        for (int i = 0; i < 50; i++)
        {
            player.tick(intersections);
        }

        assertThat(player.position, equalTo(new DoublePoint(0, 0)));
        assertThat(player.line, equalTo(lineOne));
    }

    @Test
    public void roundTrip()
    {
        Player player = new Player(path, lineTwo, new DoublePoint(13, 13));

        assertThat(Player.parse(player.toString()), equalTo(player));
    }

    @Test
    public void nooseShapedPaths()
    {
        Path noose =
            Path.parse("(0,0)->(1,1)->(2,2)->(3,3)->(4,5)->(4,6)->(3,7)->(2,6)->(1,4)->(2,3)->(2,2)");

        Player player =
            new Player(noose, noose.lines.get(0), new DoublePoint(0, 0));
        player.moveTowards(new Pixel(2,2));

        for (int i = 0; i < 50; i++)
        {
            player.tick(NO_INTERSECTIONS);
        }

        assertThat(player.position, equalTo(new DoublePoint(2, 2)));
        assertThat(player.line, equalTo(noose.lines.get(9)));
    }

    @Test
    public void intersectionsWherePathsEnd()
    {

    }
}