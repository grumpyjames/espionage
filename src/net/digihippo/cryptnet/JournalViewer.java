package net.digihippo.cryptnet;

import net.digihippo.cryptnet.model.Frame;
import net.digihippo.cryptnet.server.DelegatingServerToClient;
import net.digihippo.cryptnet.server.Journal;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.locks.LockSupport;

public class JournalViewer
{
    public static void main(String[] args) throws IOException
    {
        Viewer viewer = Viewer.newViewer();

        SwingUtilities.invokeLater(() ->
        {
            JFrame f = new JFrame("Lines and intersections");
            f.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
            f.getContentPane().setBackground(Color.BLACK);
            f.add(viewer);
            f.pack();
            f.setVisible(true);
        });

        Journal.playJournal(
                new File("/tmp/game-game-2.log"),
                new DelegatingServerToClient(viewer) {
                    @Override
                    public void onFrame(Frame frame)
                    {
                        super.onFrame(frame);
                        LockSupport.parkNanos(39_000_000);
                    }
                });
    }

}
