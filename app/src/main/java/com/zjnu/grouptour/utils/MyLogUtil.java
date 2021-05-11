package com.zjnu.grouptour.utils;

import android.os.Environment;
import android.util.Log;

import com.zjnu.grouptour.ApplicationExlike;
import com.zjnu.grouptour.api.GroupTourApi;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

/**
 * @author luchen
 * @Date 2021/4/1 10:00
 * @Description 基础功能类
 */
public class MyLogUtil {
    private static int num = 1500;

    public static void LogV(String msg) {
        if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
            while (msg.length() > num) {
                Log.v("luchen", msg.substring(0, num));
                msg = msg.substring(num);
            }
            Log.v("luchen", msg);
            writeTextFileAppend("luchen", msg);
        }
    }

    public static void LogV(String tag, String msg) {
        if(tag != null && tag.length() > 0) {
            if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
                while (msg.length() > num) {
                    Log.v(tag, msg.substring(0, num));
                    msg = msg.substring(num);
                }
                Log.v(tag, msg);
                writeTextFileAppend("luchen", msg);
            }
        }
    }

    public static void LogD(String msg) {
        if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
            while (msg.length() > num) {
                Log.d("luchen", msg.substring(0, num));
                msg = msg.substring(num);
            }
            Log.d("luchen", msg);
            writeTextFileAppend("luchen", msg);
        }
    }

    public static void LogD(String tag, String msg) {
        if(tag != null && tag.length() > 0) {
            if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
                while (msg.length() > num) {
                    Log.d(tag, msg.substring(0, num));
                    msg = msg.substring(num);
                }
                Log.d(tag, msg);
                writeTextFileAppend("luchen", msg);
            }
        }
    }

    public static void LogI(String msg) {
        if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
            while (msg.length() > num) {
                Log.i("luchen", msg.substring(0, num));
                msg = msg.substring(num);
            }
            Log.i("luchen", msg);
            writeTextFileAppend("luchen", msg);
        }
    }

    public static void LogI(String tag, String msg) {
        if(tag != null && tag.length() > 0) {
            if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
                while (msg.length() > num) {
                    Log.i(tag, msg.substring(0, num));
                    msg = msg.substring(num);
                }
                Log.i(tag, msg);
                writeTextFileAppend("luchen", msg);
            }
        }
    }

    public static void LogW(String msg) {
        if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
            while (msg.length() > num) {
                Log.w("luchen", msg.substring(0, num));
                msg = msg.substring(num);
            }
            Log.w("luchen", msg);
            writeTextFileAppend("luchen", msg);
        }
    }

    public static void LogW(String tag, String msg) {
        if(tag != null && tag.length() > 0) {
            if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
                while (msg.length() > num) {
                    Log.w(tag, msg.substring(0, num));
                    msg = msg.substring(num);
                }
                Log.w(tag, msg);
                writeTextFileAppend("luchen", msg);
            }
        }
    }

    public static void LogE(String msg) {
        if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
            while (msg.length() > num) {
                Log.e("luchen", msg.substring(0, num));
                msg = msg.substring(num);
            }
            Log.e("luchen", msg);
            writeTextFileAppend("luchen", msg);
        }
    }

    public static void LogE(String tag, String msg) {
        if(tag != null && tag.length() > 0) {
            if (GroupTourApi.getInstance().isOpenLogger() && msg != null) {
                while (msg.length() > num) {
                    Log.e(tag, msg.substring(0, num));
                    msg = msg.substring(num);
                }
                Log.e(tag, msg);
                writeTextFileAppend("luchen", msg);
            }
        }
    }

    public static void LogMap(String tag, String msg, Map map){
        if(GroupTourApi.getInstance().isOpenLogger()) {
            StringBuffer sb = new StringBuffer();
            sb.append(msg);
            Iterator entries = map.entrySet().iterator();

            while (entries.hasNext()) {
                Map.Entry entry = (Map.Entry) entries.next();
                String key = (String)entry.getKey();
                String value = (String)entry.getValue();
                sb.append(key + "=" + value + "&");
            }
            LogI(tag,sb.toString());
            writeTextFileAppend(tag,msg);
        }
    }

    public static File getRootDir() {
        String state = Environment.getExternalStorageState();
        File rootDir;
        rootDir = state.equals(Environment.MEDIA_MOUNTED) ? Environment.getExternalStorageDirectory() : ApplicationExlike.getInstance().getFilesDir();
        return rootDir;
    }

    public static SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");  //日期格式
    public static SimpleDateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");
    public static SimpleDateFormat dateFormat3 = new SimpleDateFormat("yyyyMMdd");

    public static void writeTextFileAppend(String tag, String str) {
        if(GroupTourApi.getInstance().isOpenLogger()) {
            File file = new File(getRootDir() + "/grouptour/" + tag + "_" + dateFormat3.format(new Date()) + ".txt");
            if(file.exists()) // 删除旧的日志
                file.delete();
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            BufferedWriter out = null;
            str = dateFormat.format(new Date()) + "\n" + str;
            str += "\n";
            try {
                out = new BufferedWriter(new OutputStreamWriter(
                        new FileOutputStream(file, true)));
                out.write(str);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (out!=null){
                        out.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

}
