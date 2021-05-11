package com.zjnu.grouptour.api;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.CsvFormatStrategy;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.zjnu.grouptour.BuildConfig;
import com.zjnu.grouptour.utils.ConstantUtils;

/**
 * @author luchen
 * @Date 2021/4/1 10:00
 * @Description 基础api类
 */
public class GroupTourApi {
    private boolean mIsOpenLogger = true;
    //true 灰度 false 生产
    private boolean mIsOpenTestEnvironment = true;

    public GroupTourApi() {

    }

    private volatile static GroupTourApi instance = null;

    public static GroupTourApi getInstance() {
        if (instance == null) {
            synchronized (GroupTourApi.class) {
                if (instance == null) {
                    instance = new GroupTourApi();
                }
            }
        }
        return instance;
    }

    public boolean isOpenLogger() {
        return mIsOpenLogger;
    }

    public boolean isOpenTestEnvironment() {
        return mIsOpenTestEnvironment;
    }

    public void initIp(boolean isOpenTestEnvironment) {
        mIsOpenTestEnvironment = isOpenTestEnvironment;
        ConstantUtils.API_INDEX = isOpenTestEnvironment ? 1 : 2;//开发时期0为测试环境
    }

    public void initLogger(boolean isOpenLogger) {
        if (BuildConfig.DEBUG) {
            mIsOpenLogger = isOpenLogger;
        } else {
            mIsOpenLogger = BuildConfig.DEBUG;
        }


        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .tag("Search")
                .build();
        CsvFormatStrategy diskFormatStrategy = CsvFormatStrategy.newBuilder()
                .tag("custom")
                .build();
        Logger.addLogAdapter(new DiskLogAdapter(diskFormatStrategy));

        Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy) {
            @Override
            public boolean isLoggable(int priority, @Nullable String tag) {
                return isOpenLogger;
            }
        });
    }
}
