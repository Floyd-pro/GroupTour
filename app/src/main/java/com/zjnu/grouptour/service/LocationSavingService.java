package com.zjnu.grouptour.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.hyphenate.easeim.DemoHelper;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.MyLogUtil;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import androidx.annotation.Nullable;

public class LocationSavingService extends Service {

    private LocationClient mLocClient;
    private MyLocationListener myListener;
    private ExecutorService executorService;
    private long currentTime;

    /** 标识服务如果被杀死之后的行为 */
    int mStartMode = START_STICKY;

    /** 绑定的客户端接口 */
    IBinder mBinder;

    /** 标识是否可以使用onRebind */
    boolean mAllowRebind;

    /** 当服务被创建时调用. */
    @Override
    public void onCreate() {
        MyLogUtil.LogI("LocationSavingService--onCreate()");
        // 定位初始化
        myListener = new MyLocationListener();

        executorService = Executors.newSingleThreadExecutor();

        currentTime = System.currentTimeMillis();
        super.onCreate();
    }

    /** 调用startService()启动服务时回调 */
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        MyLogUtil.LogI("LocationSavingService--onStartCommand()");

        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        // 打开gps
        option.setOpenGps(true);
        // 设置坐标类型
        option.setCoorType("bd09ll");
        option.setScanSpan(2000);
        mLocClient.setLocOption(option);
        mLocClient.start();

        return mStartMode;
    }

    /** 通过bindService()绑定到服务的客户端 */
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        MyLogUtil.LogI("LocationSavingService--onBind()");
        return mBinder;
    }

    /** 通过unbindService()解除所有客户端绑定时调用 */
    @Override
    public boolean onUnbind(Intent intent) {
        return mAllowRebind;
    }

    /** 通过bindService()将客户端绑定到服务时调用*/
    @Override
    public void onRebind(Intent intent) {

    }

    /** 服务不再有用且将要被销毁时调用 */
    @Override
    public void onDestroy() {
        MyLogUtil.LogI("LocationSavingService--onDestroy()");
        super.onDestroy();
        // 退出时销毁定位
        mLocClient.stop();
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // MapView 销毁后不在处理新接收的位置
            if (location == null) {
                return;
            }

            if(System.currentTimeMillis() - currentTime > 3000) {

                currentTime = System.currentTimeMillis();

                if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                    updateLocationInfo(location.getLongitude(), location.getLatitude());
                }
            }
        }
    }

    // 更新当前用户的定位信息至数据库
    private boolean updateLocationInfo(double longitude, double latitude) {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Boolean isUpdated = false;
                if(DemoHelper.getInstance() != null) {
                    isUpdated = DBUtil.updateLocation(DemoHelper.getInstance().getCurrentUser(), longitude, latitude);
                }
                return isUpdated;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新队伍成员信息
                if((Boolean) future.get()) {
                    return true;
                }else {
                    return false;
                }
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
//        finally {
//            executorService.shutdown();
//        }
        return false;
    }
}
