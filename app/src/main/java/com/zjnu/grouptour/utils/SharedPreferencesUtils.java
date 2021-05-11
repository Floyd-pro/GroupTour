package com.zjnu.grouptour.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.util.ArrayList;
import java.util.List;

public class SharedPreferencesUtils {
    private static final String SHARED_NAME = "grouptour";
    private static SharedPreferences mSharedPreferences;
    private static SharedPreferences.Editor mEditor;
    private static  volatile SharedPreferencesUtils mSharedPreferencesUtils;

    public SharedPreferencesUtils(Context context) {
        mSharedPreferences = context.getSharedPreferences(SHARED_NAME, Context.MODE_PRIVATE);
        mEditor = mSharedPreferences.edit();
    }

    public static SharedPreferencesUtils getInstance(Context context) {
        if (mSharedPreferencesUtils == null) {
            synchronized (SharedPreferencesUtils.class) {
                if (mSharedPreferencesUtils == null) {
                    mSharedPreferencesUtils = new SharedPreferencesUtils(context);
                }
            }
        }
        return mSharedPreferencesUtils;
    }


    /**
     * 保存List
     * @param tag
     * @param datalist
     */
    public <T> void setDataList(String tag, List<T> datalist) {
        if (null == datalist)
            return;

        Gson gson = new Gson();
        //转换成json数据，再保存
        String strJson = gson.toJson(datalist);
        mEditor.putString(tag, strJson);
        mEditor.commit();

    }

    /**
     * 获取List
     * @param tag
     * @return
     */
    public <T> List<T> getDataList(String tag) {
        List<T> datalist=new ArrayList<T>();
        String strJson = mSharedPreferences.getString(tag, null);
        if (null == strJson) {
            return datalist;
        }
        Gson gson = new Gson();
        datalist = gson.fromJson(strJson, new TypeToken<List<T>>() {
        }.getType());
        return datalist;

    }


    public void setParam(String key, Object object){

        String type = object.getClass().getSimpleName();
        if("String".equals(type)){
            mEditor.putString(key, (String)object);
        } else if("Integer".equals(type)){
            mEditor.putInt(key, (Integer)object);
        } else if("Boolean".equals(type)){
            mEditor.putBoolean(key, (Boolean)object);
        } else if("Float".equals(type)){
            mEditor.putFloat(key, (Float)object);
        } else if("Long".equals(type)){
            mEditor.putLong(key, (Long)object);
        }
        mEditor.commit();
    }

    /**
     * 得到保存数据的方法，我们根据默认值得到保存的数据的具体类型，然后调用相对于的方法获取值
     * @param key
     * @param defaultObject
     * @return
     */
    public Object getParam(String key, Object defaultObject) {
        String type = defaultObject.getClass().getSimpleName();
        if ("String".equals(type)) {
            return mSharedPreferences.getString(key, (String) defaultObject);
        } else if ("Integer".equals(type)) {
            return mSharedPreferences.getInt(key, (Integer) defaultObject);
        } else if ("Boolean".equals(type)) {
            return mSharedPreferences.getBoolean(key, (Boolean) defaultObject);
        } else if ("Float".equals(type)) {
            return mSharedPreferences.getFloat(key, (Float) defaultObject);
        } else if ("Long".equals(type)) {
            return mSharedPreferences.getLong(key, (Long) defaultObject);
        }

        return null;
    }

}
