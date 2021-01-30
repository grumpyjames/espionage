package net.digihippo.cryptnet.roadmap;

import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

public class OsmParserTest
{
    @Test
    public void parseSampleFile() throws IOException
    {
        URL resource = this.getClass().getResource("overpass.json");
        WayCollector wayCollector = new WayCollector();
        try (InputStream inputStream = resource.openConnection().getInputStream())
        {
            OsmSource.parseWays(inputStream, wayCollector);
        }

        assertThat(wayCollector.reducedWays().size(), equalTo(37));
    }

    @Test
    public void parseEmptyFile() throws IOException
    {
        URL resource = this.getClass().getResource("overpass-empty.json");
        WayCollector wayCollector = new WayCollector();
        try (InputStream inputStream = resource.openConnection().getInputStream())
        {
            OsmSource.parseWays(inputStream, wayCollector);
        }

        assertThat(wayCollector.reducedWays().size(), equalTo(0));
    }
}
