package com.riders.thelab.core.utils;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;
import android.view.WindowMetrics;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

import timber.log.Timber;

public class LabDeviceManager {

    private LabDeviceManager() {
    }

    public static void logDeviceInfo() {
        Timber.d("logDeviceInfo()");
        Timber.i("SERIAL: %s ", Build.SERIAL);
        Timber.i("MODEL: %s ", Build.MODEL);
        Timber.i("ID: %s ", Build.ID);
        Timber.i("Manufacture: %s ", Build.MANUFACTURER);
        Timber.i("brand: %s ", Build.BRAND);
        Timber.i("type: %s ", Build.TYPE);
        Timber.i("user: %s ", Build.USER);
        Timber.i("BASE: %s ", Build.VERSION_CODES.BASE);
        Timber.i("INCREMENTAL: %s ", Build.VERSION.INCREMENTAL);
        Timber.i("SDK : %s ", Build.VERSION.SDK);
        Timber.i("BOARD: %s ", Build.BOARD);
        Timber.i("BRAND: %s ", Build.BRAND);
        Timber.i("HOST: %s ", Build.HOST);
        Timber.i("FINGERPRINT: %s ", Build.FINGERPRINT);
        Timber.i("Version Code: %s ", Build.VERSION.RELEASE);
    }

    public static String getDevice() {
        return Build.DEVICE;
    }

    public static String getSerial() {
        String serial = null;

        /**
         * http://stackoverflow.com/questions/14161282/serial-number-from-samsung-device-running-android
         *
         * The OP asked about Galaxy Tab 2 and for that indeed the answer was ril.serialnumber (even for the non-3G model - see this gist).
         * According to Himanshu's answer Galaxy Tab 3 uses sys.serialnumber (also backed by this answer).
         * sys.serialnumber makes better sense for tablets as ril.* stands for Radio Interface Layer, something most tablets are not equipped with
         * (ril.serialnumber, respectively, makes better sense for phones).
         *
         * There is no standard API for getting the device serial number
         * (ie the serial number on the packaging - not to be confused with Settings.Secure.ANDROID_ID or the various other "unique" identifiers scattered throughout the API).
         * This means it is up to the manufacturer to decide where to store the device serial (if at all).
         * On the S3 Mini it's ril.serialnumber,
         * on NexusOne it's ro.serialno (gist),
         * on Galaxy Tab 2 it's ril.serialnumber,
         * on Galaxy Tab 3/4 it's ril.serialnumber,
         * on Lenovo Tab it's none of the above.
         *
         * These settings appear to be the usual suspects, when looking for the device serial, but shouldn't be taken for granted,
         * and as such, shouldn't be relied on for tracking unique app installations.
         */

        try {

            @SuppressLint("PrivateApi") Class<?> c = Class.forName("android.os.SystemProperties");
            Method get = c.getMethod("get", String.class, String.class);
            serial = (String) get.invoke(c, "ril.serialnumber", "unknown");
            return serial;
        } catch (Exception e) {
            Timber.e("Some error occurred : %s", e.getMessage());
        }

        return serial;
//        return Build.SERIAL;
    }

    public static String getModel() {
        return Build.MODEL;
    }

    public static String getID() {
        return Build.ID;
    }

    public static String getManufacturer() {
        return Build.MANUFACTURER;
    }

    public static String getBrand() {
        return Build.BRAND;
    }

    public static String getType() {
        return Build.TYPE;
    }

    public static String getUser() {
        return Build.USER;
    }

    public static int getVersionBase() {
        return Build.VERSION_CODES.BASE;
    }

    public static String getVersionIncremental() {
        return Build.VERSION.INCREMENTAL;
    }

    public static int getSdkVersion() {
        return Build.VERSION.SDK_INT;
    }

    public static String getBoard() {
        return Build.BOARD;
    }

    public static String getHost() {
        return Build.HOST;
    }

    public static String getFingerPrint() {
        return Build.FINGERPRINT;
    }

    public static String getVersionCode() {
        return Build.VERSION.RELEASE;
    }

    public static String getHardware() {
        return Build.HARDWARE;
    }

    public static String getRelease() {
        return Build.VERSION.RELEASE;
    }

    @SuppressLint("NewApi")
    public static int getScreenHeight(Activity activity) {

        int screenHeight = 0;

        if (LabCompatibilityManager.isAndroid10()) {
            WindowMetrics metrics = getDisplayMetricsAndroid10(activity);
            screenHeight = metrics.getBounds().height();
        } else {
            DisplayMetrics metrics = getDisplayMetrics(activity);
            screenHeight = metrics.heightPixels;
        }

        return screenHeight;
    }

    @SuppressLint("NewApi")
    public static int getScreenWidth(Activity activity) {
        int screenWidth = 0;

        if (LabCompatibilityManager.isAndroid10()) {
            WindowMetrics metrics = getDisplayMetricsAndroid10(activity);
            screenWidth = metrics.getBounds().width();
        } else {
            DisplayMetrics metrics = getDisplayMetrics(activity);
            screenWidth = metrics.widthPixels;
        }

        return screenWidth;
    }


    public static DisplayMetrics getDisplayMetrics(Activity activity) {
        //Retrieve Screen's height and width
        DisplayMetrics metrics = new DisplayMetrics();
        activity
                .getWindowManager()
                .getDefaultDisplay()
                .getMetrics(metrics);

        return metrics;
    }

    @SuppressLint("NewApi")
    public static WindowMetrics getDisplayMetricsAndroid10(Activity activity) {
        WindowManager manager = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);

        return new WindowMetrics(
                manager.getCurrentWindowMetrics().getBounds(),
                manager.getCurrentWindowMetrics().getWindowInsets());
    }


    /**
     * Checks if the device is rooted.
     *
     * @return <code>true</code> if the device is rooted, <code>false</code> otherwise.
     */
    public static boolean isRooted() {

        // get from build info
        String buildTags = android.os.Build.TAGS;
        if (buildTags != null && buildTags.contains("test-keys")) {
            return true;
        }

        // check if /system/app/Superuser.apk is present
        try {
            File file = new File("/system/app/Superuser.apk");
            if (file.exists()) {
                return true;
            }
        } catch (Exception e1) {
            // ignore
        }

        // try executing commands
        return canExecuteCommand("/system/xbin/which su")
                || canExecuteCommand("/system/bin/which su") || canExecuteCommand("which su");
    }

    // executes a command on the system
    private static boolean canExecuteCommand(String command) {
        boolean executedSuccesfully;
        try {
            Runtime.getRuntime().exec(command);
            executedSuccesfully = true;
        } catch (Exception e) {
            executedSuccesfully = false;
        }

        return executedSuccesfully;
    }
}
