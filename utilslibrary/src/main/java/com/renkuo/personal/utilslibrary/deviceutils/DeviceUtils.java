package com.renkuo.personal.utilslibrary.deviceutils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.app.Application;
import android.content.Context;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.telephony.TelephonyManager;
import android.text.TextUtils;

import com.renkuo.personal.utilslibrary.shellutils.ShellUtils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.NetworkInterface;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * Device tool
 * Create on 2014年12月6日 下午12:16:36
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
public final class DeviceUtils {
    /**
     * Rom总容量
     *
     * @return
     */

    public static double getTotalRomSize() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        return statFs.getBlockSizeLong() * statFs.getBlockCountLong();
    }

    /**
     * Rom可用容量
     *
     * @return
     */
    public static double getAvailableRomSize() {
        StatFs statFs = new StatFs(Environment.getDataDirectory().getPath());
        return statFs.getBlockSizeLong() * statFs.getAvailableBlocksLong();
    }

    /**
     * Ram总容量
     *
     * @return
     */
    public static double getTotalRamSize() {
        try {
            String line = null;
            RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
            line = reader.readLine();
            Pattern p = Pattern.compile("(\\d+)");
            Matcher matcher = p.matcher(line);
            String value = "";
            while (matcher.find()) {
                value = matcher.group(1);
            }
            reader.close();
            reader = null;
            return Double.parseDouble(value);
        } catch (Exception e) {

        }
        return 0D;
    }

    public static ActivityManager.MemoryInfo getMemoryInfo(@NonNull Context context) {
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        ActivityManager activityManager = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    public static long getExternalStorageTotalSize() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long blockSize;
        long blockCount;
        if (Build.VERSION.SDK_INT >= 18) {
            blockSize = statFs.getBlockSizeLong();
            blockCount = statFs.getBlockCountLong();
        } else {
            blockSize = statFs.getBlockSize();
            blockCount = statFs.getBlockCount();
        }
        return blockSize * blockCount;
    }

    public static long getExternalStorageFreeSize() {
        StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
        long free_memory = 0; //return value is in bytes
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.JELLY_BEAN_MR2) {
            free_memory = statFs.getAvailableBlocksLong() * statFs.getBlockSizeLong();
        } else {
            free_memory = statFs.getAvailableBlocks() * statFs.getBlockSize();
        }
        return free_memory;
    }

    /**
     * Ram可用容量
     *
     * @return
     */
    public static double getAvailableRamSize() {
        try {
            String line = null;
            final Pattern PATTERN = Pattern.compile("([a-zA-Z]+):\\s*(\\d+)");
            RandomAccessFile reader = new RandomAccessFile("/proc/meminfo", "r");
//            double totalSize = getTotalRamSize();
            double availableSize = 0;
            while ((line = reader.readLine()) != null) {
                Matcher m = PATTERN.matcher(line);
                if (m.find()) {
                    String name = m.group(1);
                    String size = m.group(2);
                    if (name.equalsIgnoreCase("MemFree") || name.equalsIgnoreCase("Buffers") ||
                            name.equalsIgnoreCase("Cached") || name.equalsIgnoreCase("SwapFree")) {
                        availableSize += Long.parseLong(size);
                    }
                }
            }
            reader.close();
            reader = null;
            return availableSize;
        } catch (Exception e) {
        }
        return 0D;
    }

    public static String getSerial() {
        if (TextUtils.equals(Build.UNKNOWN, Build.SERIAL)) {
            Context mCtx = getApplicationContext();
            if (mCtx != null) {
                return getSerial(mCtx);
            }
        }
        return Build.SERIAL;
    }


    private static Context getApplicationContext() {
        try {
            Class clzz = Class.forName("android.app.ActivityThread");
            Application app = (Application) clzz.getDeclaredMethod("currentApplication").invoke(null);
            return app.getApplicationContext();
        } catch (Exception e) {
            return null;
        }
    }


    public static String getSerial(Context mCtx) {
        String androidId = Settings.Secure.getString(mCtx.getContentResolver(), Settings.Secure.ANDROID_ID);
        return TextUtils.isEmpty(androidId) ? getBluetoothMacAddress(mCtx).replace(":", "") : androidId.trim();
    }


    public static String toString(Object o, String nullDefault) {
        return (o != null) ? o.toString() : nullDefault;
    }

    /**
     * 获取wifi的mac地址
     * @return
     */
    public static String getWifiMacAddress() {
        try {
            String interfaceName = "wlan0";
            List<NetworkInterface> interfaces = Collections.list(NetworkInterface.getNetworkInterfaces());
            for (NetworkInterface intf : interfaces) {
                if (!intf.getName().equalsIgnoreCase(interfaceName)) {
                    continue;
                }
                byte[] mac = intf.getHardwareAddress();
                if (mac == null) {
                    return "";
                }
                StringBuilder buf = new StringBuilder();
                for (byte aMac : mac) {
                    buf.append(String.format("%02X:", aMac));
                }
                if (buf.length() > 0) {
                    buf.deleteCharAt(buf.length() - 1);
                }
                return buf.toString();
            }
        } catch (Exception ex) {
            return "02:00:00:00:00:00";
        } // for now eat exceptions
        return "02:00:00:00:00:00";
    }

    public static boolean isPowerBoot(@NonNull Context context, @NonNull String packageName) {
        return context.getPackageManager()
                .checkPermission(android.Manifest.permission.RECEIVE_BOOT_COMPLETED, packageName)
                == PackageManager.PERMISSION_GRANTED;
    }


    public static String getRomVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    @SuppressLint("HardwareIds")
    public static String getBluetoothMacAddress(@NonNull Context context) {
        return android.provider.Settings.Secure.getString(context.getContentResolver(), "bluetooth_address");
    }


    public static String buildSystemDirInfo() {
        StringBuilder builder = new StringBuilder();
        ShellUtils.CommandResult result = ShellUtils.execCommand("ls -lZ /system", false, true);
        if (result != null) {
            if (!TextUtils.isEmpty(result.successMsg)) {
                builder.append(result.successMsg);
            }
        }

        ShellUtils.CommandResult resultX = ShellUtils.execCommand("ls -lZ /system/bin", false, true);
        if (resultX != null) {
            if (!TextUtils.isEmpty(resultX.successMsg)) {
                builder.append(resultX.successMsg);
            }
        }

        ShellUtils.CommandResult resultXx = ShellUtils.execCommand("ls -lZ /system/xbin", false, true);
        if (resultXx != null) {
            if (!TextUtils.isEmpty(resultXx.successMsg)) {
                builder.append(resultXx.successMsg);
            }
        }

        return builder.toString();
    }

    /**
     * The system libc.so file path
     */
//    private static final String SYSTEM_LIB_C_PATH = "/system/lib/libc.so";
//    private static final String SYSTEM_LIB_C_PATH_64 = "/system/lib64/libc.so";

//    private static final int ELFCLASS32 = 1;

//    public static int getScreenDensity(@NonNull Context context) {
//        return context.getResources().getDisplayMetrics().densityDpi;
//    }

    /**
     * 设备是否支持该属性
     * <p>
     * Use {@link PackageManager#hasSystemFeature(String)}
     */
    public static boolean isSupportFeature(@NonNull Context context, @NonNull String feature) {
        FeatureInfo[] infos = context.getPackageManager().getSystemAvailableFeatures();
        if (infos == null || infos.length == 0) return false;
        for (FeatureInfo info : infos) {
            if (feature.equals(info.name)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取IMEI
     */
    @SuppressLint("HardwareIds")
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    public static String getIMEI(@NonNull Context context) {
        try {
            TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
            return toString(tm.getDeviceId(), "");
        } catch (SecurityException e) {
//            QLog.e("Requires android.Manifest.permission#READ_PHONE_STATE");
        }
        return "default_imei";
    }

    @SuppressLint("HardwareIds")
    public static String getAndroidId(@NonNull Context context) {
        return Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
    }

    public static boolean isPhone(@NonNull Context context) {
        TelephonyManager telephony = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        return telephony.getPhoneType() != TelephonyManager.PHONE_TYPE_NONE;
    }

    public static boolean isTablet(@NonNull Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    public static boolean isEmulator() {
        return (Build.MODEL.equals("sdk")) || (Build.MODEL.equals("google_sdk"));
    }

    public static boolean isGenymotion() {
        return Build.DEVICE.startsWith("vbox86");
    }


    public static int getHeapSize(@NonNull Context context) {
        ActivityManager aManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        return aManager.getMemoryClass();
    }


    public static String getKernelVersion() {
        String kernelVersion = "";
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream("/proc/version");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return kernelVersion;
        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 8 * 1024);
        String info = "";
        String line = "";
        try {
            while ((line = bufferedReader.readLine()) != null) {
                info += line;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                bufferedReader.close();
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            if (info != "") {
                final String keyword = "version ";
                int index = info.indexOf(keyword);
                line = info.substring(index + keyword.length());
                index = line.indexOf(" ");
                kernelVersion = line.substring(0, index);
            }
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }

        return kernelVersion;
    }

    /**
     * 获取运行时长
     *
     * @return
     */
    public static float getUpTime() {
        try {
            BufferedReader in = new BufferedReader(new FileReader("/proc/uptime"));
            String uptime = in.readLine();
            in.close();
            Pattern split_regex = Pattern.compile("\\s+");
            String[] items = split_regex.split(uptime);
            return Float.parseFloat(items[0]);
        } catch (Exception e) {
            return 0.0f;
        }
    }

    private DeviceUtils() {/*Do not new me!*/}

}
