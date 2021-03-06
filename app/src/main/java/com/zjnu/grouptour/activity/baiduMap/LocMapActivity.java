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
 * @Description ???????????????
 */
public class LocMapActivity extends BaseActivity implements SensorEventListener, BaiduMap.OnMapClickListener, BaiduMap.OnMapLoadedCallback {

    private final static String TAG = LocMapActivity.class.getSimpleName();
    private final static int MAP_STATUS_CHANGE = 100;
    
    // ????????????
    private LocationClient mLocClient;
    private MyLocationListener myListener = new MyLocationListener();
    // ????????????????????????
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    // ???????????????
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    // ??????????????????
    private boolean isFirstLoc = true;
    // ????????????????????????
    private boolean isLocationLayerEnable = true;
    private MyLocationData myLocationData;

    // ?????????????????????1?????????????????????2?????????????????????3??????????????????
    private static int locMode = 1;

    // ??????????????????????????????
    private MapStatus mMapStatus;
    private ClusterManager<MyItem> mClusterManager;

    private Person me;
    private Team myTeam;
    private ArrayList<GroupmateInfo> groupmates;
    private ExecutorService executorService;
    // ?????????????????? ?????????
    private ExecutorService refreshExecutorService;
    private static Handler mHandler;
    private boolean isQuit;
    private long currentTime;

    private InfoWindow mInfoWindow;

    // ???????????? ????????????
    private RoutePlanSearch mRouteSearch = null;
    private WalkingRouteResult mWalkingRouteResult = null;
    private NodeUtils mNodeUtils;
    // ????????????????????????
    private RouteLine mRouteLine = null;
    private OverlayManager mRouteOverlay = null;
    private boolean mUseDefaultIcon = false;

    //??????
    private BikeNaviLaunchParam bikeParam;
    private WalkNaviLaunchParam walkParam;
    /*???????????????Marker????????????????????????????????????*/
//    private Marker mStartMarker;
//    private Marker mEndMarker;
    private BitmapDescriptor bdStart = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_start);
    private BitmapDescriptor bdEnd = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_end);

    public static boolean isNaviBack = false; // ???????????????????????????
    private static boolean isOnResume = false;
    private static LatLng startLoc, endLoc; // ????????????PoiDetailView?????????????????????????????? ?????? ????????????????????????????????????????????????

    private WalkNavigateHelper mNaviHelper;

    // ??????
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

        // ????????? ???????????????
        destinationLoc = new LatLng(39.915071, 116.403907);
        // ?????? ??????????????????
        if(myTeam.getDestinationLongitude() != 0 && myTeam.getDestinationLatitude() != 0) {
            destinationLoc = new LatLng(myTeam.getDestinationLatitude(), myTeam.getDestinationLongitude());
        }

        // ???????????????
        mMapView = (MapView) findViewById(R.id.bmapView);
        mBaiduMap = mMapView.getMap();
        // ???????????????????????????
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // ??????????????????????????????????????????
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        // ???????????????
        initLocation();

        // ??????????????????????????????
        mBaiduMap.setOnMapLoadedCallback(this);
        // ????????????????????????ClusterManager
        mClusterManager = new ClusterManager<MyItem>(this, mBaiduMap);

        mClusterManager.setHandler(mHandler, MAP_STATUS_CHANGE); //??????handler

        // ??????Marker???
        addMarkers();
        // ???????????????????????????????????????????????????????????????????????????
        mBaiduMap.setOnMapStatusChangeListener(mClusterManager);
        // ??????maker??????????????????
        mBaiduMap.setOnMarkerClickListener(mClusterManager);


        mClusterManager.setOnClusterClickListener(new ClusterManager.OnClusterClickListener<MyItem>() {
            @Override
            public boolean onClusterClick(Cluster<MyItem> cluster) {
                if(GroupTourApi.getInstance().isOpenLogger()) {
                    ToastUtils.showToast("????????????" + cluster.getSize() + "?????????");
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

                // TODO: 2021/4/15 ??????BottomSheetDialog
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
//                    ToastUtils.showToast("????????????" + item.getName() + "\n?????????" + item.getNickname());
//                }
//
//                Button button = new Button(getApplicationContext());
//                button.setBackgroundResource(R.drawable.btn_nor_down);
//                InfoWindow.OnInfoWindowClickListener listener = null;
//
//                button.setText("?????????Ta");
//                button.setTextColor(Color.BLACK);
//                button.setWidth(300);
//
//                endLoc = item.getPosition();
//                // InfoWindow????????????????????????
//                listener = new InfoWindow.OnInfoWindowClickListener() {
//                    public void onInfoWindowClick() {
////                        LatLng latLngNew = new LatLng(item.getPosition().latitude + 0.005, item.getPosition().longitude + 0.005);
////                        item.setPosition(latLngNew);
//                        // ????????????????????????InfoWindow
//                        mBaiduMap.hideInfoWindow();
//                        // TODO: 2021/4/20 ?????????Ta ????????????????????????????????????
//                        /*?????????????????????????????????*/
//                        WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
//                        walkStartNode.setLocation(startLoc);
//                        WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
//                        walkEndNode.setLocation(endLoc);
//                        walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
//                        walkParam.extraNaviMode(0);
//                        /* ??????????????????Marker */
////                initOverlay();
//                        startWalkNavi();
//                    }
//                };
//                // ??????InfoWindow
//                mInfoWindow = new InfoWindow(BitmapDescriptorFactory.fromView(button), item.getPosition(), -120, listener);
                // ?????? InfoWindow, ???????????????????????????????????????InfoWindow, ???????????????InfoWindow
                mBaiduMap.showInfoWindow(mInfoWindow);
                return false;
            }
        });

        mNaviHelper = WalkNavigateHelper.getInstance();

        // ???????????????????????????
        mWeatherSearch = WeatherSearch.newInstance();
        mGeoCoder = GeoCoder.newInstance();
        mGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener(){
            /**
             * ????????????????????????????????????
             *
             * @param result ????????????????????????
             */
            @Override
            public void onGetGeoCodeResult(GeoCodeResult result) {
            }
            /**
             * ???????????????????????????????????????
             *
             * @param result ???????????????????????????
             */
            @Override
            public void onGetReverseGeoCodeResult(ReverseGeoCodeResult result) {
                int adCode = result.getAdcode();
                mDistrict = String.valueOf(adCode);
            }
        });

        // ?????????????????? ??????
        ReverseGeoCodeOption rgcOption =
                new ReverseGeoCodeOption().location(destinationLoc).radius(500);
        mGeoCoder.reverseGeoCode(rgcOption);

        tv_goToDestination = findViewById(R.id.tv_goToDestination);
        tv_goToDestination.setText("????????????" + myTeam.getDestinationName());
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

        // ???activity??????onResume???????????????mMapView. onResume ()
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mNaviHelper.pause();

        executorService.shutdown();
        isQuit = true;
        refreshExecutorService.shutdownNow();
        // ???activity??????onPause???????????????mMapView. onPause ()
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
        // ??????bitmap
        mBitmap.recycle();
        // ???????????????????????????
        mSensorManager.unregisterListener(this);
        // ?????????????????????
        mLocClient.stop();
        // ??????????????????
        mBaiduMap.setMyLocationEnabled(false);
        // ??????????????????
        mBaiduMap.clear();
        // ???activity??????onDestroy???????????????mMapView.onDestroy()
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
                        // TODO: 2021/4/20 ???????????????????????????????????????
                        if(GroupTourApi.getInstance().isOpenLogger() && weakReference.get() != null) {
//                            ToastUtils.showToast("???" +
//                                    (weakReference.get().mClusterManager.getMarkerCollection().getMarkers().size() +
//                                    weakReference.get().mClusterManager.getClusterMarkerCollection().getMarkers().size()) +
//                                    "??????");
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
                    .accuracy(mCurrentAccracy)// ????????????????????????????????????????????????
                    .direction(mCurrentDirection)// ?????????????????????????????????????????????????????????0-360
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
     * ???????????????
     */
    public  void initLocation(){
        // ??????????????????
        mBaiduMap.setMyLocationEnabled(true);
        // ???????????????
        mLocClient = new LocationClient(this);
        mLocClient.registerLocationListener(myListener);
        LocationClientOption option = new LocationClientOption();
        // ??????gps
        option.setOpenGps(true);
        // ??????????????????
        option.setCoorType("bd09ll");
        option.setScanSpan(2000);
        mLocClient.setLocOption(option);
        mLocClient.start();
    }

    /**
     * ????????????????????????????????????
     */
    public void setLocEnable(View v){
        if(isLocationLayerEnable){
            mBaiduMap.setMyLocationEnabled(false);
            ((Button) v).setText("??????????????????");
            isLocationLayerEnable = !isLocationLayerEnable;
        }else{
            mBaiduMap.setMyLocationEnabled(true);
            mBaiduMap.setMyLocationData(myLocationData);
            ((Button) v).setText("??????????????????");
            isLocationLayerEnable = !isLocationLayerEnable;
        }
    }

    /**
     * ????????????????????????????????????????????????
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
     * ??????????????????
     */
    public void setNormalType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // ??????null?????????????????????
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
        MapStatus.Builder builder1 = new MapStatus.Builder();
        builder1.overlook(0);
        builder1.zoom(18.0f);
        mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder1.build()));
    }

    /**
     * ??????????????????
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
     * ??????????????????
     */
    public void setCompassType(View v){
        mCurrentMode = MyLocationConfiguration.LocationMode.COMPASS;
        mBaiduMap.setMyLocationConfiguration(new MyLocationConfiguration(mCurrentMode, true, null));
    }

    /**
     * ??????SDK????????????
     */
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location) {
            // MapView ???????????????????????????????????????
            if (location == null || mMapView == null) {
                return;
            }
            mCurrentLat = location.getLatitude();
            mCurrentLon = location.getLongitude();
            mCurrentAccracy = location.getRadius();
            myLocationData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())// ????????????????????????????????????????????????
                    .direction(mCurrentDirection)// ?????????????????????????????????????????????????????????0-360
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
                if(isOnResume && isFirstLoc) { // ?????????????????????????????????????????????????????????????????????
                    mBaiduMap.clear();
                    if (destinationLoc.latitude != 39.915071 && destinationLoc.longitude != 116.403907) { // ???????????????????????????????????????
                        // ??????????????????
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.target(destinationLoc);
                        builder.zoom(15.0f); // ?????????????????????
                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    }else {
                        // ??????????????????????????????
                        ToastUtils.showToast("??????????????????????????????????????????");
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.target(latLng);
                        builder.zoom(15.0f); // ?????????????????????
                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        destinationLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    }

                    // ????????????????????????????????????????????????
//                    destinationResearch(null);

                    isOnResume = false;
                }
            }
        }
    }

    // ??????????????????
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
                            ToastUtils.showToast("?????????????????????????????????");
                            return;
                        }
                        WeatherSearchRealTime weatherSearchRealTime = weatherResult.getRealTimeWeather();
                        if (null == weatherSearchRealTime) {
                            return;
                        }
                        TextView tv_title = findViewById(R.id.tv_title);
                        String weatherTitle = "???????????????" + myTeam.getDestinationName();
                        tv_title.setText(weatherTitle);
                        TextView txtRelativeHumdidity = findViewById(R.id.txtRelativeHumidity);
                        String relativeHumidity =
                                "???????????????" + weatherSearchRealTime.getRelativeHumidity() + "%";
                        txtRelativeHumdidity.setText(relativeHumidity);
                        TextView txtSensoryTemp = findViewById(R.id.txtSensoryTemp);
                        String sensoryTemp = "???????????????" + String.valueOf(weatherSearchRealTime.getSensoryTemp()) + "???";
                        txtSensoryTemp.setText(sensoryTemp);
                        TextView txtPhenomenon = findViewById(R.id.txtPhenomenon);
                        String phenomenon = "???????????????"+weatherSearchRealTime.getPhenomenon();
                        txtPhenomenon.setText(phenomenon);
                        TextView txtWindDirection = findViewById(R.id.txtWindDirection);
                        String windDirection = "?????????" + weatherSearchRealTime.getWindDirection();
                        txtWindDirection.setText(windDirection);
                        TextView txtWindPower = findViewById(R.id.txtWindPower);
                        String windPower = "?????????" + weatherSearchRealTime.getWindPower();
                        txtWindPower.setText(windPower);
                        TextView txtTemp = findViewById(R.id.txtTemp);
                        String temp = "?????????" + weatherSearchRealTime.getTemperature() + "???";
                        txtTemp.setText(temp);
                        TextView txtUpdateTime = findViewById(R.id.txtUpdateTime);
                        String updateTime = weatherSearchRealTime.getUpdateTime();
                        updateTime = updateTime.substring(0, 4) + "???" + updateTime.substring(4, 6) + "???" + updateTime.substring(6, 8) + "??? " + updateTime.substring(8, 10) + ":" + updateTime.substring(10, 12);
                        txtUpdateTime.setText(updateTime);




                        Button btn_setAlarm = findViewById(R.id.btn_setAlarm);
                        btn_setAlarm.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onClick(View view) {
                                try {
                                    CalendarReminderUtils.addCalendarEvent(LocMapActivity.this,myTeam.getDestinationName() + "??????","?????????????????????" + myTeam.getDestinationName() + "??????????????????????????????~\n   ?????????????????????App",
                                            CommonUtils.stringToLong(CommonUtils.dateToString(myTeam.getDepartureDateTime(), "yyyy-MM-dd"),"yyyy-MM-dd") - 1000*3600*6,0);
                                    CalendarReminderUtils.addCalendarEvent(LocMapActivity.this,myTeam.getDestinationName() + "??????","?????????????????????" + myTeam.getDestinationName() + "????????????????????????????????????????????????????????????~\n   ?????????????????????App",
                                            myTeam.getDepartureDateTime().getTime() - 1000*3600*2,0);
                                    ToastUtils.showToast("??????????????????????????????18??????????????????2????????????????????????");
                                } catch (ParseException e) {
                                    ToastUtils.showToast("???????????????????????????");
                                }
                            }
                        });

                        if(!drawerLayout.isOpen()) {
                            drawerLayout.openDrawer(GravityCompat.END);
                            StatusBarUtils.setStatusBarMode(LocMapActivity.this, true, R.color.teal_200);
                        }
                        else { // ????????????else???????????????
                            drawerLayout.closeDrawer(GravityCompat.END);
                            StatusBarUtils.setTranslucentStatusTextMode(LocMapActivity.this, true);
                        }
                    }
                });
            }
        });
        mWeatherSearch.request(weatherSearchOption);
    }


    // ?????????????????????????????????????????????
    private void getInfo() {
        executorService = Executors.newSingleThreadExecutor();
        if(getMyInfo()) {
            if(getTeamInfo()) {
                if(getGroupmatesInfo()) {
                    if(GroupTourApi.getInstance().isOpenLogger())
                        ToastUtils.showToast("?????????????????????????????????????????????");
                } else {
                    if(GroupTourApi.getInstance().isOpenLogger())
                        ToastUtils.showToast("?????????????????????????????????");
                }
            } else {
                if(GroupTourApi.getInstance().isOpenLogger())
                    ToastUtils.showToast("???????????????????????????");
            }
        } else {
            if(GroupTourApi.getInstance().isOpenLogger())
                ToastUtils.showToast("???????????????????????????");
        }
        executorService.shutdown();
    }

    // ??????????????????
    private boolean getMyInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Person p = DBUtil.selectPerson(me.getName());
                return p;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 ??????????????????
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

    // ???????????????????????????????????????
    private boolean getTeamInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Team t = DBUtil.selectTeam(me.getTeamID());
                return t;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 ??????????????????
                myTeam = (Team) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // ?????????????????????????????????????????????
    private boolean getGroupmatesInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                ArrayList<GroupmateInfo> groupmates = DBUtil.selectGroupmates(me.getTeamID());
                return groupmates;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 ????????????????????????
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

    // ???3???????????????????????????
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

                        // mBaiduMap.clear(); // ??????
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                mClusterManager.clearItems();
                                addMarkers(); //??????UI
                                mClusterManager.cluster();
                            }
                        });
                    }
                }
            }
        });
    }

    // TODO: 2021/4/19 ??????????????????????????????
    /**
     * ???????????????Marker???
     */
    public void addMarkers() {
        // ??????Marker???
        List<MyItem> items = new ArrayList<MyItem>();
        for(GroupmateInfo g : groupmates) {
            // ??????Marker???
            if (g.getName().equals(me.getName())) continue;
//            items.add(new MyItem(new LatLng(g.getLatitude(), g.getLongitude())));
            items.add(new MyItem(g));
        }
        mClusterManager.addItems(items);
    }

    /**
     * ??????Marker????????????Marker?????????????????????
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
        if (destinationLoc.latitude != 39.915071 && destinationLoc.longitude != 116.403907) { // ???????????????????????????????????????
            // ??????????????????
            MapStatus.Builder builder = new MapStatus.Builder();
            builder.target(destinationLoc);
            builder.zoom(14.0f); // ?????????????????????
            mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        }

        MarkerOptions markerOptions = new MarkerOptions()
                .position(destinationLoc)// ?????????
                .icon(mBitmap) // ?????? Marker ??????????????????
                .clickable(true); // ??????Marker???????????????
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.grow);
        destinationMarker = (Marker) (mBaiduMap.addOverlay(markerOptions));

        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
                if(marker.getPosition() == destinationLoc) {
                    Button button = new Button(getApplicationContext());
                    button.setBackgroundResource(R.drawable.popup);
                    InfoWindow.OnInfoWindowClickListener listener = null;
                    button.setText("??????????????????");
                    button.setTextColor(Color.BLACK);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            /*?????????????????????????????????*/
                            WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                            walkStartNode.setLocation(startLoc);
                            WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                            walkEndNode.setLocation(destinationLoc);
                            walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
                            walkParam.extraNaviMode(0);
                            /* ??????????????????Marker */
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
     * ??????????????????
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
     * ????????????????????????
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
                        ToastUtils.showToast("??????????????????50????????????????????????????????????");
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
                Toast.makeText(LocMapActivity.this, "??????????????????,??????????????????", Toast.LENGTH_SHORT).show();
            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mNaviHelper.startCameraAndSetMapView(LocMapActivity.this);
            }
        }
    }

    public static Bitmap zoomImg(Bitmap bm, int newWidth, int newHeight) {
        //?????????????????????
        int width = bm.getWidth();
        int height = bm.getHeight();
        //??????????????????
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        //?????????????????????matrix??????
        Matrix matrix = new Matrix();
        matrix.postScale(scaleWidth, scaleHeight);
        //??????????????????
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