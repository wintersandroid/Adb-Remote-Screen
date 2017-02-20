/*******************************************************************************
 * Copyright (c) 2014 Marian Schedenig
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Marian Schedenig - initial API and implementation
 *******************************************************************************/
package com.majeur.ars;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

    private String adbCommand;
    private long screenshotDelay;
    private int screenWidth;
    private int screenHeight;
    private int scale;

    public void load(InputStream in) throws IOException {
        Properties properties = new Properties();
        properties.load(in);

        adbCommand = properties.getProperty("adbCommand");
        screenshotDelay = Long.parseLong(properties.getProperty("screenshotDelay"));
        screenWidth = Integer.valueOf(properties.getProperty("screenWidth"));
        screenHeight = Integer.valueOf(properties.getProperty("screenHeight"));
        scale = Integer.valueOf(properties.getProperty("scale"));
    }

    public String getAdbCommand() {
        return adbCommand;
    }

    public void setAdbCommand(String adbCommand) {
        this.adbCommand = adbCommand;
    }

    public long getScreenshotDelay() {
        return screenshotDelay;
    }

    public void setScreenshotDelay(long screenshotDelay) {
        this.screenshotDelay = screenshotDelay;
    }

    public int getScreenWidth() {
        return screenWidth;
    }

    public void setScreenWidth(int screenWidth) {
        this.screenWidth = screenWidth;
    }

    public int getScreenHeight() {
        return screenHeight;
    }

    public void setScreenHeight(int screenHeight) {
        this.screenHeight = screenHeight;
    }

    public int getScale() {
        return scale;
    }

    public void setScale(int scale) {
        this.scale = scale;
    }

}
