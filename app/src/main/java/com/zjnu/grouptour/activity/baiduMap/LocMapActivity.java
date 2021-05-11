package com.zjnu.grouptour.activity.baiduMap;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
import com.baidu.mapapi.clusterutil.clustering.Cluster;
import com.baidu.mapapi.clusterutil.clustering.ClusterItem;
import com.baidu.mapapi.clusterutil.clustering.ClusterManager;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.baidu.mapapi.search.weather.LanguageType;
import com.baidu.mapapi.search.weather.OnGetWeatherResultListener;
import com.baidu.mapapi.search.weather.WeatherDataType;
import com.baidu.mapapi.search.weather.WeatherResult;
import com.baidu.mapapi.search.weather.WeatherSearch;
import com.baidu.mapapi.search.weather.WeatherSearchOption;
import com.baidu.mapapi.search.weather.WeatherSearchRealTime;
import com.baidu.mapapi.search.weather.WeatherServerType;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWNaviStatusListener;
import com.baidu.mapapi.walknavi.adapter.IWRouteGuidanceListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.adapter.IWTTSPlayer;
import com.baidu.mapapi.walknavi.model.RouteGuideKind;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.baidu.platform.comapi.walknavi.WalkNaviModeSwitchListener;
import com.baidu.platform.comapi.walknavi.widget.ArCameraView;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.activity.BaseActivity;
import com.zjnu.grouptour.api.GroupTourApi;
import com.zjnu.grouptour.bean.GroupmateInfo;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.bean.Team;
import com.zjnu.grouptour.dialog.UserInfoBottomSheetDialog;
import com.zjnu.grouptour.searchroute.NodeUtils;
import com.zjnu.grouptour.utils.CalendarReminderUtils;
import com.zjnu.grouptour.utils.CommonUtils;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author luchen
 * @Date 2021/4/10 18:04
 * @Description 地图主界面
 */
public class LocMapActivity extends BaseActivity implements SensorEventListener, BaiduMap.OnMapClickListener, BaiduMap.OnMapLoadedCallback {

    private final static String TAG = LocMapActivity.class.getSimpleName();
    private final static int MAP_STATUS_CHANGE = 100;
    
    // 定位相关
    private LocationClient mLocClient;
    private MyLocationListener myListener = new MyLocationListener();
    // 定位图层显示方式
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    // 初始化地图
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    // 是否首次定位
    private boolean isFirstLoc = true;
    // 是否开启定位图层
    private boolean isLocationLayerEnable = true;
    private MyLocationData myLocationData;

    // 切换定位模式，1对应普通模式，2对应跟随模式，3对应罗盘模式
    private static int locMode = 1;

    // 多人定位及点聚合模块
    private MapStatus mMapStatus;
    private ClusterManager<MyItem> mClusterManager;

    private Person me;
    private Team myTeam;
    private ArrayList<GroupmateInfo> groupmates;
    private ExecutorService executorService;
    // 刷新队员位置 子线程
    private ExecutorService refreshExecutorService;
    private static Handler mHandler;
    private boolean isQuit;
    private long currentTime;

    private InfoWindow mInfoWindow;

    // 路线规划 搜索模块
    private RoutePlanSearch mRouteSearch = null;
    private WalkingRouteResult mWalkingRouteResult = null;
    private NodeUtils mNodeUtils;
    // 浏览路线节点相关
    private RouteLine mRouteLine = null;
    private OverlayManager mRouteOverlay = null;
    private boolean mUseDefaultIcon = false;

    //导航
    private BikeNaviLaunchParam bikeParam;
    private WalkNaviLaunchParam walkParam;
    /*导航起终点Marker，可拖动改变起终点的坐标*/
//    private Marker mStartMarker;
//    private Marker mEndMarker;
    private BitmapDescriptor bdStart = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_start);
    private BitmapDescriptor bdEnd = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_end);

    public static boolean isNaviBack = false; // 是否从导航界面返回
    private static boolean isOnResume = false;
    private static LatLng startLoc, endLoc; // 保存点击PoiDetailView中某一项的经纬度信息 或是 刚打开界面时用户定位的经纬度信息

    private WalkNavigateHelper mNaviHelper;

    // 天气
    private static LatLng destinationLoc;
    private WeatherSearch mWeatherSearch;
    private GeoCoder mGeoCoder;
    private String mDistrict;

    private DrawerLayout drawerLayout;

    private static UserInfoBottomSheetDialog userInfoBottomSheetDialog;

    private Marker destinationMarker;
    private TextView tv_goToDestination;
    private BitmapDescriptor mBitmap = BitmapDescriptorFactory.fromResource(R.drawable.water_drop);

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, LocMapActivity.class);
        context.startActivity(intent);
    }

    public static void actionStart(Context context, Person person, Team team, ArrayList<GroupmateInfo> groupmates) {
        Intent intent = new Intent(context, LocMapActivity.class);
        intent.putExtra("person", person);
        intent.putExtra("team", team);
        intent.putExtra("groupmates", groupmates);
        context.startActivity(intent);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(LocMapActivity.this, true);
        setContentView(R.layout.loc_map_with_weather_drawer);

        drawerLayout = findViewById(R.id.locMap_drawer_layout);

//        StatusBarUtils.setStatusBarMode(LocMapActivity.this, true, R.color.teal_200);

        mHandler = new MyHandler(this);

        me = new Person();
        me.setName(DemoHelper.getInstance().getCurrentUser());

        if(getIntent().hasExtra("person") && getIntent().hasExtra("team") && getIntent().hasExtra("groupmates")) {
            me = getIntent().getExtras().getParcelable("person");
            myTeam = getIntent().getExtras().getParcelable("team");
            groupmates = getIntent().getExtras().getParcelableArrayList("groupmates");
        } else {
            getInfo();
        }

        // 目的地 默认天安门
        destinationLoc = new LatLng(39.915071, 116.403907);
        // 设置 目的地经纬度
        if(myTeam.getDestinationLongitude() != 0 && myTeam.getDestinationLatitude() != 0) {
            destinationLoc = new LatLng(myTeam.getDestinationLatitude(), myTeam.getDestinationLongitude());
        }

        // 地图初始化
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // 获取传感器管理服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        // 定位初始化
        initLocation();

        // 多人定位及点聚合模块
        mBaiduMap.setOnMapLoadedCallback(this);
        // 定义点聚合管理类ClusterManager
        mClusterManager = new ClusterManager<MyItem>(this, mBaiduMap);

        mClusterManager.setHandler(mHandler, MAP_STATUS_CHANGE); //设置handler

        // 添加Marker点
        addMarkers();
        // 设置地图监听，当地图状态发生改变时，进行点聚合运算
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);
        // 设置maker点击时的响应
        mBaiduMap.setOnMarkerClickListener(mClusterManager);


        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                if(GroupTourApi.getInstance().isOpenLogger()) {
                    ToastUtils.showToast("该区域有" + cluster.getSize() + "名队员");
                }


                List<MyItem> items = (List<MyItem>) cluster.getItems();
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                int i=0;
                for(MyItem myItem : items){
                    builder = builder.include(myItem.getPosition());
//                    Log.i(TAG,"log: i="+ i++ +" pos="+myItem.getPosition().toString());
                }

                LatLngBounds latlngBounds = builder.build();
                MapStatusUpdate u = MapStatusUpdateFactory.newLatLngBounds(latlngBounds,mMapView.getWidth()-200, mMapView.getHeight());
                mBaiduMap.animateMapStatus(u);
                return false;
            }
        });
        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(MyItem item) {

                endLoc = item.getPosition();

                // TODO: 2021/4/15 弹出BottomSheetDialog
                ArrayList<Person> personList = new ArrayList<Person>();
                Person p = new Person();
                p.setName(item.getName());
                p.setNickname(item.getNickname());
                p.setTel(item.getTel());
                p.setGender(item.getGender());
                personList.add(p);
                userInfoBottomSheetDialog = new UserInfoBottomSheetDialog(LocMapActivity.this, personList, startLoc, endLoc, mNaviHelper);
                userInfoBottomSheetDialog.setOwnerActivity(LocMapActivity.this);
                userInfoBottomSheetDialog.show();

//                if(GroupTourApi.getInstance().isOpenLogger()) {
//                    ToastUtils.showToast("用户名：" + item.getName() + "\n昵称：" + item.getNickname());
//                }
//
//                Button button = new Button(getApplicationContext());
//                button.setBackgroundResource(R.drawable.btn_nor_down);
//                InfoWindow.OnInfoWindowClickListener listener = null;
//
//                button.setText("导航到Ta");
//                button.setTextColor(Color.BLACK);
//                button.setWidth(300);
//
//                endLoc = item.getPosition();
//                // InfoWindow点击事件监听接口
//                listener = new InfoWindow.OnInfoWindowClickListener() {
//                    public void onInfoWindowClick() {
////                        LatLng latLngNew = new LatLng(item.getPosition().latitude + 0.005, item.getPosition().longitude + 0.005);
////                        item.setPosition(latLngNew);
//                        // 隐藏地图上的所有InfoWindow
//                        mBaiduMap.hideInfoWindow();
//                        // TODO: 2021/4/20 导航到Ta 即导航到指定队员位置功能
//                        /*构造导航起终点参数对象*/
//                        WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
//                        walkStartNode.setLocation(startLoc);
//                        WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
//                        walkEndNode.setLocation(endLoc);
//                        walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
//                        walkParam.extraNaviMode(0);
//                        /* 初始化起终点Marker */
////                initOverlay();
//                        startWalkNavi();
//                    }
//                };
//                // 创建InfoWindow
//                mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), item.getPosition(), -120, listener);
                // 显示 InfoWindow, 该接口会先隐藏其他已添加的InfoWindow, 再添加新的InfoWindow
                mBaiduMap.showInfoWindow(mInfoWindow);
                return false;
            }
        });

        mNaviHelper = WalkNavigateHelper.getInstance();

        // 天气查询模块初始化
        mWeatherSearch = WeatherSearch.newInstance();
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener(){
            /**
             * 地理编码查询结果回调函数
             *
             * @param result 地理编码查询结果
             */
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
            }
            /**
             * 反地理编码查询结果回调函数
             *
             * @param result 反地理编码查询结果
             */
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                int adCode = result.getAdcode();
                mDistrict = String.valueOf(adCode);
            }
        });

        // 行政区划编码 更新
        ReverseGeoCodeOption rgcOption =
                new ReverseGeoCodeOption().location(destinationLoc).radius(500);
        mGeoCoder.reverseGeoCode(rgcOption);

        tv_goToDestination = findViewById(R.id.tv_goToDestination);
        tv_goToDestination.setText("目的地：" + myTeam.getDestinationName());
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnResume = true;
        if(userInfoBottomSheetDialog != null && userInfoBottomSheetDialog.isShowing())
            userInfoBottomSheetDialog.hide();

        mNaviHelper.resume();

        executorService = Executors.newSingleThreadExecutor();
        isQuit = false;
        refreshGroupmatesLoc();

        // 在activity执行onResume时必须调用mMapView. onResume ()
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviHelper.pause();

        executorService.shutdown();
        isQuit = true;
        refreshExecutorService.shutdownNow();
        // 在activity执行onPause时必须调用mMapView. onPause ()
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mNaviHelper.quit();
        bdStart.recycle();
        bdEnd.recycle();
        mHandler = null;
        destinationMarker = null;
        // 释放bitmap
        mBitmap.recycle();
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        // 退出时销毁定位
        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // 清除所有图层
        mBaiduMap.clear();
        // 在activity执行onDestroy时必须调用mMapView.onDestroy()
        mMapView.onDestroy();
    }

    private static class MyHandler extends Handler {
        WeakReference<LocMapActivity> weakReference;

        public MyHandler() {
        }

        public MyHandler(LocMapActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case MAP_STATUS_CHANGE:
                    MapStatus mapStatus = (MapStatus) msg.obj;
                    if(mapStatus != null){
//                        Log.i("MarkerClusterDemo", "mapStatus="+mapStatus.toString());
                        // TODO: 2021/4/20 判断地图状态，进行相应处理
                        if(GroupTourApi.getInstance().isOpenLogger() && weakReference.get() != null) {
//                            ToastUtils.showToast("有" +
//                                    (weakReference.get().mClusterManager.getMarkerCollection().getMarkers().size() +
//                                    weakReference.get().mClusterManager.getClusterMarkerCollection().getMarkers().size()) +
//                                    "个点");
                        }
                    }
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        double x = sensorEvent.values[SensorManager.DATA_X];
        if (Math.abs(x - lastX) > 1.0) {
            mCurrentDirection = (int) x;
            myLocationData = new MyLocationData.Builder()
                    .accuracy(mCurrentAccracy)// 设置定位数据的精度信息，单位：米
                    .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(mCurrentLat)
                    .longitude(mCurrentLon)
                    .build();
            mBaiduMap.setMyLocationData(myLocationData);
            startLoc = new LatLng(mCurrentLat, mCurrentLon);
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 定位初始化
     */
    public  void initLocation(){
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 定位初始化
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
    }

    /**
     * 设置定位图层的开启和关闭
     */
    public void setLocEnable(View v){
        if(isLocationLayerEnable){
            mBaiduMap.setMyLocationEnabled(false);
            ((Button) v).setText("开启定位图层");
            isLocationLayerEnable = !isLocationLayerEnable;
        }else{
            mBaiduMap.setMyLocationEnabled(true);
            mBaiduMap.setMyLocationData(myLocationData);
            ((Button) v).setText("关闭定位图层");
            isLocationLayerEnable = !isLocationLayerEnable;
        }
    }

    /**
     * 在普通、跟随、罗盘三种模式中切换
     */
    public void changeLocationMode(View v) {
        if(locMode == 1) {
            setFollowType(null);
            locMode = 2;
        }else if(locMode == 2) {
            setCompassType(null);
            locMode = 3;
        }else if(locMode == 3) {
            setNormalType(null);
            locMode = 1;
        }
    }

    /**
     * 设置普通模式
     */
    public void setNormalType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 传入null，则为默认图标
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder1 = new MapStatus.Builder();
        builder1.overlook(0);
        builder1.zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
    }

    /**
     * 设置跟随模式
     */
    public void setFollowType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.FOLLOWING;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.overlook(0);
        builder.zoom(19.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
    }

    /**
     * 设置罗盘模式
     */
    public void setCompassType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
    }

    /**
     * 定位SDK监听函数
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // MapView 销毁后不在处理新接收的位置
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            myLocationData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())// 设置定位数据的精度信息，单位：米
                    .direction(mCurrentDirection)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(location.getLatitude())
                    .longitude(location.getLongitude())
                    .build();
            mBaiduMap.setMyLocationData(myLocationData);
            startLoc = new LatLng(mCurrentLat, mCurrentLon);
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                if(isOnResume && isFirstLoc) { // 界面出现时，根据用户定位以及目的地位置刷新地图
                    mBaiduMap.clear();
                    if (destinationLoc.latitude != 39.915071 && destinationLoc.longitude != 116.403907) { // 若已获取到队伍目的地经纬度
                        // 目的地的位置
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.target(destinationLoc);
                        builder.zoom(15.0f); // 调节地图比例尺
                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    }else {
                        // 用户打开界面时的位置
                        ToastUtils.showToast("队伍目的地位置信息获取失败！");
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.target(latLng);
                        builder.zoom(15.0f); // 调节地图比例尺
                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        destinationLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    }

                    // 自动搜索当前搜索框内显示的目的地
//                    destinationResearch(null);

                    isOnResume = false;
                }
            }
        }
    }

    // 显示当地天气
    public void showWeather(View v) {
        String districtId = mDistrict;
        WeatherSearchOption weatherSearchOption = new WeatherSearchOption();
        weatherSearchOption
                .weatherDataType(WeatherDataType.WEATHER_DATA_TYPE_ALL)
                .districtID(districtId)
                .languageType(LanguageType.LanguageTypeChinese)
                .serverType(WeatherServerType.WEATHER_SERVER_TYPE_DEFAULT);
        mWeatherSearch.setWeatherSearchResultListener(new OnGetWeatherResultListener() {
            @Override
            public void onGetWeatherResultListener(final WeatherResult weatherResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
//                        List<WeatherSearchForecasts> list = weatherResult.getForecasts();
//                        for(WeatherSearchForecasts forecast : list) {
//                            ToastUtils.showToast(forecast.getPhenomenonDay());
//                        }

                        if (null == weatherResult) {
                            ToastUtils.showToast("没有搜索到目的地天气！");
                            return;
                        }
                        WeatherSearchRealTime weatherSearchRealTime = weatherResult.getRealTimeWeather();
                        if (null == weatherSearchRealTime) {
                            return;
                        }
                        TextView tv_title = findViewById(R.id.tv_title);
                        String weatherTitle = "实时天气：" + myTeam.getDestinationName();
                        tv_title.setText(weatherTitle);
                        TextView txtRelativeHumdidity = findViewById(R.id.txtRelativeHumidity);
                        String relativeHumidity =
                                "相对湿度：" + weatherSearchRealTime.getRelativeHumidity() + "%";
                        txtRelativeHumdidity.setText(relativeHumidity);
                        TextView txtSensoryTemp = findViewById(R.id.txtSensoryTemp);
                        String sensoryTemp = "体感温度：" + String.valueOf(weatherSearchRealTime.getSensoryTemp()) + "℃";
                        txtSensoryTemp.setText(sensoryTemp);
                        TextView txtPhenomenon = findViewById(R.id.txtPhenomenon);
                        String phenomenon = "天气现象："+weatherSearchRealTime.getPhenomenon();
                        txtPhenomenon.setText(phenomenon);
                        TextView txtWindDirection = findViewById(R.id.txtWindDirection);
                        String windDirection = "风向：" + weatherSearchRealTime.getWindDirection();
                        txtWindDirection.setText(windDirection);
                        TextView txtWindPower = findViewById(R.id.txtWindPower);
                        String windPower = "风力：" + weatherSearchRealTime.getWindPower();
                        txtWindPower.setText(windPower);
                        TextView txtTemp = findViewById(R.id.txtTemp);
                        String temp = "温度：" + weatherSearchRealTime.getTemperature() + "℃";
                        txtTemp.setText(temp);
                        TextView txtUpdateTime = findViewById(R.id.txtUpdateTime);
                        String updateTime = weatherSearchRealTime.getUpdateTime();
                        updateTime = updateTime.substring(0, 4) + "年" + updateTime.substring(4, 6) + "月" + updateTime.substring(6, 8) + "日 " + updateTime.substring(8, 10) + ":" + updateTime.substring(10, 12);
                        txtUpdateTime.setText(updateTime);




                        Button btn_setAlarm = findViewById(R.id.btn_setAlarm);
                        btn_setAlarm.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onClick(View view) {
                                try {
                                    CalendarReminderUtils.addCalendarEvent(LocMapActivity.this,"天目山旅游","明天就要出发去" + myTeam.getDestinationName() + "了哦，赶紧准备一下吧~\n   ——来自提姆游App",
                                            CommonUtils.stringToLong(CommonUtils.dateToString(myTeam.getDepartureDateTime(), "yyyy-MM-dd"),"yyyy-MM-dd") - 1000*3600*6,0);
                                    CalendarReminderUtils.addCalendarEvent(LocMapActivity.this,"天目山旅游","马上就要出发去" + myTeam.getDestinationName() + "了哦，最后检查一下有没有什么东西落下了吧~\n   ——来自提姆游App",
                                            myTeam.getDepartureDateTime().getTime() - 1000*3600*2,0);
                                    ToastUtils.showToast("已为您设置出发前一晚18点整和出发前2小时的日历提醒！");
                                } catch (ParseException e) {
                                    ToastUtils.showToast("设置日历闹钟错误！");
                                }
                            }
                        });

                        if(!drawerLayout.isOpen()) {
                            drawerLayout.openDrawer(GravityCompat.END);
                            StatusBarUtils.setStatusBarMode(LocMapActivity.this, true, R.color.teal_200);
                        }
                        else { // 不会执行else里面的操作
                            drawerLayout.closeDrawer(GravityCompat.END);
                            StatusBarUtils.setTranslucentStatusTextMode(LocMapActivity.this, true);
                        }
                    }
                });
            }
        });
        mWeatherSearch.request(weatherSearchOption);
    }


    // 获取用户、队伍及队伍成员的信息
    private void getInfo() {
        executorService = Executors.newSingleThreadExecutor();
        if(getMyInfo()) {
            if(getTeamInfo()) {
                if(getGroupmatesInfo()) {
                    if(GroupTourApi.getInstance().isOpenLogger())
                        ToastUtils.showToast("个人、队伍及队员信息获取成功！");
                } else {
                    if(GroupTourApi.getInstance().isOpenLogger())
                        ToastUtils.showToast("队伍成员信息获取失败！");
                }
            } else {
                if(GroupTourApi.getInstance().isOpenLogger())
                    ToastUtils.showToast("队伍信息获取失败！");
            }
        } else {
            if(GroupTourApi.getInstance().isOpenLogger())
                ToastUtils.showToast("个人信息获取失败！");
        }
        executorService.shutdown();
    }

    // 获取用户信息
    private boolean getMyInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Person p = DBUtil.selectPerson(me.getName());
                return p;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新用户信息
                me = (Person) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
//        finally {
//            executorService.shutdown();
//        }
        return false;
    }

    // 获取当前用户所在队伍的信息
    private boolean getTeamInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Team t = DBUtil.selectTeam(me.getTeamID());
                return t;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新队伍信息
                myTeam = (Team) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取当前用户所在队伍的成员信息
    private boolean getGroupmatesInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                ArrayList<GroupmateInfo> groupmates = DBUtil.selectGroupmates(me.getTeamID());
                return groupmates;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新队伍成员信息
                this.groupmates = (ArrayList<GroupmateInfo>) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
//        finally {
//            executorService.shutdown();
//        }
        return false;
    }

    // 每3秒刷新队员所在位置
    private void refreshGroupmatesLoc() {
        refreshExecutorService = Executors.newSingleThreadExecutor();
        refreshExecutorService.execute(new Runnable() {
            @Override
            public void run() {
                while(true) {
                    if(isQuit) {
                        break;
                    }
                    if(System.currentTimeMillis() - currentTime > 3000) {

                        currentTime = System.currentTimeMillis();

                        groupmates = DBUtil.selectGroupmates(me.getTeamID());

                        // mBaiduMap.clear(); // 没用
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mClusterManager.clearItems();
                                addMarkers(); //更新UI
                                mClusterManager.cluster();
                            }
                        });
                    }
                }
            }
        });
    }

    // TODO: 2021/4/19 多人定位及点聚合模块
    /**
     * 向地图添加Marker点
     */
    public void addMarkers() {
        // 添加Marker点
        List<MyItem> items = new ArrayList<MyItem>();
        for(GroupmateInfo g : groupmates) {
            // 添加Marker点
            if (g.getName().equals(me.getName())) continue;
//            items.add(new MyItem(new LatLng(g.getLatitude(), g.getLongitude())));
            items.add(new MyItem(g));
        }
        mClusterManager.addItems(items);
    }

    /**
     * 每个Marker点，包含Marker点坐标以及图标
     */
    public class MyItem implements ClusterItem {
        private LatLng mPosition;
        private GroupmateInfo g;

        private MyItem(GroupmateInfo g) {
            this.g = g;
            mPosition = new LatLng(g.getLatitude(), g.getLongitude());
        }

        private MyItem(LatLng latLng) {
            mPosition = latLng;
        }

        @Override
        public LatLng getPosition() {
            if(mPosition != null) {
                return mPosition;
            }
            return null;
        }

        public void setPosition(LatLng ll) {
            this.mPosition = ll;
            this.g.setLongitude(ll.longitude);
            this.g.setLatitude(ll.latitude);
        }

        public GroupmateInfo getInfo() {
            return g;
        }

        public String getName() {
            return g.getName();
        }

        public String getNickname() {
            return g.getNickname();
        }

        public String getTel() {
            return g.getTel();
        }

        public String getGender() {
            return g.getGender();
        }

        @Override
        public BitmapDescriptor getBitmapDescriptor() {
            Bitmap bitmapMarker = zoomImg(BitmapFactory.decodeResource(getResources(), R.drawable.marker_location), 150, 150);
            return BitmapDescriptorFactory.fromBitmap(bitmapMarker);
//            return BitmapDescriptorFactory.fromResource(R.drawable.marker_location);
//            return BitmapDescriptorFactory.fromView(findViewById(R.id.img_marker_location_big));
        }
    }

    @Override
    public void onMapLoaded() {
//        // TODO Auto-generated method stub
//        mMapStatus = new MapStatus.Builder().zoom(9).build();
//        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(mMapStatus));
    }

    @Override
    public void onMapClick(LatLng latLng) {
        mBaiduMap.hideInfoWindow();
    }

    @Override
    public void onMapPoiClick(MapPoi mapPoi) {
        
    }

    public void goToDestination(View v) {
        if (destinationLoc.latitude != 39.915071 && destinationLoc.longitude != 116.403907) { // 若已获取到队伍目的地经纬度
            // 目的地的位置
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(destinationLoc);
            builder.zoom(14.0f); // 调节地图比例尺
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(destinationLoc)// 经纬度
                .icon(mBitmap) // 设置 Marker 覆盖物的图标
                .clickable(true); // 设置Marker是否可点击
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.grow);
        destinationMarker = (Marker) (mBaiduMap.addOverlay(markerOptions));

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getPosition() == destinationLoc) {
                    Button button = new Button(getApplicationContext());
                    button.setBackgroundResource(R.drawable.popup);
                    InfoWindow.OnInfoWindowClickListener listener = null;
                    button.setText("导航至目的地");
                    button.setTextColor(Color.BLACK);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            /*构造导航起终点参数对象*/
                            WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                            walkStartNode.setLocation(startLoc);
                            WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                            walkEndNode.setLocation(destinationLoc);
                            walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
                            walkParam.extraNaviMode(0);
                            /* 初始化起终点Marker */
//                            initOverlay();
                            startWalkNavi();

                            mBaiduMap.clear();
                            mBaiduMap.hideInfoWindow();
                        }
                    });
                    LatLng latLng = marker.getPosition();
                    mInfoWindow = new InfoWindow(button, latLng, -250);
                    mBaiduMap.showInfoWindow(mInfoWindow);
                }

                return false;
            }
        });
    }

    /**
     * 开始步行导航
     */
    private void startWalkNavi() {
        Log.d(TAG, "startWalkNavi");
        try {
            WalkNavigateHelper.getInstance().initNaviEngine(this, new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d(TAG, "WalkNavi engineInitSuccess");
                    routePlanWithWalkParam();
                }

                @Override
                public void engineInitFail() {
                    Log.d(TAG, "WalkNavi engineInitFail");
                    WalkNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "startBikeNavi Exception");
            e.printStackTrace();
        }
    }

    /**
     * 发起步行导航算路
     */
    private void routePlanWithWalkParam() {
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d(TAG, "WalkNavi onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {

                Log.d(TAG, "onRoutePlanSuccess");

//                Intent intent = new Intent();
//                intent.setClass(LocMapActivity.this, WalkNaviGuideActivity.class);
//                startActivity(intent);

                try {
                    View view = mNaviHelper.onCreate(LocMapActivity.this);
                    if (view != null) {
                        setContentView(view);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mNaviHelper.setWalkNaviStatusListener(new IWNaviStatusListener() {
                    @Override
                    public void onWalkNaviModeChange(int mode, WalkNaviModeSwitchListener listener) {
                        Log.d(TAG, "onWalkNaviModeChange : " + mode);
                        mNaviHelper.switchWalkNaviMode(LocMapActivity.this, mode, listener);
                    }

                    @Override
                    public void onNaviExit() {
                        Log.d(TAG, "onNaviExit");
                    }
                });

                mNaviHelper.setTTsPlayer(new IWTTSPlayer() {
                    @Override
                    public int playTTSText(final String s, boolean b) {
                        Log.d(TAG, "tts: " + s);
                        return 0;
                    }
                });

                boolean startResult = mNaviHelper.startWalkNavi(LocMapActivity.this);
                Log.e(TAG, "startWalkNavi result : " + startResult);

                mNaviHelper.setRouteGuidanceListener(LocMapActivity.this, new IWRouteGuidanceListener() {
                    @Override
                    public void onRouteGuideIconUpdate(Drawable icon) {

                    }

                    @Override
                    public void onRouteGuideKind(RouteGuideKind routeGuideKind) {
                        Log.d(TAG, "onRouteGuideKind: " + routeGuideKind);
                    }

                    @Override
                    public void onRoadGuideTextUpdate(CharSequence charSequence, CharSequence charSequence1) {
                        Log.d(TAG, "onRoadGuideTextUpdate   charSequence=: " + charSequence + "   charSequence1 = : " +
                                charSequence1);

                    }

                    @Override
                    public void onRemainDistanceUpdate(CharSequence charSequence) {
                        Log.d(TAG, "onRemainDistanceUpdate: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onRemainTimeUpdate(CharSequence charSequence) {
                        Log.d(TAG, "onRemainTimeUpdate: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onGpsStatusChange(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onGpsStatusChange: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onRouteFarAway(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onRouteFarAway: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onRoutePlanYawing(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onRoutePlanYawing: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onReRouteComplete() {

                    }

                    @Override
                    public void onArriveDest() {

                    }

                    @Override
                    public void onIndoorEnd(Message msg) {

                    }

                    @Override
                    public void onFinalEnd(Message msg) {

                    }

                    @Override
                    public void onVibrate() {

                    }
                });

            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                Log.d(TAG, "WalkNavi onRoutePlanFail");
                if(GroupTourApi.getInstance().isOpenLogger()) {
                    if("DISTANCE_MORE_THAN_50KM".equals(error.name()))
                        ToastUtils.showToast("离目的地超过50千米，发起步行导航失败！");
                    finish();
                }
            }

        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == ArCameraView.WALK_AR_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
                Toast.makeText(LocMapActivity.this, "没有相机权限,请打开后重试", Toast.LENGTH_SHORT).show();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaviHelper.startCameraAndSetMapView(LocMapActivity.this);
            }
        }
    }

    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        //获得图片的宽高
        int width = bm.getWidth();
        int height = bm.getHeight();
        //计算缩放比例
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        //取得想要缩放的matrix参数
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        //得到新的图片
        return Bitmap.createBitmap(bm, 0, 0, width, height, matrix, true);
    }

    public static boolean closeUserInfoDialog() {
        if(userInfoBottomSheetDialog != null) {
            userInfoBottomSheetDialog.hide();
            return true;
        }
        return false;
    }
}