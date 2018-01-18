    
package com.renkuo.personal.utilslibrary.log;

import android.util.Log;

public class LogUtils {
    private static final String TAG = "LogUtils";

    public static int LEVEL = android.util.Log.DEBUG;

    public static boolean isDebug() {
        return LEVEL <= android.util.Log.DEBUG;
    }

    public static void v(String tag, String msg) {
        if (LEVEL <= android.util.Log.VERBOSE)
            Log.v(tag, msg);
    }

    public static void v(String tag, String format, Object... args) {
        if (LEVEL <= android.util.Log.VERBOSE)
            Log.v(tag, logFormat(format, args));
    }

    public static void d(String tag, String msg) {
        if (LEVEL <= android.util.Log.DEBUG)
            Log.d(tag, msg);
    }

    public static void d(String tag, String format, Object... args) {
        if (LEVEL <= android.util.Log.DEBUG)
            Log.d(tag, logFormat(format, args));
    }

    public static void i(String tag, String msg) {
        if (LEVEL <= android.util.Log.INFO)
            Log.i(tag, msg);
    }

    public static void i(String tag, String format, Object... args) {
        if (LEVEL <= android.util.Log.INFO)
            Log.i(tag, logFormat(format, args));
    }

    public static void w(String tag, String msg) {
        if (LEVEL <= android.util.Log.WARN)
            Log.w(tag, msg);
    }

    public static void w(String tag, String format, Object... args) {
        if (LEVEL <= android.util.Log.WARN) {
            Log.w(tag, logFormat(format, args));
        }
    }

    public static void e(String tag, String msg) {
        if (LEVEL <= android.util.Log.ERROR)
            Log.e(tag, msg);
    }

    public static void e(String tag, String format, Object... args) {
        if (LEVEL <= android.util.Log.ERROR)
            Log.e(tag, logFormat(format, args));
    }

    private static String logFormat(String format, Object... args) {
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String[]) {
                args[i] = prettyArray((String[]) args[i]);
            }
        }
        String s = String.format(format, args);
        s = "[" + Thread.currentThread().getId() + "] " + s;
        return s;
    }

    private static String prettyArray(String[] array) {
        if (array.length == 0) {
            return "[]";
        }

        StringBuilder sb = new StringBuilder("[");
        int len = array.length - 1;
        for (int i = 0; i < len; i++) {
            sb.append(array[i]);
            sb.append(", ");
        }
        sb.append(array[len]);
        sb.append("]");

        return sb.toString();
    }
}
