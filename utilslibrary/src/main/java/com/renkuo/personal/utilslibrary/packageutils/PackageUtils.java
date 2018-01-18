package com.renkuo.personal.utilslibrary.packageutils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;

import java.util.List;

/**
 *
 */
public final class PackageUtils {
//    static final boolean DEBUG = false;

    public static final String APP_NOT_FOUND = "应用程序未安装";
    public static final int FLAG_SYSTEM = 1;
    public static final int FLAG_NOT_SYSTEM = 1 << 1;
    public static final int FLAG_ALL = FLAG_SYSTEM | FLAG_NOT_SYSTEM;

    public static boolean isNewUser(@NonNull Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            PackageInfo pi = pm.getPackageInfo(context.getPackageName(), 0);
            return pi.firstInstallTime == pi.lastUpdateTime;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取app名称
     * @param packageName
     * @param packageManager
     * @return
     */
    public static final String getAppName(String packageName, PackageManager packageManager) {
        try {
            ApplicationInfo info = packageManager.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
            if (info != null) {
                return info.loadLabel(packageManager).toString();
            }
        } catch (Throwable e) {
        }

        return packageName;
    }

    /**
     * 是否监听开机启动
     * @param context
     * @param packageName
     * @return
     */
    public static boolean isPowerBoot(@NonNull Context context, @NonNull String packageName) {
        return context.getPackageManager()
                .checkPermission(Manifest.permission.RECEIVE_BOOT_COMPLETED, packageName)
                == PackageManager.PERMISSION_GRANTED;
    }

    /**
     * 是否忽略电池优化
     * @param context
     * @param packageName
     * @return
     */
    @TargetApi(Build.VERSION_CODES.M)
    @RequiresPermission(Manifest.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS)
    public static boolean isIgnoringBatteryOptimizations(Context context, @NonNull String packageName) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            return powerManager.isIgnoringBatteryOptimizations(packageName);
        } else {
            return true;
        }
    }

    /**
     * 获取系统上可用的功能列表。
     * @param context
     * @return
     */
    public static FeatureInfo[] getSystemAvailableFeatures(Context context) {
        return getPackageManager(context).getSystemAvailableFeatures();
    }

    /**
     * 检查给定的功能名称是否是可用的功能之一
     * @param context
     * @param name
     * @return
     */
    public static boolean hasSystemFeature(Context context, String name) {
        return getPackageManager(context).hasSystemFeature(name);
    }

    /**
     * 获取PackageManager
     * @param context
     * @return
     */
    public static PackageManager getPackageManager(Context context) {
        return context.getPackageManager();
    }

    /**
     * 获取应用信息
     * Application:Information you can retrieve about a particular application
     * @param context
     * @param packageName
     * @return
     */
    public static ApplicationInfo getApplicationInfo(Context context, String packageName) {
        PackageManager pm;
        try {
            pm = context.getPackageManager();
        } catch (RuntimeException e) {
            return null;
        }
        if (pm == null || TextUtils.isEmpty(packageName)) {
            return null;
        }

        try {
            return pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return null;
        }
//        return null;
    }

//    public static ApplicationInfo getApplicationInfo(Context context) {
//        return getApplicationInfo(context, context.getPackageName());
//    }

    /**
     * 通过apk路径获取包信息
     * PackageInfo：information about the contents of a package
     * @param context
     * @param apkPath
     * @return
     */
    public static PackageInfo getPackageInfoByApkPath(Context context, String apkPath) {
        if (TextUtils.isEmpty(apkPath)) return null;
        return getPackageManager(context).getPackageArchiveInfo(apkPath, 0);
    }

    /**
     * 通过apk路径获取包信息
     * eg: codePath = "/base.apk"
     * @param context
     * @param codePath
     * @return
     */
    public static PackageInfo getPackageInfoByApk(Context context, String codePath){
        PackageManager pm = getPackageManager(context);
        return pm.getPackageArchiveInfo(codePath, PackageManager.GET_ACTIVITIES);

    }

    /**
     * 通过包名获取包信息
     * @param context
     * @param packageName
     * @return
     */
    public static PackageInfo getPackageInfo(Context context, String packageName) {
        try {
            return getPackageManager(context).getPackageInfo(packageName, 0);
        } catch (NameNotFoundException e) {
            return null;
        }
    }

    /**
     * 获取uid
     * @param context
     * @param packageName
     * @return
     */
    public static int getUid(Context context, String packageName) {
        ApplicationInfo info = getApplicationInfo(context, packageName);
        if (info != null) {
            return info.uid;
        }
        return -256;
    }

    /**
     * 通过包名判断包是否存在
     * @param context
     * @param targetPackage
     * @return
     */
    public static boolean isPackageExisted(Context context, String targetPackage) {
        PackageManager pm = context.getPackageManager();
        try {
            pm.getPackageInfo(targetPackage, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            return false;
        }
        return true;
    }

    /**
     * 获取ShareUserId
     * @param context
     * @param packageName
     * @return
     */
    public static String getShareUserId(Context context, String packageName) {
        PackageInfo info = getPackageInfo(context, packageName);
        if (info != null) {
            return info.sharedUserId;
        }
        return null;
    }

    /**
     * 获取App的版本号
     */
    public static int getVersionCode(Context context) {
        PackageInfo info = getPackageInfo(context, context.getPackageName());
        if (info != null) {
            return info.versionCode;
        }
        return 0;
    }

    /**
     * 通过apk文件路径获取版本号
     * @param context
     * @param archiveFilePath
     * @return
     */
    public static int getVersionCode(Context context, String archiveFilePath) {
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            return packageInfo.versionCode;
        }

        return 0;
    }

    /**
     * 获取版本名
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        PackageInfo info = getPackageInfo(context, context.getPackageName());
        if (info != null) {
            return info.versionName;
        }
        return "";
    }

    /**
     * 通过uid获取包名
     * @param context
     * @param uid
     * @return
     */
    public static String getNameForUid(Context context, int uid) {
        return context.getPackageManager().getNameForUid(uid);
    }

    /**
     * 获取App的版本名称
     */
    public static String getVersionName(Context context, String archiveFilePath) {
        PackageInfo packageInfo = context.getPackageManager().getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
        if (packageInfo != null) {
            return packageInfo.versionName;
        }
        return "";
    }


    /**
     * 通过apk路径判断是否是一个app
     * @param context
     * @param apkPath
     * @return
     */
    private static boolean isSameApp(Context context, String apkPath) {
        if (TextUtils.isEmpty(apkPath)) return false;

        PackageInfo info = getPackageInfoByApkPath(context, apkPath);
        if (info != null) {
            if (info.packageName.equals(context.getPackageName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * 杀进程
     */
    @RequiresPermission(Manifest.permission.KILL_BACKGROUND_PROCESSES)
    public static void killBackgroundProcesses(@NonNull Context context, String processName) {
        if (TextUtils.isEmpty(processName)) return;

        ActivityManager aManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appInfoList = aManager.getRunningAppProcesses();
        if (appInfoList == null || appInfoList.size() <= 0)
            return;


        for (ActivityManager.RunningAppProcessInfo appInfo : appInfoList) {
            if (processName.equals(appInfo.processName)) {
                String[] pkgList = appInfo.pkgList;
                if (pkgList == null || pkgList.length == 0) continue;
                for (String pkg : pkgList) {
                    aManager.killBackgroundProcesses(pkg);
                }
                return;
            }
        }
    }

    /**
     * 获取已安装Apk文件的源Apk文件
     *
     * @param context
     * @param packageName
     * @return
     */
    public static String getSourceApkPath(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName))
            return null;
        try {
            ApplicationInfo appInfo = getPackageManager(context).getApplicationInfo(packageName, 0);
            return appInfo.sourceDir;
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取应用图标
     * @param packageName
     * @param packageManager
     * @return
     */
    public static Drawable getApplicationIcon(String packageName, PackageManager packageManager) {

        try {
            return packageManager.getApplicationIcon(packageName);
        } catch (NameNotFoundException e) {
            return packageManager.getDefaultActivityIcon();
        } catch (OutOfMemoryError e) {
            System.gc();
            return packageManager.getDefaultActivityIcon();
        } catch (Throwable e) {
            return packageManager.getDefaultActivityIcon();
        }
    }


}
