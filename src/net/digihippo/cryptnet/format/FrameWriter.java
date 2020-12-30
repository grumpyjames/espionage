package net.digihippo.cryptnet.format;

import io.netty.buffer.ByteBuf;
import net.digihippo.cryptnet.model.FrameCollector;
import net.digihippo.cryptnet.roadmap.LatLn;
import net.digihippo.cryptnet.roadmap.UnitVector;

public class FrameWriter
{
    public static void write(FrameCollector.Frame frame, ByteBuf byteBuf)
    {
        byteBuf.writeInt(frame.frameCounter);
        byteBuf.writeBoolean(frame.victory);
        byteBuf.writeBoolean(frame.gameOver);
        writeLatLn(frame.playerLocation, byteBuf);
        byteBuf.writeInt(frame.joining.size());
        frame.joining.forEach(jv -> {
            writeLatLn(jv.location, byteBuf);
            writeUnitVector(jv.orientation, byteBuf);
            writeLatLn(jv.connectionLocation, byteBuf);
        });
        byteBuf.writeInt(frame.patrols.size());
        frame.patrols.forEach(p -> {
            writeLatLn(p.location, byteBuf);
            writeUnitVector(p.orientation, byteBuf);
        });
    }

    private static void writeUnitVector(UnitVector unitVector, ByteBuf byteBuf)
    {
        byteBuf.writeDouble(unitVector.dLat);
        byteBuf.writeDouble(unitVector.dLon);
    }

    public static void writeLatLn(LatLn latLn, ByteBuf byteBuf)
    {
        byteBuf.writeDouble(latLn.lat);
        byteBuf.writeDouble(latLn.lon);
    }
}
