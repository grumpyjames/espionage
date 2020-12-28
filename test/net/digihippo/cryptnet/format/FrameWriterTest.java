package net.digihippo.cryptnet.format;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.UnpooledByteBufAllocator;
import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FrameWriterTest
{
    @Test
    public void roundTrip()
    {
        FrameCollector.Frame frame = new FrameCollector.Frame(2342);
        double latDegs = 43.5635644D;
        double lonDegs = 12.23453436D;
        frame.playerLocation = LatLn.toRads(latDegs, lonDegs);
        frame.gameOver = true;
        frame.victory = false;
        frame.joining.add(new FrameCollector.JoiningView(
                LatLn.toRads(46.243466D, 45.5253443D),
                new UnitVector(0.004D, 0.0001D),
                LatLn.toRads(16.243466D, 15.53D)
        ));
        frame.joining.add(new FrameCollector.JoiningView(
                LatLn.toRads(76.273766D, 75.5253743D),
                new UnitVector(0.001D, 0.0004D),
                LatLn.toRads(26.243466D, 25.53D)
        ));
        frame.patrols.add(new FrameCollector.PatrolView(
                LatLn.toRads(86.283866D, 85.5253843D),
                new UnitVector(0.003D, 0.0002D)
        ));
        frame.patrols.add(new FrameCollector.PatrolView(
                LatLn.toRads(66.263666D, 65.5253643D),
                new UnitVector(0.002D, 0.0003D)
                ));

        ByteBuf buffer = UnpooledByteBufAllocator.DEFAULT.buffer();
        FrameWriter.write(frame, buffer);

        FrameCollector.Frame read = readFrame(buffer);

        assertEquals(read, frame);
    }

    private FrameCollector.Frame readFrame(ByteBuf buffer)
    {
        int frameCounter = buffer.readInt();
        FrameCollector.Frame frame = new FrameCollector.Frame(frameCounter);
        frame.victory = buffer.readBoolean();
        frame.gameOver = buffer.readBoolean();
        frame.playerLocation = readLatLn(buffer);
        int joinCount = buffer.readInt();
        for (int i = 0; i < joinCount; i++)
        {
            LatLn latLn = readLatLn(buffer);
            UnitVector dir = readUnitVector(buffer);
            LatLn connection = readLatLn(buffer);

            FrameCollector.JoiningView jv = new FrameCollector.JoiningView(latLn, dir, connection);
            frame.joining.add(jv);
        }

        int patrolCount = buffer.readInt();
        for (int i = 0; i < patrolCount; i++)
        {
            LatLn latLn = readLatLn(buffer);
            UnitVector dir = readUnitVector(buffer);

            FrameCollector.PatrolView pv = new FrameCollector.PatrolView(latLn, dir);
            frame.patrols.add(pv);
        }

        return frame;
    }

    private UnitVector readUnitVector(ByteBuf buffer)
    {
        double dLat = buffer.readDouble();
        double dLon = buffer.readDouble();
        return new UnitVector(dLat, dLon);
    }

    private LatLn readLatLn(ByteBuf buffer)
    {
        double lat = buffer.readDouble();
        double lon = buffer.readDouble();
        return new LatLn(lat, lon);
    }
}