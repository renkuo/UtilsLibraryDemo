package com.renkuo.personal.utilslibrary.convertutils;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;


public class ConvertUtils {

    public static long daysBetween(Date begin, Date end) throws ParseException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        begin = sdf.parse(sdf.format(begin));
        end = sdf.parse(sdf.format(end));
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(begin);
        long time1 = calendar.getTimeInMillis();
        calendar.setTime(end);
        long time2 = calendar.getTimeInMillis();
        return (time2 - time1) / (1000 * 3600 * 24);
    }

    /**
     * return a string in human readable format
     *
     * @param bytes 单位 B
     */
    public static String getHumanReadableSizeMore(long bytes) {
        NumberFormat mSizeFormat = NumberFormat.getInstance();
        mSizeFormat.setMaximumFractionDigits(2);
        if (bytes == 0) {
            return "0M";
        } else if (bytes < 1024) {
            return bytes + "B";
        } else if (bytes < 1048576) {
            return mSizeFormat.format(bytes / 1024f) + "K";
        } else if (bytes < 1048576 * 1024) {
            return mSizeFormat.format(bytes / 1024f / 1024f) + "M";
        } else {
            return mSizeFormat.format(bytes / 1024f / 1024f / 1024f) + "G";
        }
    }

    /**
     * B转换为MB
     *
     * @param bytes
     * @return
     */
    public static int bToMb(long bytes) {
        return (int) (bytes / (1024 << 10));
    }
}
