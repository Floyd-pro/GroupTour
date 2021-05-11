package com.zjnu.grouptour.utils;

import android.util.Base64;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * @author luchen
 * @Date 2021/4/1 10:00
 * @Description Base64类
 */
public class Base64Util {

    public static String PIC_BASE64 = "";
    public static String IMG_CACHE_ID = "";
    /** */
    /**
     * 文件读取缓冲区大小
     */
    private static final int CACHE_SIZE = 1024;

    public static String encode(String text) {
        return Base64.encodeToString(text.getBytes(), Base64.DEFAULT);
    }

    public static String decode(String text) {
        return new String(Base64.decode(text.getBytes(), Base64.DEFAULT));
    }

    /** */
    /**
     * <p>
     * BASE64字符串解码为二进制数据
     * </p>
     *
     * @param base64
     * @return
     * @throws Exception
     */
    public static byte[] decode1(String base64) throws Exception {
        return Base64.decode(base64.getBytes(), Base64.DEFAULT);
    }

    /** */
    /**
     * <p>
     * 二进制数据编码为BASE64字符串
     * </p>
     *
     * @param bytes
     * @return
     * @throws Exception
     */
    public static String encode(byte[] bytes) throws Exception {
        return new String(Base64.encode(bytes, Base64.DEFAULT));
    }

    /** */
    /**
     * <p>
     * 将文件编码为BASE64字符串
     * </p>
     * <p>
     * 大文件慎用，可能会导致内存溢出
     * </p>
     *
     * @param filePath 文件绝对路径
     * @return
     * @throws Exception
     */
    public static String encodeFile(String filePath) throws Exception {
        byte[] bytes = fileToByte(filePath);
        return encode(bytes);
    }

    /** */
    /**
     * <p>
     * BASE64字符串转回文件
     * </p>
     *
     * @param filePath 文件绝对路径
     * @param base64   编码字符串
     * @throws Exception
     */
    public static void decodeToFile(String filePath, String base64) throws Exception {
        byte[] bytes = decode1(base64);
        byteArrayToFile(bytes, filePath);
    }

    /** */
    /**
     * <p>
     * 文件分段转换为二进制数组
     * </p>
     *
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static String splitFileToByte(String filePath) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = new FileInputStream(filePath);
        byte[] byteBuf = new byte[3 * 1000]; //必须3的倍数
        byte[] base64ByteBuf;
        int count; //每次从文件中读取到的有效字节数
        while ((count = in.read(byteBuf)) != -1) {
            if (count != byteBuf.length) //如果有效字节数不为3*1000，则说明文件已经读到尾了，不够填充满byteBuf了
            {
                byte[] copy = Arrays.copyOf(byteBuf, count); //从byteBuf中截取包含有效字节数的字节段
                base64ByteBuf = Base64.encode(copy, Base64.DEFAULT); //对有效字节段进行编码
            } else {
                base64ByteBuf = Base64.encode(byteBuf, Base64.DEFAULT);
            }
            out.write(base64ByteBuf, 0, base64ByteBuf.length);
            out.flush();
        }
        in.close();
        return out.toString();
    }

    /** */
    /**
     * <p>
     * 文件转换为二进制数组
     * </p>
     *
     * @param filePath 文件路径
     * @return
     * @throws Exception
     */
    public static byte[] fileToByte(String filePath) throws Exception {
        byte[] data = new byte[0];
        File file = new File(filePath);
        if (file.exists()) {
            FileInputStream in = new FileInputStream(file);
            ByteArrayOutputStream out = new ByteArrayOutputStream(2048);
            byte[] cache = new byte[CACHE_SIZE];
            int nRead = 0;
            while ((nRead = in.read(cache)) != -1) {
                out.write(cache, 0, nRead);
                out.flush();
            }
            out.close();
            in.close();
            data = out.toByteArray();
        }
        return data;
    }

    /** */
    /**
     * <p>
     * 二进制数据写文件
     * </p>
     *
     * @param bytes    二进制数据
     * @param filePath 文件生成目录
     */
    public static void byteArrayToFile(byte[] bytes, String filePath) throws Exception {
        InputStream in = new ByteArrayInputStream(bytes);
        File destFile = new File(filePath);
        if (!destFile.getParentFile().exists()) {
            destFile.getParentFile().mkdirs();
        }
        destFile.createNewFile();
        OutputStream out = new FileOutputStream(destFile);
        byte[] cache = new byte[CACHE_SIZE];
        int nRead = 0;
        while ((nRead = in.read(cache)) != -1) {
            out.write(cache, 0, nRead);
            out.flush();
        }
        out.close();
        in.close();
    }

}
