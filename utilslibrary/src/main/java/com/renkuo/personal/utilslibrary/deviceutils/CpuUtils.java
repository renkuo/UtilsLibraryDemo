package com.renkuo.personal.utilslibrary.deviceutils;


import com.renkuo.personal.utilslibrary.deviceutils.model.CpuInfo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

public class CpuUtils {

    private CpuUtils(){

    }

    public static CpuInfo buildCpuInfo(float cpuUsage) {
        CpuInfo cpuInfo = new CpuInfo();
        cpuInfo.setCorenum(getNumCores());
        cpuInfo.setBits(getArchType());
        cpuInfo.setMode(getCpuModel());
        cpuInfo.setUseage(cpuUsage);
        List<CpuInfo.CpuCoreInfo> data = new ArrayList<>();

        int maxfreq = 0;
        int minfreq = 0;
        int temperature = 0;
        for (int i = 0; i < cpuInfo.getCorenum(); i++) {
            String name = "core" + i;
            int coreMaxFreq = readSystemFile("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_max_freq");
            maxfreq = maxfreq > coreMaxFreq ? maxfreq : coreMaxFreq;
            int coreMinFreq = readSystemFile("/sys/devices/system/cpu/cpu" + i + "/cpufreq/cpuinfo_min_freq");
            if (minfreq == 0 && coreMinFreq != 0 && coreMinFreq != minfreq) {
                minfreq = coreMinFreq;
            }
            minfreq = coreMinFreq < minfreq ? coreMinFreq : minfreq;
            int coreCurFreq = readSystemFile("/sys/devices/system/cpu/cpu" + i + "/cpufreq/scaling_cur_freq");
            temperature += readSystemFile("/sys/devices/virtual/thermal/thermal_zone" + i + "/temp");
//            QLog.i(temperature);
            data.add(new CpuInfo.CpuCoreInfo(name, coreMaxFreq, coreMinFreq, coreCurFreq));
        }
        cpuInfo.setMaxfreq(maxfreq);
        cpuInfo.setMinfreq(minfreq);
        cpuInfo.setTemperature(temperature / cpuInfo.getCorenum());
        cpuInfo.setData(data);
//        QLog.i(ConvertFactory.toJson(cpuInfo));
        return cpuInfo;
    }

    private static int readSystemFile(String path) {
//        QLog.i(path);
        String result = "0";
        try {
            FileReader fr = new FileReader(path);
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            result = text.trim();
            fr.close();
            br.close();
        } catch (Exception e) {
//            QLog.i("Exception");
        }
        return Integer.parseInt(result);
    }

    /**
     * 获取CPU核数
     *
     * @return
     */
    public static int getNumCores() {
        class CpuFilter implements FileFilter {
            @Override
            public boolean accept(File pathname) {
                if (Pattern.matches("cpu[0-9]+", pathname.getName())) {
                    return true;
                }
                return false;
            }
        }

        try {
            //Get directory containing CPU info
            File dir = new File("/sys/devices/system/cpu/");
            //Filter to only list the devices we care about
            File[] files = dir.listFiles(new CpuFilter());
            //Return the number of cores (virtual CPU devices)
            return files.length;
        } catch (Exception e) {
            //Default to return 1 core
            return 1;
        }
    }

    // 获取CPU型号
    public static String getCpuModel() {
        try {
            FileReader fr = new FileReader("/proc/cpuinfo");
            BufferedReader br = new BufferedReader(fr);
            String text = br.readLine();
            String[] array = text.split(":\\s+", 2);
            fr.close();
            br.close();
            return array[1];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

//    /**
//     * ELF文件头 e_indent[EI_CLASS]的取值：ELFCLASS32表示32位目标
//     */
//    private static final int ELFCLASS32 = 1;
    /**
     * ELF文件头 e_indent[EI_CLASS]的取值：ELFCLASS64表示64位目标
     */
    private static final int ELFCLASS64 = 2;

    /**
     * ELF文件头 e_indent[]数组文件类标识索引
     */
    private static final int EI_CLASS = 4;

    /**
     * Check if system libc.so is 32 bit or 64 bit
     */
    private static boolean isLibc64() {
        File libcFile = new File("/system/lib/libc.so");
        if (libcFile != null && libcFile.exists()) {
            byte[] header = readELFHeadrIndentArray(libcFile);
            if (header != null && header[EI_CLASS] == ELFCLASS64) {
//                QLog.d("is 64bit");
                return true;
            }
        }

        File libcFile64 = new File("/system/lib64/libc.so");
        if (libcFile64 != null && libcFile64.exists()) {
            byte[] header = readELFHeadrIndentArray(libcFile64);
            if (header != null && header[EI_CLASS] == ELFCLASS64) {
//                QLog.d(" is 64bit");
                return true;
            }
        }

        return false;
    }

    /**
     * ELF文件头格式是固定的:文件开始是一个16字节的byte数组e_indent[16]
     * e_indent[4]的值可以判断ELF是32位还是64位
     */
    private static byte[] readELFHeadrIndentArray(File libFile) {
        if (libFile != null && libFile.exists()) {
            FileInputStream inputStream = null;
            try {
                inputStream = new FileInputStream(libFile);
                if (inputStream != null) {
                    byte[] tempBuffer = new byte[16];
                    int count = inputStream.read(tempBuffer, 0, 16);
                    if (count == 16) {
                        return tempBuffer;
                    } else {
//                        QLog.e("Error: e_indent lenght should be 16, but actual is " + count);
                    }
                }
            } catch (Throwable t) {
//                QLog.e("Error:" + t.toString());
            } finally {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        return null;
    }

//    private static final String CPU_ARCHITECTURE_KEY_64 = "ro.product.cpu.abilist64";
//    private static final String CPU_ARCHITECTURE_TYPE_32 = "32";
//    private static final String CPU_ARCHITECTURE_TYPE_64 = "64";

    /**
     * Get the CPU arch type: x32 or x64
     */
    public static String getArchType() {
        if (getSystemProperty("ro.product.cpu.abilist64", "").length() > 0) {

            return "64";
        } else if (isCPUInfo64()) {
            return "64";
        } else if (isLibc64()) {
            return "64";
        } else {
            return "32";
        }
    }

//    private static final String PROC_CPU_INFO_PATH = "/proc/cpuinfo";

    /**
     * Read the first line of "/proc/cpuinfo" file, and check if it is 64 bit.
     */
    private static boolean isCPUInfo64() {
        File cpuInfo = new File("/proc/cpuinfo");
        if (cpuInfo != null && cpuInfo.exists()) {
            InputStream inputStream = null;
            BufferedReader bufferedReader = null;
            try {
                inputStream = new FileInputStream(cpuInfo);
                bufferedReader = new BufferedReader(new InputStreamReader(inputStream), 512);
                String line = bufferedReader.readLine();
                if (line != null && line.length() > 0 && line.toLowerCase(Locale.US).contains("arch64")) {
                    return true;
                }
            } catch (Throwable t) {
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (bufferedReader != null) {
                        bufferedReader.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return false;
    }

    private static String getSystemProperty(String key, String defaultValue) {
        String value = defaultValue;
        try {
            Class<?> clazz = Class.forName("android.os.SystemProperties");
            Method get = clazz.getMethod("get", String.class, String.class);
            value = (String) (get.invoke(clazz, key, ""));
        } catch (Exception e) {
//            QLog.d("key = " + key + ", error = " + e.getMessage());
        }

//        QLog.d(key + " = " + value);
        return value;
    }


    /**
     * 获取系统总CPU使用时间
     *
     * @return 总CPU使用时间
     */
    private static long getTotalCpuTime() {
        WeakReference<String[]> cpuInsfoWrf = null;
        try {
            FileInputStream fis = new FileInputStream("/proc/stat");
            InputStreamReader isReader = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(isReader, 1000);
            String load = reader.readLine();
            fis.close();
            isReader.close();
            reader.close();
            cpuInsfoWrf = new WeakReference<String[]>(load.split(" "));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        long totalCpu = Long.parseLong(cpuInsfoWrf.get()[2])
                + Long.parseLong(cpuInsfoWrf.get()[3]) + Long.parseLong(cpuInsfoWrf.get()[4])
                + Long.parseLong(cpuInsfoWrf.get()[6]) + Long.parseLong(cpuInsfoWrf.get()[5])
                + Long.parseLong(cpuInsfoWrf.get()[7]) + Long.parseLong(cpuInsfoWrf.get()[8]);
        return totalCpu;
    }

    /**
     * 获取除IO等待时间以外的其它等待时间
     *
     * @return 除IO等待时间以外的其它等待时间
     */
    private static long getIdleCpuTime() {
        WeakReference<String[]> cpuInsfoWrf = null;
        try {
            FileInputStream fis = new FileInputStream("/proc/stat");
            InputStreamReader in = new InputStreamReader(fis);
            BufferedReader reader = new BufferedReader(in, 1000);
            String load = reader.readLine();
            fis.close();
            in.close();
            reader.close();
            cpuInsfoWrf = new WeakReference<String[]>(load.split(" "));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return Long.parseLong(cpuInsfoWrf.get()[5]);
    }


    public static float getCupUseage() {
        try {
            float totalCpuTime1 = getTotalCpuTime();
            float idleCupTime1 = getIdleCpuTime();
            Thread.sleep(1000);
            float totalCpuTime2 = getTotalCpuTime();
            float idleCupTime2 = getIdleCpuTime();
            //cpu使用率
            float cpuUsageRate = (100 * ((totalCpuTime1 - totalCpuTime2) - (idleCupTime1 - idleCupTime2)) / (totalCpuTime1 - totalCpuTime2));
//            QLog.i("  " + cpuUsageRate);
            if (cpuUsageRate > 100)
                return 100;
            else if (cpuUsageRate < 0)
                return 0;
            return cpuUsageRate / 100;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0f;
    }

}
