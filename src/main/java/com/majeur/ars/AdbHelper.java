package com.majeur.ars;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;

public class AdbHelper {
    public final static boolean IS_WINDOW = System.getProperty("os.name").toLowerCase().contains("windows");
    private DevicesWatcher mDevicesWatcher;
    private final String mAdbPath;

    private String mDevice;

    public AdbHelper(String path) {
        mAdbPath = path;
    }

    public void registerDevicesChangedListener(OnDevicesChangedListener listener) {
        mDevicesWatcher = new DevicesWatcher(listener);
        mDevicesWatcher.startWach();
    }

    public void unregisterDevicesChangedListener() {
        mDevicesWatcher.stopWatch();
        mDevicesWatcher = null;
    }

    public void setTargetDevice(String device) {
        mDevice = device;
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

    private String executeDeviceShellCommand(String command) {
        if (mDevice == null) {
            Logger.e("No device selected, unable to execute '%s' command", command);
            return null;
        }

        return executeAdbCommand(String.format("-s %s shell %s", mDevice, command));
    }

    public String executeAdbCommand(String command) {
        return Utils.executeCommand(mAdbPath + " " + command);
    }

    public String[] getConnectedDevices() {
        String[] lines = executeAdbCommand("devices").split("\n");

        if (lines.length == 0) {
            return new String[0];
        }

        List<String> devices = new LinkedList<>();
        for (String line : lines) {
            if (line.startsWith("adb server") || line.startsWith("List of devices attached")) {
                continue;
            }

            devices.add(line.split("\t")[0]);
        }

        return devices.toArray(new String[devices.size()]);
    }

    public BufferedImage retrieveScreenShot() {
        if (mDevice == null) {
            Logger.e("No device selected, screenshot aborted");
            return null;
        }

        final String command = String.format(Constants.Adb.CMD_SCREENCAP, mAdbPath, mDevice);

        try {
            //return ImageIO.read(Utils.executeCommandGetInputStream("cmd.exe","/C",command));
            //return ImageIO.read(Utils.executeCommandGetInputStream("/bin/sh", "-c", command));

            InputStream inputStream;
			if (IS_WINDOW) {
				inputStream = Utils.executeCommandGetInputStream(command);
			} else {
				inputStream = Utils.executeCommandGetInputStream("/bin/sh", "-c", command);
			}
			byte[] bytes = convertStreamToByteArray(inputStream);
			System.out.println("bytes.length=" + bytes.length);
			InputStream inputStream2 = convertStreamToByteArray(bytes);
			return ImageIO.read(inputStream2);

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static InputStream convertStreamToByteArray(byte[] bytes) {
		byte[] bytes2 = new byte[bytes.length];
		int k = 0;
		for (int i = 0; i < bytes.length; i++) {
			if (i + 2 < bytes.length) {
				//System.out.println("==========>" + Integer.toHexString(bytes[i] & 0xff));
				if ((bytes[i + 0] & 0xff) == 0x0D && 
					(bytes[i + 1] & 0xff) == 0x0D &&
					(bytes[i + 2] & 0xff) == 0x0A) {
					i++;
					continue;
				}
			}
			bytes2[k] = bytes[i];
			k++;
		}
		byte[] bytes3 = new byte[k];
		for (int i = 0; i < k; i++) {
			bytes3[i] = bytes2[i];
		}
		System.out.println("bytes3.length=" + bytes3.length);
		return new ByteArrayInputStream(bytes3);
	}
	
	public static byte[] convertStreamToByteArray(InputStream is) {
		try {
			ByteArrayOutputStream ots = new ByteArrayOutputStream();
			byte[] data = new byte[4096];
			int count = -1;
			while ((count = is.read(data)) != -1) {
				ots.write(data, 0, count);
			}
			byte[] bytes = ots.toByteArray();
			return bytes;
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return new byte[0];
	}

    public boolean saveScreenShotToFile(File file) {
        if (mDevice == null) {
            return false;
        }

        String commandLine = String.format(Constants.Adb.CMD_SCREENCAP, mAdbPath, mDevice) + " > "
                + file.getAbsolutePath();
        //Utils.executeCommand("/bin/sh", "-c", commandLine);
        Utils.executeCommand( commandLine);
        return true;
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
