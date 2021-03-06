package com.renkuo.personal.utilslibrary.packageutils;

import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Map;

/**
 * 提供获取指定包名的ClassLoader,获取Dalvik和system的Context，设置activity当前线程等
 */
public class PackageUtilsSystem {
//    private static String TAG = PackageUtilsSystem.class.getSimpleName();

    private static Object sCurrentActivityThread = null;
    private final static Object sCurrentActivityThreadLock = new Object();

    private static Context sSystemContext = null;
    private final static Object sSystemContextLock = new Object();
    private static Context friendContext = null;
    private final static Object friendContextLock = new Object();

    private static Application sCurrentApplication = null;
    private final static Object sCurrentApplicationLock = new Object();

    //private static final HashMap<String, ClassLoader> mPackages = new HashMap<>();

    private PackageUtilsSystem() {
        if (Build.VERSION.SDK_INT >= 18) {
            setCurrentActivityThread();
            setApplication();
        }
    }

    public Application getApplication() {
        return sCurrentApplication;
    }

    public void setApplication() {
        if (sCurrentApplication != null) {
            return;
        }

        synchronized (sCurrentApplicationLock) {
            if (sCurrentApplication != null) {
                return;
            }

            try {
                Class<?> cls = Class.forName("android.app.ActivityThread");
                Method method = cls.getDeclaredMethod("currentApplication");
                method.setAccessible(true);
                sCurrentApplication = (Application) method.invoke(null);
            } catch (Exception e) {
//                MLog.LogE(TAG, "", e);
            }
        }
    }

    /**
     * 获取系统的Context
     * @return
     */
    public Context getSystemContext() {
        if (sSystemContext == null) {
            synchronized (sSystemContextLock) {
                if (sSystemContext != null) {
                    return sSystemContext;
                }

                try {
                    Class<?> cls = Class.forName("android.app.ActivityThread");
                    Field field = cls.getDeclaredField("mSystemContext");
                    field.setAccessible(true);
                    if ((field.getModifiers() & Modifier.STATIC) == Modifier.STATIC) {
                        sSystemContext = (Context) field.get(null);
                    } else {
                        if (sCurrentActivityThread == null) {
                            setCurrentActivityThread();
                        }
                        sSystemContext = (Context) field.get(sCurrentActivityThread);
                    }
                } catch (Exception e) {
//                    MLog.LogE(TAG, "", e);
                }
            }
        }
        return sSystemContext;
    }

    /**
     * 设置当前Activity线程
     */
    public void setCurrentActivityThread() {
        if (sCurrentActivityThread == null) {
            synchronized (sCurrentActivityThreadLock) {
                if (sCurrentActivityThread != null) {
                    return;
                }

                try {
                    Class<?> ActivityThreadClass = Class.forName("android.app.ActivityThread");
                    Method method = ActivityThreadClass.getMethod("currentActivityThread");
                    method.setAccessible(true);
                    sCurrentActivityThread = method.invoke(null);
                } catch (Exception e) {
//                    MLog.LogE(TAG, "", e);
                }
            }
        }
    }

    /**
     * 获取指定包名对应的ClassLoader
     * @param packageName
     * @return
     */
    public ClassLoader getPackageClassLoader(String packageName) {
        try {
            Field packageField = sCurrentActivityThread.getClass().getDeclaredField("mPackages");
            packageField.setAccessible(true);
            Map<?, ?> mPackages = (Map<?, ?>) packageField.get(sCurrentActivityThread);

            WeakReference<?> wr = (WeakReference<?>) mPackages.get(packageName);
            Object apkObject = wr.get();
            Field clField = apkObject.getClass().getDeclaredField("mClassLoader");
            clField.setAccessible(true);

            return (ClassLoader) clField.get(apkObject);
        } catch (Exception e) {
//            MLog.LogE(TAG, "find class loader failed", e);
        }
        return null;
    }

//    private static final PackageUtilsSystem mInstance = new PackageUtilsSystem();

    /**
     * 静态内部类单例模式
     */
    private static class SingletonHolder {
        private static final PackageUtilsSystem INSTANCE = new PackageUtilsSystem();
    }

    public static synchronized PackageUtilsSystem getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public Context getFriendContext() {
        try {
            if (friendContext == null) {
                synchronized (friendContextLock) {
                    if (friendContext != null) {
                        return friendContext;
                    }
                    Class clzz = Class.forName("android.app.ActivityThread");
                    Application app = (Application) clzz.getDeclaredMethod("currentApplication").invoke(null);
                    friendContext = app.createPackageContext("com.qihoo360.vehiclesafe", Context.CONTEXT_IGNORE_SECURITY);
                }
            }
        } catch (Exception e) {

        }
        return friendContext;
    }

    /**
     * 获取Dalvik()的context
     * @return
     */
    public static Context getContext_Dalvik() {
        Context context = null;
        try {
            Class<?> cActivityManagerService = Class.forName("com.android.server.am.ActivityManagerService");
//            Method mSelf = cActivityManagerService.getMethod("self", null);
            Method mSelf = cActivityManagerService.getMethod("self", new Class[0]);
//            Object oActivityManagerService = mSelf.invoke(null, null);
            Object oActivityManagerService = mSelf.invoke(null, new Object[]{});
            Field field = cActivityManagerService.getDeclaredField("mContext");
            field.setAccessible(true);
            context = (Context) field.get(oActivityManagerService);
        } catch (Exception e) {
            Log.e("QihooSanbox", "e4 " + e);
        }
        return context;
    }
}
