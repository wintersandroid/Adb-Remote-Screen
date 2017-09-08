package com.majeur.ars;

import com.android.ddmlib.*;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public class AdbHelper {
    private DevicesWatcher mDevicesWatcher;
    private final String mAdbPath;

    private IDevice mDevice;
    private final AndroidDebugBridge adb;
    private final BufferedImage buffy;
    private IDevice[] devices;

    public AdbHelper(String path, int width, int height) {
        mAdbPath = path;
        AndroidDebugBridge.init(false);
        adb = AndroidDebugBridge.createBridge(mAdbPath, true);

        int trials = 10;
        while (trials > 0) {
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (adb.isConnected()) {
                break;
            }
            trials--;
        }

        if (!adb.isConnected()) {
            System.out.println("Couldn't connect to ADB server");
        }

        GraphicsEnvironment env = GraphicsEnvironment.getLocalGraphicsEnvironment();
        GraphicsDevice device2 = env.getDefaultScreenDevice();
        GraphicsConfiguration config = device2.getDefaultConfiguration();
        buffy = config.createCompatibleImage(width, height, BufferedImage.TYPE_INT_ARGB);
    }

    public void registerDevicesChangedListener(OnDevicesChangedListener listener) {
        mDevicesWatcher = new DevicesWatcher(listener);
        mDevicesWatcher.startWach();
    }

    public void unregisterDevicesChangedListener() {
        mDevicesWatcher.stopWatch();
        mDevicesWatcher = null;
    }

    public void setTargetDevice(String deviceName) {
        for (IDevice device : devices) {
            if (device.getSerialNumber().equals(deviceName)) {
                mDevice = device;
            }
        }
    }

    public void performInputKey(AndroidKey key) {
        Logger.i("Key pressed '" + key.toString() + "' (code: %d)", key.getCode());
        executeDeviceShellCommand(String.format(Constants.Adb.CMD_KEY, key.getCode()));
    }

    public void performInputKey(int keyCode) {
        Logger.i("Key pressed (code: %d)", keyCode);
        executeDeviceShellCommand(String.format(Constants.Adb.CMD_KEY, keyCode));
    }

    public void performClick(double x, double y) {
        Logger.i("Click at %.1f %.1f", x, y);
        executeDeviceShellCommand(String.format(Constants.Adb.CMD_TAP, x, y).replace(',', '.'));
    }

    public void performSwipe(double x1, double y1, double x2, double y2, long duration) {
        Logger.i("Swipe from %.0f %.0f to %.0f %.0f during %d ms", x1, y1, x2, y2, duration);
        executeDeviceShellCommand(String.format(Constants.Adb.CMD_SWIPE, x1, y1, x2, y2, duration));
    }

    private void executeDeviceShellCommand(String command) {
        if (mDevice == null) {
            Logger.e("No device selected, unable to execute '%s' command", command);
            return;
        }
        try {
            mDevice.executeShellCommand(command, new IShellOutputReceiver() {
                @Override
                public void addOutput(byte[] bytes, int i, int i1) {

                }

                @Override
                public void flush() {

                }

                @Override
                public boolean isCancelled() {
                    return false;
                }
            });
        } catch (TimeoutException e) {
            e.printStackTrace();
        } catch (AdbCommandRejectedException e) {
            e.printStackTrace();
        } catch (ShellCommandUnresponsiveException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String[] getConnectedDevices() {
        devices = adb.getDevices();
        String[] names = new String[devices.length];
        for (int i = 0; i < devices.length; i++) {
            names[i] = devices[i].getSerialNumber();
        }
        return names;
    }

    public BufferedImage retrieveScreenShot() {
        if (mDevice == null) {
            Logger.e("No device selected, screenshot aborted");
            return null;
        }

        RawImage rawImage;
        try {
            rawImage = mDevice.getScreenshot();
        } catch (TimeoutException e) {
            System.out.println("Unable to get frame buffer: timeout");
            return null;
        } catch (Exception ioe) {
            System.out.println("Unable to get frame buffer: " + ioe.getMessage());
            return null;
        }
        // device/adb not available?
        if (rawImage == null)
            return null;
        // convert raw data to an Image
        int index = 0;
        int IndexInc = rawImage.bpp >> 3;
        for (int y = 0; y < rawImage.height; y++) {
            for (int x = 0; x < rawImage.width; x++) {
                int value = rawImage.getARGB(index);
                index += IndexInc;
                buffy.setRGB(x, y, value);
            }
        }
        return buffy;
    }

    public boolean saveScreenShotToFile(File file) {
        if (mDevice == null) {
            return false;
        }

        BufferedImage image = retrieveScreenShot();
        try {
            return ImageIO.write(image, "png", file);
        } catch (IOException e) {
            Logger.e(e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    interface OnDevicesChangedListener {
        void onDevicesChanged(String[] devices);
    }

    private class DevicesWatcher implements Runnable {

        Thread mWatchThread;
        private String[] mOldDevices;
        private OnDevicesChangedListener mListener;

        public DevicesWatcher(OnDevicesChangedListener listener) {
            mListener = listener;
        }

        void startWach() {
            mWatchThread = new Thread(this);
            mWatchThread.start();
        }

        void stopWatch() {
            mWatchThread.interrupt();
            mWatchThread = null;
        }

        @Override
        public void run() {
            while (!Thread.interrupted()) {
                final String[] newDevices = getConnectedDevices();
                if (!Arrays.equals(newDevices, mOldDevices)) {
                    mOldDevices = newDevices;
                    SwingUtilities.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            mListener.onDevicesChanged(newDevices);
                        }
                    });
                }

                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
