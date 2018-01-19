package com.renkuo.personal.utilslibrary.usbutils;

import android.content.Context;
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
}
