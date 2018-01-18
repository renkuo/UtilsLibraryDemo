
package com.renkuo.personal.utilslibrary.installutils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.IPackageInstallObserver;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.text.TextUtils;

import com.renkuo.personal.utilslibrary.log.LogUtils;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

//import android.content.pm.IPackageDeleteObserver;
/**
 * 静默安装卸载
 * 调用方式示例
 * InstallSilentUtils.silentInstallApk(mContext, apkPath, pkgName);
 * InstallSilentUtils.silentUninstallApk(mContext, pkgName);
 */
public class InstallSilentUtils {
    private static final String TAG = "InstallSilentUtils";
    private static final int INSTALL_REPLACE_EXISTING = 0x00000002;
    private static final int INSTALL_ALL_USERS = 0x00000040;
    private static final String CAPABILITY_MANAGER_PKG = "com.sec.android.app.capabilitymanager";

    private static int sInstallCount = 0;

    static class PackageInstallObserver extends IPackageInstallObserver.Stub {
        private Context mContext;
        private String mPkgName;
        private int mApi;

        public PackageInstallObserver(Context context) {
            mContext = context;
        }

        public PackageInstallObserver(Context context, int api, String pkgName) {
            mContext = context;
            mApi = api;
            mPkgName = pkgName;
        }

        public void packageInstalled(String packageName, int returnCode) {
            LogUtils.i(TAG, "====INSTALL_COMPLETE packageName " + packageName + ", returnCode " + returnCode);
            subtractInstallCount(mContext);

        }

    };

    /*static class PackageUninstallObserver extends IPackageDeleteObserver.Stub {
        public void packageDeleted(String packageName, int returnCode) {
            LogUtils.i(TAG, "====UNINSTALL_COMPLETE packageName " + packageName + ", returnCode " + returnCode);
        }
    };*/
    
    public static void silentInstallApkAsUser(Context context, String filePath, String packageName) {
        if (TextUtils.isEmpty(filePath)) return;

        PackageManager pm = context.getPackageManager();

        if (TextUtils.isEmpty(packageName)) {
            packageName = getUninstallAppPkgName(pm, filePath);
        }
        LogUtils.i(TAG, " silentInstallApk packageName " + packageName);

        int installFlags = 0;
        if (!TextUtils.isEmpty(packageName)) {
            try {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                LogUtils.i(TAG, " pi " + pi);
                if (pi != null) {
                    installFlags |= INSTALL_REPLACE_EXISTING; //PackageManager.INSTALL_REPLACE_EXISTING
                }
            } catch (NameNotFoundException e) {
                LogUtils.e(TAG, " e " + e);
            }
        } else {
            installFlags |= INSTALL_REPLACE_EXISTING;
            installFlags |= INSTALL_ALL_USERS;
        }

        PackageInstallObserver observer = new PackageInstallObserver(context);

        addInstallCount(context);
        try {
           Class<?> pmClass = Class.forName("android.content.pm.IPackageManager");
           Class<?> vpClass = Class.forName("android.content.pm.VerificationParams");
           Constructor vpCon = vpClass.getConstructor(new Class[]{Uri.class, Uri.class, Uri.class,
                   int.class, Class.forName("android.content.pm.ManifestDigest")});
           Object o = vpCon.newInstance(Uri.fromFile(new File(filePath)),null,null,-1,null);
           Method method = pmClass.getMethod("installPackageAsUser", String.class, Class.forName("android.content.pm.IPackageInstallObserver"), int.class, Class.forName("android.content.pm.VerificationParams"),String.class,int.class);
           method.invoke(pm, filePath, observer, installFlags, packageName,o,null,0);
        } catch (Exception e) {
            subtractInstallCount(context);
            e.printStackTrace();
        }
    }

    public static void silentInstallApk(Context context, String filePath, String packageName, int api) {
        if (TextUtils.isEmpty(filePath)) return;

        PackageManager pm = context.getPackageManager();

        if (TextUtils.isEmpty(packageName)) {
            packageName = getUninstallAppPkgName(pm, filePath);
        }
        LogUtils.i(TAG, " silentInstallApk1 packageName " + packageName);

        int installFlags = 0;
        if (!TextUtils.isEmpty(packageName)) {
            try {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                LogUtils.i(TAG, " pi1 " + pi);
                if (pi != null) {
                    installFlags |= INSTALL_REPLACE_EXISTING; //PackageManager.INSTALL_REPLACE_EXISTING
                }
            } catch (NameNotFoundException e) {
                LogUtils.e(TAG, " e " + e);
            }
        } else {
            installFlags |= INSTALL_REPLACE_EXISTING;
        }

        Uri uri = Uri.fromFile(new File(filePath));
        PackageInstallObserver observer = new PackageInstallObserver(context, api, packageName);
        //pm.installPackage(uri, null, installFlags, packageName);

        addInstallCount(context);
        try {
           Class<?> pmClass = pm.getClass();
           Method method = pmClass.getMethod("installPackage", Uri.class, Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class);
           method.invoke(pm, uri, observer, installFlags, packageName);
        } catch (Exception e) {
            subtractInstallCount(context);
            e.printStackTrace();
        }
    }

    public static void silentInstallApk(Context context, String filePath, String packageName) {
        if (TextUtils.isEmpty(filePath)) return;

        PackageManager pm = context.getPackageManager();

        if (TextUtils.isEmpty(packageName)) {
            packageName = getUninstallAppPkgName(pm, filePath);
        }
        LogUtils.i(TAG, " silentInstallApk packageName " + packageName);

        int installFlags = 0;
        if (!TextUtils.isEmpty(packageName)) {
            try {
                PackageInfo pi = pm.getPackageInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
                LogUtils.i(TAG, " pi " + pi);
                if (pi != null) {
                    installFlags |= INSTALL_REPLACE_EXISTING; //PackageManager.INSTALL_REPLACE_EXISTING
                }
            } catch (NameNotFoundException e) {
                LogUtils.e(TAG, " e " + e);
            }
        } else {
            installFlags |= INSTALL_REPLACE_EXISTING;
        }

        Uri uri = Uri.fromFile(new File(filePath));
        LogUtils.d(TAG,"install uri:" + uri.toString());
        PackageInstallObserver observer = new PackageInstallObserver(context);
        //pm.installPackage(uri, null, installFlags, packageName);

        addInstallCount(context);
        try {
           Class<?> pmClass = pm.getClass();
           Method method = pmClass.getMethod("installPackage", Uri.class, Class.forName("android.content.pm.IPackageInstallObserver"), int.class, String.class);
           method.invoke(pm, uri, observer, installFlags, packageName);
        } catch (Exception e) {
            subtractInstallCount(context);
            e.printStackTrace();
            LogUtils.e(TAG, "excpetion:"+ e);
        }
    }

    public static void silentUninstallApk(Context context, String packageName) {
        if (TextUtils.isEmpty(packageName)) return;

        //PackageUninstallObserver observer = new PackageUninstallObserver();
        PackageManager pm = context.getPackageManager();
        //pm.deletePackage(packageName, null, 0);

        try {
           Class<?> pmClass = pm.getClass();
           Method method = pmClass.getMethod("deletePackage", String.class, Class.forName("android.content.pm.IPackageDeleteObserver"), int.class);
           method.invoke(pm, packageName, null, 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static String getUninstallAppPkgName(PackageManager pm, String archiveFilePath) {
        String pkgName = null;
        try {
            PackageInfo pakinfo = pm.getPackageArchiveInfo(archiveFilePath, PackageManager.GET_ACTIVITIES);
            if (pakinfo != null) {
                ApplicationInfo appInfo = pakinfo.applicationInfo;
                if (appInfo != null) {
                    pkgName = appInfo.packageName;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return pkgName;
    }

    private static void addInstallCount(Context context) {
        LogUtils.i(TAG, " addInstallCount sInstallCount: " + sInstallCount);
        if (sInstallCount == 0) {
            PackageManager pm = context.getPackageManager();
            try {
                pm.setApplicationEnabledSetting(CAPABILITY_MANAGER_PKG, PackageManager.COMPONENT_ENABLED_STATE_DISABLED, 0);
            } catch (Exception e) {
                LogUtils.e(TAG, "e "  + e);
            }
        }

        sInstallCount++;
    }

    private static void subtractInstallCount(Context context) {
        LogUtils.i(TAG, " subtractInstallCount sInstallCount: " + sInstallCount);
        sInstallCount--;

        if (sInstallCount == 0) {
            PackageManager pm = context.getPackageManager();
            try {
                pm.setApplicationEnabledSetting(CAPABILITY_MANAGER_PKG, PackageManager.COMPONENT_ENABLED_STATE_ENABLED, 0);
            } catch (Exception e) {
                LogUtils.e(TAG, "e "  + e);
            }
        }
    }
}
