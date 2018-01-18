package com.renkuo.personal.utilslibrary.numberutils;

import android.support.annotation.NonNull;


public class NumberUtils {

    public static int parseInt(String numberString) {
        return parseInt(numberString, 10, 0);
    }

    /**
     *  例如：int c=Integer.parseInt("12",8); 表示将可以理解为双引号里的12是个八进制的数，也就是二进制1010，转化为十进制就是10
     * @param numberString
     * @param radix
     * @param defValue
     * @return
     */
    public static int parseInt(String numberString, int radix, int defValue) {
        try {
            return Integer.parseInt(numberString, radix);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static long parseLong(String numberString, long defValue) {
        try {
            return Long.parseLong(numberString);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static double parseDouble(String numberString, double defValue) {
        try {
            return Double.parseDouble(numberString);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static int checkNumberLength(long number) {
        try {
            return String.valueOf(Math.abs(number)).length();
        } catch (Exception e) {
            return -1;
        }
    }

    public static boolean isHexNumber(@NonNull String str) {
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'f') || (c >= 'A' && c <= 'F'))) {
                return false;
            }
        }
        return true;
    }

    public static float parseFloat(String numberString) {
        return parseFloat(numberString, 0f);
    }

    public static float parseFloat(String numberString, float defValue) {
        try {
            return Float.parseFloat(numberString);
        } catch (NumberFormatException e) {
            return defValue;
        }
    }

    public static long parseLong(String numberString) {
        return parseLong(numberString, 0);
    }

    public static double parseDouble(String numberString) {
        return parseDouble(numberString, 0);
    }

    private NumberUtils() {
    }
}
