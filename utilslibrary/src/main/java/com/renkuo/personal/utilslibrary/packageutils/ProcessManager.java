/*
 * Copyright (C) 2015. Jared Rummler <jared.rummler@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.renkuo.personal.utilslibrary.packageutils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;

import com.renkuo.personal.utilslibrary.log.QLog;
import com.renkuo.personal.utilslibrary.packageutils.model.AndroidAppProcess;
import com.renkuo.personal.utilslibrary.packageutils.model.AndroidProcess;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class ProcessManager {

    /**
     * 获取所有运行的进程
     * @return
     */
    public static List<AndroidProcess> getRunningProcesses() {
        List<AndroidProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    processes.add(new AndroidProcess(pid));
                } catch (IOException e) {
                    QLog.e(e);
                    // System apps will not be readable on Android 5.0+ if SELinux is enforcing.
                    // You will need root access or an elevated SELinux context to read all files under /proc.
                }
            }
        }
        return processes;
    }

    /**
     * 获取所有运行的app进程
     * @return
     */
    public static List<AndroidAppProcess> getRunningAppProcesses() {
        List<AndroidAppProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    processes.add(new AndroidAppProcess(pid));
                } catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                } catch (IOException e) {
                    QLog.e(e);
                    // System apps will not be readable on Android 5.0+ if SELinux is enforcing.
                    // You will need root access or an elevated SELinux context to read all files under /proc.
                }
            }
        }
        return processes;
    }

    /**
     * 获取正在运行的前台应用
     * @param ctx
     * @return
     */
    public static List<AndroidAppProcess> getRunningForegroundApps(Context ctx) {
        List<AndroidAppProcess> processes = new ArrayList<>();
        File[] files = new File("/proc").listFiles();
        PackageManager pm = ctx.getPackageManager();
        for (File file : files) {
            if (file.isDirectory()) {
                int pid;
                try {
                    pid = Integer.parseInt(file.getName());
                } catch (NumberFormatException e) {
                    continue;
                }
                try {
                    AndroidAppProcess process = new AndroidAppProcess(pid);
                    if (process.foreground
                            // ignore system processes. First app user starts at 10000.
                            && (process.uid < 1000 || process.uid > 9999)
                            // ignore processes that are not running in the default app process.
                            && !process.name.contains(":")
                            // Ignore processes that the user cannot launch.
                            && pm.getLaunchIntentForPackage(process.getPackageName()) != null) {
                        processes.add(process);
                    }
                } catch (AndroidAppProcess.NotAndroidAppProcessException ignored) {
                } catch (IOException e) {
                    QLog.e(e);
                    // System apps will not be readable on Android 5.0+ if SELinux is enforcing.
                    // You will need root access or an elevated SELinux context to read all files under /proc.
                }
            }
        }
        return processes;
    }

    /**
     * 判断自己是否在前台运行
     * @return
     */
    public static boolean isMyProcessInTheForeground() {
        List<AndroidAppProcess> processes = getRunningAppProcesses();
        int myPid = android.os.Process.myPid();
        for (AndroidAppProcess process : processes) {
            if (process.pid == myPid && process.foreground) {
                return true;
            }
        }
        return false;
    }

    /**
     * 获取正在运行的应用进程信息
     * @param ctx
     * @return
     */
    public static List<ActivityManager.RunningAppProcessInfo> getRunningAppProcessInfo(Context ctx) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
            List<AndroidAppProcess> runningAppProcesses = ProcessManager.getRunningAppProcesses();
            List<ActivityManager.RunningAppProcessInfo> appProcessInfos = new ArrayList<>();
            for (AndroidAppProcess process : runningAppProcesses) {
                ActivityManager.RunningAppProcessInfo info = new ActivityManager.RunningAppProcessInfo(process.name, process.pid, null);
                info.uid = process.uid;
                // TODO: Get more information about the process. pkgList, importance, lru, etc.
                appProcessInfos.add(info);
            }
            return appProcessInfos;
        }
        ActivityManager am = (ActivityManager) ctx.getSystemService(Context.ACTIVITY_SERVICE);
        return am.getRunningAppProcesses();
    }

    /**
     * 获取进程名称
     * @param pid
     * @return
     */
    public static String getProcessName(int pid) {
        try {
            File file = new File("/proc/" + pid + "/cmdline");
            BufferedReader reader = new BufferedReader(new FileReader(file));
            String processName = reader.readLine().trim();
            reader.close();
            return processName;
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    private ProcessManager() {
        throw new AssertionError("no instances");
    }

    /**
     * Comparator to list processes by name
     */
    public static final class ProcessComparator implements Comparator<AndroidProcess> {

        @Override
        public int compare(AndroidProcess p1, AndroidProcess p2) {
            return p1.name.compareToIgnoreCase(p2.name);
        }

    }

}
