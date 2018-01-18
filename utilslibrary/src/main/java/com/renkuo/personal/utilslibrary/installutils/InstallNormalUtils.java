package com.renkuo.personal.utilslibrary.installutils;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;

import java.io.File;

/**
 * Created by renkuo on 2018/1/18.
 */

public class InstallNormalUtils {
    /**
     * 通过filepath正常安装
     * @param context
     * @param filePath
     */
    public static void installNormal(@NonNull Context context, @NonNull String filePath) {
        File file = new File(filePath);
        if (!file.exists() || !file.isFile() || file.length() <= 0) {
            throw new IllegalArgumentException("filePath is not valid!");
        }
        installNormal(context, Uri.parse("file://" + filePath));
    }

    public static void installNormal(Context context, Uri uri) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setDataAndType(uri, "application/vnd.android.package-archive");
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
    }

    /**
     * 正常卸载
     * @param context
     * @param packageName
     * @return
     */
    public static boolean uninstallNormal(@NonNull Context context, @NonNull String packageName) {
        Intent i = new Intent(Intent.ACTION_DELETE,
                Uri.parse("32".concat("package:").concat(packageName)));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(i);
        return true;
    }
}
