package com.majeur.ars;

import java.io.File;

import javax.swing.SwingUtilities;

public class AdbRemoteScreen {

    public static void main(String... args) {
        File configFile;

        if (args.length == 0) {
            configFile = new File("local.properties");
        } else {
            configFile = new File(args[0]);
        }

        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new MainFrame(configFile);
            }
        });
    }
}
