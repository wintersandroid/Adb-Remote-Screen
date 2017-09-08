package com.majeur.ars;

import java.net.URISyntaxException;

public class Utils {

    static String getRunningJarPath() {
        try {
            String s = Utils.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            return s.substring(0, s.lastIndexOf(System.getProperty("file.separator")));
        } catch (URISyntaxException e) {
            e.printStackTrace();
            return null;
        }
    }

    static void sleep(long t) {
        try {
            Thread.sleep(t);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
