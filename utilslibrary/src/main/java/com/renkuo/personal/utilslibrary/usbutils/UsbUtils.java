package com.renkuo.personal.utilslibrary.usbutils;

import android.content.Context;
import android.os.Build;
import android.provider.Settings;

public class UsbUtils {

    private UsbUtils(){}

    /**
     * 设备中的Usb Debuging模式是否打开
     *
     * @param mContext
     * @return true 已打开
     */
    public static boolean isUsbDebuggingChecked(Context mContext) {
        return Settings.Secure.getInt(mContext.getContentResolver(), Settings.Global.ADB_ENABLED, 0) == 1;
    }

    /**
     * 打开或关闭usb调试
     * @param isEnable
     * @param mContext
     * @return
     */
    public boolean setAdbDebugState(boolean isEnable, Context mContext) {
        String ADB_ENABLED;
        try {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                ADB_ENABLED = Settings.Secure.ADB_ENABLED;
            } else {
                ADB_ENABLED = Settings.Global.ADB_ENABLED;
            }
            boolean adbOldState = (Settings.Secure.getInt(mContext.getContentResolver(),
                    ADB_ENABLED, 0) > 0);
            if (adbOldState != isEnable) {
                int adbNewState = (isEnable ? 1 : 0);
                return Settings.Secure.putInt(mContext.getContentResolver(), ADB_ENABLED,
                        adbNewState);
            } else {
                return true;
            }
        } catch (Exception e) {
        }
        return false;
    }
}
