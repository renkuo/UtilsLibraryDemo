package com.renkuo.personal.utilslibrary.packageutils;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.FeatureInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.Signature;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresPermission;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.renkuo.personal.utilslibrary.ioutils.IoUtils;
import com.renkuo.personal.utilslibrary.log.QLog;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

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

    /**
     * 通过反射加载安装包Resources
     */
    public static Resources getApkResByRefrect(Context context, String apkFilePath) {
        Method addAssetPathMethod = null;
        Object instance = null;
        Class<?> clazz = null;
        Resources apkRes = null;
        try {
            clazz = Class.forName("android.content.res.AssetManager");
            instance = clazz.newInstance();
            addAssetPathMethod = clazz.getMethod("addAssetPath", String.class);
            addAssetPathMethod.invoke(instance, apkFilePath);
            Resources res = context.getResources();
            apkRes = new Resources((AssetManager) instance, res.getDisplayMetrics(), res.getConfiguration());
        } catch (Throwable e) {
            QLog.e("Class.forName(\"android.content.res.AssetManager\") error", e);
        }
        return apkRes;
    }

    /**
     * 判断是否为系统应用
     * @param context
     * @param packageName
     * @return false 非系统应用  true 系统应用
     * @throws NameNotFoundException
     */
    public static boolean isSystemApp(Context context, String packageName) throws NameNotFoundException {
        PackageManager pm = getPackageManager(context);
        ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        final List<Map<String, String>> bootLst = new ArrayList<>();
        if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {//非系统应用
            return false;
        }else {
            return true;
        }
    }

    /**
     * 获取自启非系统应用
     * @param appList
     * @return
     */
    public static List<Map<String, String>> getStartUpApps(List<ApplicationInfo> appList) {
        final List<Map<String, String>> bootLst = new ArrayList<>();
        for (ApplicationInfo packageInfo : appList) {
            if ((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {//非系统应用
                Map<String, String> map = parsePackageItem(packageInfo.sourceDir);
                if (map != null && map.size() > 0)
                    bootLst.add(map);
            }
        }
        return bootLst;
    }

    public static Map<String, String> parsePackageItem(String apkPath) {
        Map<String, String> item = null;
        try {
            Class<?> clz_PackageParser = Class.forName("android.content.pm.PackageParser");
            if (null == clz_PackageParser) {
                return null;
            }
            Class<?> clz_Package = Class.forName("android.content.pm.PackageParser$Package");
            if (null == clz_Package) {
                return null;
            }
            Class<?> clz_Activity = Class.forName("android.content.pm.PackageParser$Activity");
            if (null == clz_Activity) {
                return null;
            }
            Class<?> clz_Component = Class.forName("android.content.pm.PackageParser$Component");
            if (null == clz_Component)
                return null;
            Object obj_PackageParser = buildPackageParser(clz_PackageParser, apkPath);
            QLog.e(obj_PackageParser);
            if (null == obj_PackageParser) {
                return null;
            }
            Object mPkg = buildInvokePackageObj(clz_PackageParser, obj_PackageParser, apkPath);
            if (null == mPkg) {
//                met_parsePackage.setAccessible(false);
                return null;
            }
            Field fld_receivers = clz_Package.getDeclaredField("receivers");
            QLog.e(fld_receivers);
            if (fld_receivers == null) {
                return null;
            }
            fld_receivers.setAccessible(true);
            Type tp_receivers = fld_receivers.getGenericType();
            QLog.e(tp_receivers);
            if (tp_receivers == null) {
                return null;
            }
            Object obj_receivers = fld_receivers.get(mPkg);
            QLog.e(obj_receivers);
            if (obj_receivers == null) {
                return null;
            }
            Field fld_intents = clz_Component.getDeclaredField("intents");
            fld_intents.setAccessible(true);
            QLog.e(fld_intents);
            Field fld_info = clz_Activity.getDeclaredField("info");


            ArrayList<?> list_receivers = (ArrayList<?>) obj_receivers;
            if (list_receivers != null && list_receivers.size() > 0) {
                for (int i = 0; i < list_receivers.size(); i++) {
                    Object obj_Activity = list_receivers.get(i);
                    if (obj_Activity == null) {
                        continue;
                    }
                    Object obj_intents = fld_intents.get(obj_Activity);
                    ArrayList<?> list_intents = (ArrayList<?>) obj_intents;
                    for (int j = 0; list_intents != null && j < list_intents.size(); j++) {
                        Object obj = list_intents.get(j);
                        if (obj == null)
                            continue;
                        IntentFilter filter = (IntentFilter) obj;
                        for (int k = 0; k < filter.countActions(); k++) {
                            if (TextUtils.equals(filter.getAction(k), Intent.ACTION_BOOT_COMPLETED)) {
                                ActivityInfo ai = (ActivityInfo) fld_info.get(obj_Activity);
                                if (ai == null) {
                                    continue;
                                }
                                if (item == null) {
                                    item = new IdentityHashMap<>();
                                }
//                                QLog.i("packageName:" + ai.packageName + "  receiverName:" + ai.name);
                                item.put(new String(ai.packageName), ai.name);
//                                map.put(new String(ai.packageName), ai.name);
//                                mReceiversAndService.add(ai.name);
//                                QLog.i(filter.getAction(k));
                                break;
                            }
                        }
                    }
                }
            }


        } catch (Exception e) {
            QLog.e(e);
        }
        return item;
    }

    private static Object buildPackageParser(Class<?> packageParser, String archiveSourcePath) {
        Constructor constructor = null;
        Object packageParserObj = null;
        try {
            constructor = packageParser.getConstructor(new Class[]{String.class});
            constructor.setAccessible(true);
            packageParserObj = constructor.newInstance(new Object[]{archiveSourcePath});
        } catch (Exception e) {
            try {
                constructor = packageParser.getConstructor();
                constructor.setAccessible(true);
                packageParserObj = constructor.newInstance();
            } catch (Exception e1) {
                QLog.e(e1);
            }
        }
        return packageParserObj;
    }

    private static Object buildInvokePackageObj(Class<?> clz_PackageParser, Object obj_PackageParser, String archiveSourcePath) {
        Method met_parsePackage = null;
        final File sourceFile = new File(archiveSourcePath);

        try {
            met_parsePackage = clz_PackageParser.getDeclaredMethod("parsePackage", new Class<?>[]{File.class, String.class, DisplayMetrics.class, int.class});
            if (null == met_parsePackage) {
                return null;
            }
            met_parsePackage.setAccessible(true);
            DisplayMetrics mMetrics = new DisplayMetrics();
            mMetrics.setToDefaults();
            return met_parsePackage.invoke(obj_PackageParser, sourceFile, null, mMetrics, 0);

        } catch (Exception e) {
            try {
                met_parsePackage = clz_PackageParser.getDeclaredMethod("parsePackage", new Class<?>[]{File.class, int.class});
                if (null == met_parsePackage) {
                    return null;
                }
                met_parsePackage.setAccessible(true);
                return met_parsePackage.invoke(obj_PackageParser, new Object[]{sourceFile, 0});

            } catch (Exception e1) {
                QLog.e(e1);
                return null;
            }
        }

    }

    /**
     * 获取App权限
     */
    public static String[] getAppPermissions(Context context, String packageName) {
        if (context == null) return new String[0];

        PackageManager pManager = context.getPackageManager();
        try {
            return pManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS).requestedPermissions;
        } catch (NameNotFoundException e) {
            QLog.e(e);
        }
        return new String[0];
    }

    /**
     * 获取签名md5
     * @param mContext
     * @return
     */
    public static String getSignMd5(Context mContext) {
        try {
            PackageManager pMgr = mContext.getPackageManager();
            PackageInfo packageInfo = pMgr.getPackageInfo(mContext.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            return parseSign("MD5", signs[0].toByteArray());
        } catch (PackageManager.NameNotFoundException e) {
        }
        return "";
    }


    /**
     * 获取签名sha1
     * @param context
     * @return
     */
    public static String getSignSha1(Context context) {
        ByteArrayInputStream bis = null;
        try {
            PackageInfo packageInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_SIGNATURES);
            Signature[] signs = packageInfo.signatures;
            CertificateFactory certFactory = CertificateFactory.getInstance("X.509");
            bis = new ByteArrayInputStream(signs[0].toByteArray());
            X509Certificate cert = (X509Certificate) certFactory.generateCertificate(bis);
            return parseSign("SHA1", cert.getEncoded());
        } catch (Exception e) {
        } finally {
            IoUtils.close(bis);
        }
        return "";
    }

    private static String parseSign(String algorithm, byte[] input) {
        try {
            MessageDigest mD = MessageDigest.getInstance(algorithm);
            mD.reset();
            mD.update(input);
            return encodeHexString(mD.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    public static String encodeHexString(final byte[] data) {
        return new String(encodeHex(data, DIGITS_UPPER));
    }

    protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }

    private static final char[] DIGITS_UPPER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};

}
