package net.digihippo.cryptnet.model;

import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.Node;
import net.digihippo.cryptnet.roadmap.Way;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class PathsTest {
    @Test
    public void connectTheDots()
    {
        Node one = node(2123, new LatLn(2.001, 0.112));
        Node two = node(2124, new LatLn(2.002, 0.113));
        Node three = node(2125, new LatLn(2.004, 0.114));

        final Way way = new Way(Arrays.asList(one, two, three));

        Node four = node(2126, new LatLn(2.000, 0.115));
        Node six = node(2126, new LatLn(2.003, 0.111));

        final Way another = new Way(Arrays.asList(four, two, six));

        List<Path> paths = Paths.from(Arrays.asList(way, another));

        assertThat(paths.size(), equalTo(2));
        final Path first = paths.get(0);
        final Path second = paths.get(0);

        assertThat(first.vertexAt(1), is(second.vertexAt(1)));
    }

    private Node node(int nodeId, LatLn latLn) {
        Node node = new Node(nodeId);
        node.latLn = latLn;

        return node;
    }
}