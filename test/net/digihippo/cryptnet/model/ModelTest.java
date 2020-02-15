package net.digihippo.cryptnet.model;

import org.junit.Test;

import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

public class ModelTest
{
    @Test
    public void roundTrip()
    {
//        Segment segmentIntersectOne = Segment.createLine(new Pixel(2, 3), new Pixel(4, 5));
//        Segment segmentTwo = Segment.createLine(new Pixel(4, 5), new Pixel(4, 7));
//        Segment segmentThree = Segment.createLine(new Pixel(4, 7), new Pixel(4, 10));
//        Path one = new Path(Arrays.asList(segmentIntersectOne, segmentTwo, segmentThree));
//
//        Segment segmentFour = Segment.createLine(new Pixel(0, 0), new Pixel(3, 6));
//        Segment segmentFive = Segment.createLine(new Pixel(3, 6), new Pixel(5, 6));
//        Segment segmentSix = Segment.createLine(new Pixel(5, 6), new Pixel(10, 10));
//        Path two = new Path(Arrays.asList(segmentFour, segmentFive, segmentSix));
//
//        Model model = Model.createModel(Arrays.asList(one, two), 256, 256);
//
//        model.addSentry(42, 43);
//        model.addSentry(10, 1);
//
//        model.addPatrols(Arrays.asList(
//            new Patrol("patrol-one", one, segmentThree, new DoublePoint(0, 1), new DoublePoint(4, 8), Direction.Forwards),
//            new Patrol("patrol-two", one, segmentThree, new DoublePoint(0, 1), new DoublePoint(4, 8), Direction.Backwards)
//        ));
//
//        model.setPlayerLocation(13, 44);
//
//        // The String representation of some of the double values is, sadly, lossy.
//        assertThat(Model.parse(model.toString()).toString(), equalTo(model.toString()));
    }
}