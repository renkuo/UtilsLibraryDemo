package com.renkuo.personal.utilslibrary.encryptionutils;

import android.text.TextUtils;

import com.renkuo.personal.utilslibrary.ioutils.IoUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by renkuo on 2018/1/19.
 */

public class EnCryptionutils {
    /**
     * 获取文件md5
     * @param file
     * @return
     */
    public static String getFileMd5(File file) {
        if (file == null || !file.isFile() || !file.exists())
            return null;
        FileInputStream fis = null;
        try {
            byte[] buffer = new byte[1024];
            int length;
            MessageDigest md = MessageDigest.getInstance("MD5");
            fis = new FileInputStream(file);
            while ((length = fis.read(buffer, 0, 1024)) != -1) {
                md.update(buffer, 0, length);
            }
            return encodeHexString(md.digest());
        } catch (Exception e) {
            return null;
        } finally {
            IoUtils.close(fis);
        }
    }

    /**
     * 对字符串进行MD5加密
     *
     * @param srcBody
     * @return
     */
    public static String getMd5Str(String srcBody) {
        if (!TextUtils.isEmpty(srcBody))
            return parseSign("MD5", srcBody.getBytes());
        return null;
    }

    /**
     * 对字符串进行SHA1加密
     *
     * @param srcBody
     * @return
     */
    public static String getSha1Str(String srcBody) {
        if (!TextUtils.isEmpty(srcBody))
            return parseSign("SHA-1", srcBody.getBytes());
        return null;
    }

    /**
     * 获取文件的sha1值
     * @param file
     * @return
     */
    public static String getSignSha1(File file) {
        if (file == null || !file.isFile() || !file.exists())
            return null;
        FileInputStream in = null;
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-1");
            in = new FileInputStream(file);
            FileChannel ch = in.getChannel();
            MappedByteBuffer buffer = ch.map(FileChannel.MapMode.READ_ONLY, 0, file.length());
            md.update(buffer);
            return encodeHexString(md.digest());
        } catch (Exception e) {
            return null;
        } finally {
            IoUtils.close(in);
        }

    }

    protected static char[] encodeHex(final byte[] data, final char[] toDigits) {
        final int l = data.length;
        final char[] out = new char[l << 1];
        // two characters form the hex value.
        for (int i = 0, j = 0; i < l; i++) {
            out[j++] = toDigits[(0xF0 & data[i]) >>> 4];
            out[j++] = toDigits[0x0F & data[i]];
        }
        return out;
    }


    public static String encodeHexString(final byte[] data) {
        return new String(encodeHex(data, DIGITS_UPPER));
    }


    private EnCryptionutils() {
    }

    private static String parseSign(String algorithm, byte[] input) {
        try {
            MessageDigest mD = MessageDigest.getInstance(algorithm);
            mD.reset();
            mD.update(input);
            return encodeHexString(mD.digest());
        } catch (NoSuchAlgorithmException e) {
            return null;
        }
    }

    private static final char[] DIGITS_LOWER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] DIGITS_UPPER =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};


    /**
     *  利用java原生的摘要实现SHA256加密
     * @param str 加密后的报文
     * @return
     */
    public static String getSHA256StrJava(String str){
        MessageDigest messageDigest;
        String encodeStr = "";
        try {
            messageDigest = MessageDigest.getInstance("SHA-256");
            messageDigest.update(str.getBytes("UTF-8"));
            encodeStr = byte2Hex(messageDigest.digest());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return encodeStr;
    }

    /**
     * 将byte转为16进制
     * @param bytes
     * @return
     */
    private static String byte2Hex(byte[] bytes){
        StringBuffer stringBuffer = new StringBuffer();
        String temp = null;
        for (int i=0;i<bytes.length;i++){
            temp = Integer.toHexString(bytes[i] & 0xFF);
            if (temp.length()==1){
                //1得到一位的进行补0操作
                stringBuffer.append("0");
            }
            stringBuffer.append(temp);
        }
        return stringBuffer.toString();
    }
}
