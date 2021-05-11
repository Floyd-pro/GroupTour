package com.zjnu.grouptour.utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Build;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import com.zjnu.grouptour.R;
import com.zjnu.grouptour.api.GroupTourApi;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static android.content.Context.ACTIVITY_SERVICE;

/**
 * @author luchen
 * @Date 2021/4/1 10:00
 * @Description 基础功能类
 */
public class CommonUtils {

    public CommonUtils() {

    }

    private volatile static CommonUtils instance = null;

    public static CommonUtils getInstance() {
        if (instance == null) {
            synchronized (CommonUtils.class) {
                if (instance == null) {
                    instance = new CommonUtils();
                }
            }
        }
        return instance;
    }

    // formatType格式为yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    // data Date类型的时间
    public static String dateToString(Date data, String formatType) {
        return new SimpleDateFormat(formatType).format(data);
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的string类型的时间格式
    public static String longToString(long currentTime, String formatType)
            throws ParseException {
        Date date = longToDate(currentTime, formatType); // long类型转成Date类型
        String strTime = dateToString(date, formatType); // date类型转成String
        return strTime;
    }

    // strTime要转换的string类型的时间，formatType要转换的格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日
    // HH时mm分ss秒，
    // strTime的时间格式必须要与formatType的时间格式相同
    public static Date stringToDate(String strTime, String formatType)
            throws ParseException {
        SimpleDateFormat formatter = new SimpleDateFormat(formatType);
        Date date = null;
        date = formatter.parse(strTime);
        return date;
    }

    // currentTime要转换的long类型的时间
    // formatType要转换的时间格式yyyy-MM-dd HH:mm:ss//yyyy年MM月dd日 HH时mm分ss秒
    public static Date longToDate(long currentTime, String formatType)
            throws ParseException {
        Date dateOld = new Date(currentTime); // 根据long类型的毫秒数生命一个date类型的时间
        String sDateTime = dateToString(dateOld, formatType); // 把date类型的时间转换为string
        Date date = stringToDate(sDateTime, formatType); // 把String类型转换为Date类型
        return date;
    }

    // strTime要转换的String类型的时间
    // formatType时间格式
    // strTime的时间格式和formatType的时间格式必须相同
    public static long stringToLong(String strTime, String formatType)
            throws ParseException {
        Date date = stringToDate(strTime, formatType); // String类型转成date类型
        if (date == null) {
            return 0;
        } else {
            long currentTime = dateToLong(date); // date类型转成long类型
            return currentTime;
        }
    }

    // date要转换的date类型的时间
    public static long dateToLong(Date date) {
        return date.getTime();
    }

    /**
     * 判断字符串是否为空
     *
     * @param object
     * @return
     */
    public static boolean notIsEmpty(Object object) {
        boolean flag = false;
        if (object != null && object.toString().trim().length() != 0) {
            flag = true;
        }
        return flag;
    }

    public static PackageInfo getVersionName(Context context) {
        try {
            if (context == null) {
                return null;
            } else {
                PackageManager packageManager = context.getPackageManager();
                PackageInfo packInfo = packageManager.getPackageInfo(context.getPackageName(), 16384);
                return packInfo;
            }
        } catch (PackageManager.NameNotFoundException var3) {
            var3.printStackTrace();
            return null;
        }
    }

    /**
     * 实现搜索结果高亮
     * str2为要高亮的内容（title,subtitle）
     * stringList为高亮的分词，title根据title_highsdk,subtile根据subtitle_higsdk，参考json
     *
     * @param context
     * @param textView
     * @param stringList
     * @param str2
     */
    public void setTextHighLight(Context context, TextView textView, List<String> stringList, String str2) {

        SpannableString sp = new SpannableString(str2);
        if (stringList != null && stringList.size() > 0 && !TextUtils.isEmpty(str2)) {
            // 遍历要显示的文字
            for (int i = 0; i < stringList.size(); i++) {
                // 得到单个文字
                String str1 = stringList.get(i);
                // 判断字符串是否包含高亮显示的文字
                if (!TextUtils.isEmpty(str1) && str2.contains(str1)) {
                    // 查找文字在字符串的下标
                    int index = str2.indexOf(str1);
                    // 循环查找字符串中所有该文字并高亮显示
                    while (index != -1) {
                        ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.search_highlight));
                        sp.setSpan(colorSpan, index, index + str1.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                        // s1从这个索引往后开始第一个出现的位置
                        index = str2.indexOf(str1, index + str1.length());
                    }
                }
            }
            // 设置控件
            textView.setText(sp);
        } else {
            textView.setText(str2);
        }
    }

    //设置高亮
    public void setTextHighLight(Context context, TextView textView, String str1, String str2) {

        if (!TextUtils.isEmpty(str1) && !TextUtils.isEmpty(str2)) {
            SpannableString sp = new SpannableString(str2);
            if (str2.contains(str1)) {
                // 查找文字在字符串的下标
                int index = str2.indexOf(str1);
                // 循环查找字符串中所有该文字并高亮显示
                while (index != -1) {
                    ForegroundColorSpan colorSpan = new ForegroundColorSpan(context.getResources().getColor(R.color.search_highlight));
                    sp.setSpan(colorSpan, index, index + str1.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
                    // s1从这个索引往后开始第一个出现的位置
                    index = str2.indexOf(str1, index + str1.length());
                }
            }
            // 设置控件
            textView.setText(sp);
        } else {
            textView.setText(str2);
        }
    }

    /**
     * list去重
     *
     * @param list
     * @param string
     * @return
     */
    public List<String> removeDuplicateList(List<String> list, String string) {
        if (list != null && list.size() > 0 && !TextUtils.isEmpty(string)) {
            for (int i = 0; i < list.size(); i++) {
                if (string.equals(list.get(i))) {
                    list.remove(i);
                }
            }
        }
        list.add(string);
        return list;
    }

    public void hintKeyBoard(Activity activity) {
        //拿到InputMethodManager
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
        //如果window上view获取焦点 && view不为空
        if (imm.isActive() && activity.getCurrentFocus() != null) {
            //拿到view的token 不为空
            if (activity.getCurrentFocus().getWindowToken() != null) {
                //表示软键盘窗口总是隐藏，除非开始时以SHOW_FORCED显示。
                imm.hideSoftInputFromWindow(activity.getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }
        }
    }

    /**
     * 隐藏键盘
     *
     * @param activity
     */
    public void hideKeyboard(Activity activity) {
        activity.getWindow().setSoftInputMode
                (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        InputMethodManager imm = (InputMethodManager) activity.getSystemService(Activity.INPUT_METHOD_SERVICE);
        //Find the currently focused view, so we can grab the correct window token from it.
        View view = activity.getCurrentFocus();
        //If no view currently has focus, create a new one, just so we can grab a window token from it
        if (view == null) {
            view = new View(activity);
        }
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }

    //    public static void HideKeyboard(EditText v, Activity context) {
    //        context.getWindow().setSoftInputMode
    //                (WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    //
    //        int currentVersion = android.os.Build.VERSION.SDK_INT;
    //        String methodName = null;//4.0以上和4.2以上方法名有所改变
    //        if (currentVersion >= 16) {
    //            // 4.2
    //            methodName = "setShowSoftInputOnFocus";
    //        } else if (currentVersion >= 14) {
    //            // 4.0
    //            methodName = "setSoftInputShownOnFocus";
    //        }
    //
    //        if (methodName == null) {//4.0以下采用此方法
    //            v.setInputType(InputType.TYPE_NULL);
    //        } else {
    //            Class<EditText> cls = EditText.class;
    //            Method setShowSoftInputOnFocus;
    //            try {
    //                setShowSoftInputOnFocus = cls.getMethod(methodName, boolean.class);
    //                setShowSoftInputOnFocus.setAccessible(true);
    //                setShowSoftInputOnFocus.invoke(v, false);
    //            } catch (NoSuchMethodException e) {
    //                v.setInputType(InputType.TYPE_NULL);
    //                e.printStackTrace();
    //            } catch (IllegalAccessException e) {
    //                // TODO Auto-generated catch block
    //                e.printStackTrace();
    //            } catch (IllegalArgumentException e) {
    //                // TODO Auto-generated catch block
    //                e.printStackTrace();
    //            } catch (InvocationTargetException e) {
    //                // TODO Auto-generated catch block
    //                e.printStackTrace();
    //            }
    //        }
    //    }
    //
    //拉起键盘
    public void showKeyBoard(Activity context, View view) {
        context.getWindow().setSoftInputMode(WindowManager.LayoutParams
                .SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        view.setFocusable(true);
        view.setFocusableInTouchMode(true);
        view.requestFocus();
        view.findFocus();
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        //        if (!isSoftShowing(context)) {
        //            imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
        //        }
        imm.showSoftInput(view, InputMethodManager.SHOW_FORCED);
    }

    private boolean isSoftShowing(Activity activity) {
        //获取当前屏幕内容的高度
        int screenHeight = activity.getWindow().getDecorView().getHeight();
        //获取View可见区域的bottom
        Rect rect = new Rect();
        activity.getWindow().getDecorView().getWindowVisibleDisplayFrame(rect);

        return screenHeight - rect.bottom - getSoftButtonsBarHeight(activity) != 0;
    }

    /**
     * 底部虚拟按键栏的高度
     *
     * @return
     */
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    private int getSoftButtonsBarHeight(Activity activity) {
        DisplayMetrics metrics = new DisplayMetrics();
        //这个方法获取可能不是真实屏幕的高度
        activity.getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int usableHeight = metrics.heightPixels;
        //获取当前屏幕的真实高度
        activity.getWindowManager().getDefaultDisplay().getRealMetrics(metrics);
        int realHeight = metrics.heightPixels;
        if (realHeight > usableHeight) {
            return realHeight - usableHeight;
        } else {
            return 0;
        }
    }

    /**
     * 根据手机的分辨率从 dp 的单位 转成为 px(像素)
     */
    public static int dip2px(Context context, float dpValue) {
        float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dpValue * scale + 0.5f);
    }

    public static int px2sp(Context context, float pxValue) {
        final float fontScale = context.getResources().getDisplayMetrics().scaledDensity;
        return (int) (pxValue / fontScale + 0.5f);
    }


    public boolean isExistActivity(Activity context, Class<?> activity) {
        Intent intent = new Intent(context, activity);
        ComponentName cmpName = intent.resolveActivity(context.getPackageManager());
        boolean flag = false;
        if (cmpName != null) { // 说明系统中存在这个activity    
            ActivityManager am = (ActivityManager) context.getSystemService(ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> taskInfoList = am.getRunningTasks(10);//
            //这里获取的是APP栈的数量，一般也就两个
            ActivityManager.RunningTaskInfo runningTaskInfo = taskInfoList.get(0);// 只是拿当前运行的栈
            int numActivities = taskInfoList.get(0).numActivities;

            for (ActivityManager.RunningTaskInfo taskInfo : taskInfoList) {
                if (taskInfo.baseActivity.equals(cmpName)) {// 说明它已经启动了
                    flag = true;
                    break;//跳出循环，优化效率
                }
            }
        }
        return flag;

    }

    public List<Activity> getAllActivitys() {
        List<Activity> list = new ArrayList<>();
        try {
            Class<?> activityThread = Class.forName("android.app.ActivityThread");
            Method currentActivityThread = activityThread.getDeclaredMethod("currentActivityThread");
            currentActivityThread.setAccessible(true);
            //获取主线程对象
            Object activityThreadObject = currentActivityThread.invoke(null);
            Field mActivitiesField = activityThread.getDeclaredField("mActivities");
            mActivitiesField.setAccessible(true);
            Map<Object, Object> mActivities = (Map<Object, Object>) mActivitiesField.get(activityThreadObject);
            for (Map.Entry<Object, Object> entry : mActivities.entrySet()) {
                Object value = entry.getValue();
                Class<?> activityClientRecordClass = value.getClass();
                Field activityField = activityClientRecordClass.getDeclaredField("activity");
                activityField.setAccessible(true);
                Object o = activityField.get(value);
                list.add((Activity) o);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public boolean isHasRecordPermission(final Context context) {
        // 音频获取源
        int audioSource = MediaRecorder.AudioSource.MIC;
        // 设置音频采样率，44100是目前的标准，但是某些设备仍然支持22050，16000，11025
        int sampleRateInHz = 44100;
        // 设置音频的录制的声道CHANNEL_IN_STEREO为双声道，CHANNEL_CONFIGURATION_MONO为单声道
        int channelConfig = AudioFormat.CHANNEL_IN_STEREO;
        // 音频数据格式:PCM 16位每个样本。保证设备支持。PCM 8位每个样本。不一定能得到设备支持。
        int audioFormat = AudioFormat.ENCODING_PCM_16BIT;
        // 缓冲区字节大小
        int bufferSizeInBytes = 0;


        bufferSizeInBytes = 0;
        bufferSizeInBytes = AudioRecord.getMinBufferSize(sampleRateInHz,
                channelConfig, audioFormat);
        AudioRecord audioRecord = new AudioRecord(audioSource, sampleRateInHz,
                channelConfig, audioFormat, bufferSizeInBytes);
        //开始录制音频
        try {
            // 防止某些手机崩溃，例如联想
            audioRecord.startRecording();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        }
        /**
         * 根据开始录音判断是否有录音权限
         */
        if (audioRecord.getRecordingState() != AudioRecord.RECORDSTATE_RECORDING) {
            return false;
        }
        audioRecord.stop();
        audioRecord.release();
        audioRecord = null;

        return true;
    }

    //获取当前日期
    public String getDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    //获取当前时间
    public String getTime() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:MM:SS");
        Date date = new Date(System.currentTimeMillis());
        return simpleDateFormat.format(date);
    }

    /**
     * 获取屏幕宽度
     *
     * @param context
     * @return
     */
    public int getScreenWidth(Context context) {
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int mScreenWidth = dm.widthPixels;
        return mScreenWidth;
    }

    /**
     * 获取屏幕高度
     *
     * @param context
     * @return
     */
    public int getScreenHeight(Context context) {
        WindowManager wm = (WindowManager) (context.getSystemService(Context.WINDOW_SERVICE));
        DisplayMetrics dm = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(dm);
        int mScreenHeight = dm.heightPixels;
        return mScreenHeight;
    }

    public long getTraceId() {
        return System.currentTimeMillis();
    }

    /**
     * 价格格式化,去掉小数点后面的0,比如360.00 = 360, 360.50 = 360.5等
     *
     * @param price
     * @return
     */
    public String formatPrice(String price) {
        if (!TextUtils.isEmpty(price)) {
            if (price.indexOf(".") > 0) {
                price = price.replaceFirst("0+?$", "");
                price = price.replaceAll("[.]$", "");
            }
        }
        return price;
    }

    // 两次点击间隔不能少于1000ms
    private static final int FAST_CLICK_DELAY_TIME = 1000;
    private static long lastClickTime;

    public boolean isFastClick() {
        boolean flag = true;
        long currentClickTime = System.currentTimeMillis();
        if ((currentClickTime - lastClickTime) >= FAST_CLICK_DELAY_TIME) {
            flag = false;
        }
        lastClickTime = currentClickTime;
        return flag;
    }

    public void showToast(Context context, String content) {
        if (GroupTourApi.getInstance().isOpenLogger() && content != null) {
            Toast.makeText(context, content, Toast.LENGTH_SHORT).show();
        }
    }
}
