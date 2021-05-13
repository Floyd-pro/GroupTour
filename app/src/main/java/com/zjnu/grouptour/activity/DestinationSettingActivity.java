package com.zjnu.grouptour.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.weather.LanguageType;
import com.baidu.mapapi.search.weather.OnGetWeatherResultListener;
import com.baidu.mapapi.search.weather.WeatherDataType;
import com.baidu.mapapi.search.weather.WeatherResult;
import com.baidu.mapapi.search.weather.WeatherSearch;
import com.baidu.mapapi.search.weather.WeatherSearchOption;
import com.baidu.mapapi.search.weather.WeatherSearchRealTime;
import com.baidu.mapapi.search.weather.WeatherServerType;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.ApplicationExlike;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.api.GroupTourApi;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.bean.Team;
import com.zjnu.grouptour.search.KeybordUtil;
import com.zjnu.grouptour.search.PoiListAdapter;
import com.zjnu.grouptour.service.LocationService;
import com.zjnu.grouptour.service.Utils;
import com.zjnu.grouptour.utils.CalendarReminderUtils;
import com.zjnu.grouptour.utils.CommonUtils;
import com.zjnu.grouptour.utils.ConstantUtils;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import cn.addapp.pickers.entity.City;
import cn.addapp.pickers.entity.County;
import cn.addapp.pickers.entity.Province;

/**
 * @author luchen
 * @Date 2021/4/10 18:20
 * @Description 目的地设置界面
 */
public class DestinationSettingActivity extends BaseActivity implements BaiduMap.OnMapClickListener,
        OnGetPoiSearchResultListener, OnGetSuggestionResultListener,
        AdapterView.OnItemClickListener, PoiListAdapter.OnGetChildrenLocationListener, SensorEventListener {

    private final static String TAG = DestinationSettingActivity.class.getSimpleName();

    // 地图相关，使用继承MapView的MyRouteMapView目的是重写touch事件实现泡泡处理
    // 如果不处理touch事件，则无需继承，直接使用MapView即可
    private MapView mMapView = null;    // 地图View
    private BaiduMap mBaiduMap = null;

    //定位
    private LocationService locationService;
    private LocationClientOption mOption;
    private TextView tv_locResult;
    // 定位图层显示方式
    private MyLocationConfiguration.LocationMode mCurrentMode;
    private SensorManager mSensorManager;
    private Double lastX = 0.0;
    private int mCurrentDirection = 0;
    private double mCurrentLat = 0.0;
    private double mCurrentLon = 0.0;
    private float mCurrentAccracy;
    // 是否首次定位 定位图层用
    private boolean isFirstLoc = true;
    // 是否开启定位图层
    private boolean isLocationLayerEnable = true;
    private MyLocationData myLocationData;

    // 城市POI搜索
    private PoiSearch mPoiSearch = null;
    private RelativeLayout mPoiDetailView;
    private ListView mPoiList;
    // 提示POI搜索

    // 分页
    private int mLoadIndex = 0;
    private List<PoiInfo> mAllPoi;
    private BitmapDescriptor mBitmap = BitmapDescriptorFactory.fromResource(R.drawable.water_drop);

    // 路线规划 搜索模块
    private boolean hasShowDialog = false;
    private EditText mEditStartCity;
    private EditText mEditDestination;
    private AutoCompleteTextView mStartNodeView;
    // 浏览路线节点相关
    private boolean mUseDefaultIcon = false;


    private static int setSensitivity = LocationClientOption.LOC_SENSITIVITY_HIGHT;
    private static boolean limit = true;
    private static int scope = 2;
    private static boolean isOnResume = false; // 用作 界面出现时，根据用户定位刷新地图
    private static boolean isStartItemClicked = false; // 处理用户在未点击ListView中显示的查询结果的条件下点击搜索按钮，导致路线查询无结果的bug
    private static boolean isRepeatEmpty = false; // 是否重复清空了AutoCompleteTextView中的文本
    private static LatLng destinationLoc; // 保存点击PoiDetailView中某一项的经纬度信息 或是 刚打开界面时用户定位的经纬度信息
    private static boolean isPoiDetailViewClicked = false;  // 防止点击PoiDetailView后 地图重复刷新bug
    private static boolean isDestinationResearchClick = false;

    ExecutorService executorService; // 用于把信息 更新到数据库
    private Person me;
    private Team myTeam = null;
    private Marker mMarker;
    private InfoWindow mInfoWindow;

    private WeatherSearch mWeatherSearch;
    private GeoCoder mGeoCoder;
    private String mDistrict;
    private boolean isMarkerAppeared = false;
    private Button btn_setAlarm;

    private boolean isNotPrincipal = false;

    private DrawerLayout drawerLayout;

    public static void actionStart(Context context, Team team, String groupId) {
        Intent intent = new Intent(context, DestinationSettingActivity.class);
        intent.putExtra("team", team);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.destination_setting_with_weather_drawer);

        drawerLayout = findViewById(R.id.destination_drawer_layout);

        StatusBarUtils.setStatusBarMode(DestinationSettingActivity.this, true, R.color.teal_200);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // 为了在软键盘弹出时，不把布局顶上去

        me = new Person();
        me.setName(DemoHelper.getInstance().getCurrentUser());

        if(getIntent().hasExtra("team"))
            myTeam = getIntent().getExtras().getParcelable("team");
        else {
            getInfo();
            isNotPrincipal = true;
        }

        btn_setAlarm = findViewById(R.id.btn_setAlarm);
        if(getIntent().hasExtra("isSetting")) {
            if(getIntent().getBooleanExtra("isSetting", true) == false) {
                btn_setAlarm.setVisibility(View.GONE);
            }
        }

        // 初始化地图
        mMapView = (MapView) findViewById(R.id.map);
        mBaiduMap = mMapView.getMap();

        // 定位图层相关 初始化
        // 获取传感器管理服务
        mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        mCurrentMode = MyLocationConfiguration.LocationMode.NORMAL;
        // 为系统的方向传感器注册监听器
        mSensorManager.registerListener(this, mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION), SensorManager.SENSOR_DELAY_UI);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        // 设置普通模式（随着用户设备方向旋转）
        setNormalType(null);

        // 定位相关 初始化
        tv_locResult = (TextView) findViewById(R.id.tv_weatherResult);
        tv_locResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        // 是否显示定位信息
        tv_locResult.setVisibility(View.GONE);

        mEditStartCity = (EditText) findViewById(R.id.st_city);
        mEditDestination = (EditText) findViewById(R.id.et_destination);
//        mStartNodeView = (AutoCompleteTextView) findViewById(R.id.st_destination);

        // 地图点击事件处理
        mBaiduMap.setOnMapClickListener(this);

        // 创建poi检索实例，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        // 默认 天安门
        destinationLoc = new LatLng(39.915071, 116.403907);
        // 设置 目的地经纬度
        if(myTeam.getDestinationLongitude() != 0 && myTeam.getDestinationLatitude() != 0) {
            destinationLoc = new LatLng(myTeam.getDestinationLatitude(), myTeam.getDestinationLongitude());
        }
        // 展示父子节点控件
        mPoiDetailView = (RelativeLayout) findViewById(R.id.poi_detail);
        mPoiList = (ListView) findViewById(R.id.poi_list);
        mPoiList.setOnItemClickListener(this);

        mEditStartCity.setFocusable(false);
        mEditStartCity.setKeyListener(null);
        mEditStartCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddressPickTask task = new AddressPickTask(DestinationSettingActivity.this);
                task.setHideCounty(true);
                task.setCallback(new AddressPickTask.Callback() {
                    @Override
                    public void onAddressInitFailed() {
                        ToastUtils.showToast("数据初始化失败");
                    }

                    @Override
                    public void onAddressPicked(Province province, City city, County county) {
                        String cityName = city.getAreaName();
                        if(cityName.contains("市"))
                            cityName = cityName.substring(0, cityName.length()-1);
                        mEditStartCity.setText(cityName);
//                        ToastUtils.showToast(province.getAreaName() + " " + city.getAreaName());
                    }
                });
                task.execute("北京市", "北京市");
            }
        });

        mEditDestination.addTextChangedListener(new TextWatcher() {

            boolean limit = true;
            int scope = 2;

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!isOnResume) {
                    isOnResume = false;
                    //  按搜索按钮时隐藏软键盘，为了在结果回调时计算 PoiDetailView 控件的高度，把地图中poi展示到合理范围内
//                KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);
                    if(editable.toString().equals("") && !isRepeatEmpty) {
                        // TODO: 2021/4/11 显示用户所在区域的推荐搜索
                        ToastUtils.showToast("您想查询哪里的天气？");
                        return;
                    }else if(editable.toString().trim().equals("")) {
                        // 清除之前的覆盖物
                        mBaiduMap.clear();
                        return;
                    }
                    // 获取检索城市
                    String cityStr = mEditStartCity.getText().toString().trim();
                    // 获取检索关键字
                    String keyWordStr = editable.toString().trim();
                    // 发起请求
                    mPoiSearch.searchInCity((new PoiCitySearchOption())
                            .city(cityStr)
                            .keyword(keyWordStr)
                            .pageNum(mLoadIndex) // 分页编号
                            .cityLimit(limit)
                            .scope(scope));
                }
            }
        });

        mEditDestination.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(view.isFocused()) {
//                    if (mStartNodeView.getText().toString().trim().equals("")) {
                    if (mEditDestination.getText().toString().trim().equals("")) {
                        // TODO: 2021/4/11 显示用户所在区域的推荐搜索
                        ToastUtils.showToast("您想查询哪里的天气？");
                        isRepeatEmpty = true;
                    }
                }else if(!isStartItemClicked) {
                    destinationLoc = mAllPoi.get(0).location; // 用作 目的地输入完毕时，更新目的地数据
                }
            }
        });

        mEditDestination.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View view, int keyCode, KeyEvent keyEvent) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    // 监听到回车键，会执行2次该方法：按下与松开
                    if (keyEvent.getAction() == KeyEvent.ACTION_UP) {
                        //按下事件
                        destinationResearch(null);
                    }
                }
                return false;
            }
        });

//        mStartNodeView.addTextChangedListener(new TextWatcher() {
//
//            boolean limit = true;
//            int scope = 2;
//
//            @Override
//            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
//
//            }
//
//            @Override
//            public void afterTextChanged(Editable editable) {
//                if(!isOnResume) {
//                    isOnResume = false;
//                    //  按搜索按钮时隐藏软键盘，为了在结果回调时计算 PoiDetailView 控件的高度，把地图中poi展示到合理范围内
////                KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);
//                    if(editable.toString().equals("") && !isRepeatEmpty) {
//                        // TODO: 2021/4/11 显示用户所在区域的推荐搜索
//                        ToastUtils.showToast("您想查询哪里的天气？");
//                        return;
//                    }else if(editable.toString().trim().equals("")) {
//                        // 清除之前的覆盖物
//                        mBaiduMap.clear();
//                        return;
//                    }
//                    // 获取检索城市
//                    String cityStr = mEditStartCity.getText().toString().trim();
//                    // 获取检索关键字
//                    String keyWordStr = editable.toString().trim();
//                    // 发起请求
//                    mPoiSearch.searchInCity((new PoiCitySearchOption())
//                            .city(cityStr)
//                            .keyword(keyWordStr)
//                            .pageNum(mLoadIndex) // 分页编号
//                            .cityLimit(limit)
//                            .scope(scope));
//                }
//            }
//        });
//
//        mStartNodeView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View view, boolean b) {
//                if(view.isFocused()) {
//                    if (mStartNodeView.getText().toString().trim().equals("")) {
//                        // TODO: 2021/4/11 显示用户所在区域的推荐搜索
//                        ToastUtils.showToast("您想查询哪里的天气？");
//                        isRepeatEmpty = true;
//                    }
//                }else if(!isStartItemClicked) {
//                    destinationLoc = mAllPoi.get(0).location; // 用作 目的地输入完毕时，更新目的地数据
//                }
//            }
//        });

        // -----------location config ------------
        locationService = ((ApplicationExlike) getApplication()).locationService;
        mOption = new LocationClientOption();
        mOption = locationService.getDefaultLocationClientOption();
        // 打开gps
        mOption.setOpenGps(true);
        // 设置坐标类型
        mOption.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setScanSpan(2000); // 每2秒更新一次用户当前位置
        mOption.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);

        switch (ConstantUtils.LOCATION_MODE) {
            case "hight":
                setSensitivity = LocationClientOption.LOC_SENSITIVITY_HIGHT;
                break;
            case "middle":
                mOption.setLocationMode(LocationClientOption.LocationMode.Battery_Saving);
                setSensitivity = LocationClientOption.LOC_SENSITIVITY_MIDDLE;
                break;
            case "low":
                mOption.setLocationMode(LocationClientOption.LocationMode.Device_Sensors);
                setSensitivity = LocationClientOption.LOC_SENSITIVITY_LOW;
                break;
            default:
                Toast.makeText(DestinationSettingActivity.this, "定位模式取值错误！", Toast.LENGTH_LONG).show();
                break;
        }
        /**
         * setOpenAutoNotifyMode方法有bug，设置成2秒，结果还是1秒1刷新，参数设置无效
         */
        //设置打开自动回调位置模式，该开关打开后，期间只要定位SDK检测到位置变化就会主动回调给开发者
//        mOption.setOpenAutoNotifyMode(LOCATION_TIME_SPACING, LOCATION_DISTANCE_SPACING, setSensitivity);
        locationService.setLocationOption(mOption);
        //获取locationservice实例，建议应用中只初始化1个location实例，然后使用，可以参考其他示例的activity，都是通过此种方式获取locationservice实例的
        locationService.registerListener(mListener);


//        if(mStartNodeView.getText().toString().trim().equals("")) {
//            Toast.makeText(DestinationSettingActivity.this, "请输入起点信息！", Toast.LENGTH_LONG).show();
//            return;
//        }
//        /**
//         * 更新起终点参数 防止用户在未点击ListView中显示的查询结果的条件下直接点击导航按钮，导致导航信息出错
//         */
//        // 起点参数
//        if(isStartItemClicked && isStartTextChanged) {
//            isStartItemClicked = false;
//        }
//        else if(isStartTextChanged) {
//            destinationLoc = mAllPoi.get(0).location;
//        }
//
//        showPoiDetailView(false);
//        KeybordUtil.closeKeybord(DestinationSettingActivity.this);
//
//        // 清除之前的覆盖物
////                mBaiduMap.clear();
//        if(getCurrentFocus() != null) {
//            getCurrentFocus().clearFocus();
//        }

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
    }

    @Override
    protected void onStart() {
        super.onStart();
        locationService.start();// 定位SDK
        // start之后会默认发起一次定位请求，开发者无须判断isstart并主动调用request
//        locationService.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        isOnResume = true;
        // 隐藏控件
        showPoiDetailView(false);
        mMapView.onResume();
        if(getCurrentFocus() != null) {
            getCurrentFocus().clearFocus();
        }
    }

    // DESCRIPTION: 2021/4/11 Activity生命周期
    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
        locationService.unregisterListener(mListener); //注销掉监听
        locationService.stop(); //停止定位服务
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMarker = null;
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        // 释放bitmap
        mBitmap.recycle();
//        // 退出时销毁定位
//        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // 清空地图所有的覆盖物
        mBaiduMap.clear();
        // 释放地图组件
        mMapView.onDestroy();
    }

    // DESCRIPTION: 2021/4/11 以下方法与 用户定位 相关
    /**
     * 显示请求字符串
     *
     * @param str
     */
    public void logMsg(final String str, final int tag) {

        try {
            if (tv_locResult != null) {
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        tv_locResult.post(new Runnable() {
                            @Override
                            public void run() {
                                if (tag == Utils.RECEIVE_TAG) {
                                    tv_locResult.setText(str);
                                } else if (tag == Utils.DIAGNOSTIC_TAG) {
                                    tv_locResult.setText(str);
                                }
                            }
                        });
                    }
                }).start();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*****
     *
     * 定位结果回调，重写onReceiveLocation方法，可以直接拷贝如下代码到自己工程中修改
     *
     */
    private BDAbstractLocationListener mListener = new BDAbstractLocationListener() {

        /**
         * 定位请求回调函数
         * @param location 定位结果
         */
        @Override
        public void onReceiveLocation(BDLocation location) {
            /**
             * 定位图层
             */
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
            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng ll = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(ll).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
            }

            if (null != location && location.getLocType() != BDLocation.TypeServerError) {
                int tag = 1;
                StringBuffer sb = new StringBuffer(256);
                sb.append("time : ");
                /**
                 * 时间也可以使用systemClock.elapsedRealtime()方法 获取的是自从开机以来，每次回调的时间；
                 * location.getTime() 是指服务端出本次结果的时间，如果位置不发生变化，则时间不变
                 */
                sb.append(location.getTime());
                sb.append("\nlatitude : ");// 纬度
                sb.append(location.getLatitude());
                sb.append("\nlongtitude : ");// 经度
                sb.append(location.getLongitude());
                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****
                sb.append(location.getUserIndoorState());
                sb.append("\nUserIndoorState: ");// *****返回用户室内外判断结果*****

                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    sb.append("\nspeed : ");
                    sb.append(location.getSpeed());// 速度 单位：km/h
                    sb.append("\nsatellite : ");
                    sb.append(location.getSatelliteNumber());// 卫星数目
                    sb.append("\nheight : ");
                    sb.append(location.getAltitude());// 海拔高度 单位：米
                    sb.append("\ngps status : ");
                    sb.append(location.getGpsAccuracyStatus());// *****gps质量判断*****
                    sb.append("\ndescribe : ");
                    sb.append("gps定位成功");
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    // 运营商信息
                    if (location.hasAltitude()) {// *****如果有海拔高度*****
                        sb.append("\nheight : ");
                        sb.append(location.getAltitude());// 单位：米
                    }
                    sb.append("\noperationers : ");// 运营商信息
                    sb.append(location.getOperators());
                    sb.append("\ndescribe : ");
                    sb.append("网络定位成功");
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    sb.append("\ndescribe : ");
                    sb.append("离线定位成功，离线定位结果也是有效的");
                } else if (location.getLocType() == BDLocation.TypeServerError) {
                    sb.append("\ndescribe : ");
                    sb.append("服务端网络定位失败，可以反馈IMEI号和大体定位时间到loc-bugs@baidu.com，会有人追查原因");
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {
                    sb.append("\ndescribe : ");
                    sb.append("网络不同导致定位失败，请检查网络是否通畅");
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {
                    sb.append("\ndescribe : ");
                    sb.append("无法获取有效定位依据导致定位失败，一般是由于手机的原因，处于飞行模式下一般会造成这种结果，可以试着重启手机");
                }
                sb.append("\n");
                logMsg(sb.toString(), tag);

                if(isOnResume) { // 界面出现时，根据用户定位刷新地图
//                    mEditStartCity.setText(location.getCity().substring(0, location.getCity().lastIndexOf("市")));
//                    if (location.getPoiList() != null && !location.getPoiList().isEmpty())
//                        mEditDestination.setText(location.getPoiList().get(0).getName());
////                        mStartNodeView.setText(location.getPoiList().get(0).getName());
//
//                    mBaiduMap.clear();
//                    showPoiDetailView(false);


                    if (destinationLoc.latitude != 39.915071 && destinationLoc.longitude != 116.403907) { // 若已获取到队伍目的地经纬度
                        // 目的地的位置
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.target(destinationLoc);
                        builder.zoom(14.0f); // 调节地图比例尺
                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        mEditStartCity.setText(myTeam.getDestinationCity().substring(0, myTeam.getDestinationCity().indexOf("市")));
                        mEditDestination.setText(myTeam.getDestinationName());
                    }else {
                        // 用户打开界面时的位置
                        ToastUtils.showToast("队伍目的地位置信息获取失败！");
                        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                        MapStatus.Builder builder = new MapStatus.Builder();
                        builder.target(latLng);
                        builder.zoom(14.0f); // 调节地图比例尺
                        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                        mEditDestination.setText(myTeam.getDestinationName());
                        destinationLoc = new LatLng(location.getLatitude(), location.getLongitude());
                    }

                    // 自动搜索当前搜索框内显示的目的地
                    destinationResearch(null);

                    isOnResume = false;
                }
            }
        }

        @Override
        public void onConnectHotSpotMessage(String s, int i) {
            super.onConnectHotSpotMessage(s, i);
        }

        /**
         * 回调定位诊断信息，开发者可以根据相关信息解决定位遇到的一些问题
         * @param locType 当前定位类型
         * @param diagnosticType 诊断类型（1~9）
         * @param diagnosticMessage 具体的诊断信息释义
         */
        @Override
        public void onLocDiagnosticMessage(int locType, int diagnosticType, String diagnosticMessage) {
            super.onLocDiagnosticMessage(locType, diagnosticType, diagnosticMessage);
            int tag = 2;
            StringBuffer sb = new StringBuffer(256);
            sb.append("诊断结果: ");
            if (locType == BDLocation.TypeNetWorkLocation) {
                if (diagnosticType == 1) {
                    sb.append("网络定位成功，没有开启GPS，建议打开GPS会更好");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 2) {
                    sb.append("网络定位成功，没有开启Wi-Fi，建议打开Wi-Fi会更好");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeOffLineLocationFail) {
                if (diagnosticType == 3) {
                    sb.append("定位失败，请您检查您的网络状态");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeCriteriaException) {
                if (diagnosticType == 4) {
                    sb.append("定位失败，无法获取任何有效定位依据");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 5) {
                    sb.append("定位失败，无法获取有效定位依据，请检查运营商网络或者Wi-Fi网络是否正常开启，尝试重新请求定位");
                    sb.append(diagnosticMessage);
                } else if (diagnosticType == 6) {
                    sb.append("定位失败，无法获取有效定位依据，请尝试插入一张sim卡或打开Wi-Fi重试");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 7) {
                    sb.append("定位失败，飞行模式下无法获取有效定位依据，请关闭飞行模式重试");
                    sb.append("\n" + diagnosticMessage);
                } else if (diagnosticType == 9) {
                    sb.append("定位失败，无法获取任何有效定位依据");
                    sb.append("\n" + diagnosticMessage);
                }
            } else if (locType == BDLocation.TypeServerError) {
                if (diagnosticType == 8) {
                    sb.append("定位失败，请确认您定位的开关打开状态，是否赋予APP定位权限");
                    sb.append("\n" + diagnosticMessage);
                }
            }
            logMsg(sb.toString(), tag);
        }
    };

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
        }
        lastX = x;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    /**
     * 设置定位图层的开启和关闭
     */
    public void setLocEnable(boolean b){
        if(b){
            mBaiduMap.setMyLocationEnabled(false);
            Toast.makeText(DestinationSettingActivity.this, "开启定位图层", Toast.LENGTH_LONG).show();
        }else{
            mBaiduMap.setMyLocationEnabled(true);
            mBaiduMap.setMyLocationData(myLocationData);
            Toast.makeText(DestinationSettingActivity.this, "关闭定位图层", Toast.LENGTH_LONG).show();
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
     * 设置底图显示模式
     */
//    public void setMapMode(View view) {
//        boolean checked = ((RadioButton) view).isChecked();
//        switch (view.getId()) {
//            // 普通图
//            case R.id.normal:
//                if (checked) {
//                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
//                }
//                break;
//            // 卫星图
//            case R.id.statellite:
//                if (checked) {
//                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
//                }
//                break;
//            // 空白地图
//            case R.id.none:
//                if (checked) {
//                    mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NONE);
//                }
//                break;
//            default:
//                break;
//        }
//    }

    /**
     * 清除地图缓存数据，支持清除普通地图和卫星图缓存，再次进入地图页面生效。
     */
    public void cleanMapCache(View view) {
        if (mBaiduMap == null){
            return;
        }
        int mapType = mBaiduMap.getMapType();
        if (mapType == BaiduMap.MAP_TYPE_NORMAL) {
            // // 清除地图缓存数据
            mBaiduMap.cleanCache(BaiduMap.MAP_TYPE_NORMAL);
        } else if (mapType == BaiduMap.MAP_TYPE_SATELLITE) {
            // 清除地图缓存数据
            mBaiduMap.cleanCache(BaiduMap.MAP_TYPE_SATELLITE);
        }
    }


    // DESCRIPTION: 2021/4/11 以下方法与 POI搜索 相关
    /**
     * 下一页
     */
    public void goToNextPage(View v) {
        mLoadIndex++;
        //  按搜索按钮时隐藏软件盘，为了在结果回调时计算 PoiDetailView 控件的高度，把地图中poi展示到合理范围内
        KeybordUtil.closeKeybord(DestinationSettingActivity.this);
        String cityStr, keyWordStr;

        // 获取检索城市
        cityStr = mEditStartCity.getText().toString();
        // 获取检索关键字
        keyWordStr = mEditDestination.getText().toString();
//        keyWordStr = mStartNodeView.getText().toString();
        // 发起请求
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(cityStr)
                .keyword(keyWordStr)
                .pageNum(mLoadIndex) // 分页编号
                .cityLimit(limit)
                .scope(scope));
    }

    /**
     * 获取城市poi检索结果
     *
     * @param result poi查询结果
     */
    @Override
    public void onGetPoiResult(final PoiResult result) {
        if(isPoiDetailViewClicked){
            isPoiDetailViewClicked = false;
            return; // 防止重复刷新
        }
        if (result == null || result.error == SearchResult.ERRORNO.RESULT_NOT_FOUND) {
            mLoadIndex = 0;
            // 注释原因：取消每次afterTextChanged后都进行的刷新操作
//            mBaiduMap.clear();
            showPoiDetailView(false);
            Toast.makeText(DestinationSettingActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {

            // 获取poi结果
            mAllPoi = result.getAllPoi();
            PoiListAdapter poiListAdapter = new PoiListAdapter(this, mAllPoi);
            poiListAdapter.setOnGetChildrenLocationListener(this);
            // 把poi结果添加到适配器
            mPoiList.setAdapter(poiListAdapter);

            if(isDestinationResearchClick) { // 若用户 目的地名称输入完毕，没有在列表中选择 而是直接点击查询按钮
                try {
                    PoiInfo poiInfo = mAllPoi.get(0);
                    if (poiInfo.getLocation() == null) {
                        return;
                    }
                    // 清除之前的覆盖物
                    mBaiduMap.clear();
                    try {
                        showPoiDetailView(false);
                    } catch (Exception e) {
                    }
                    try {
                        KeybordUtil.closeKeybord(DestinationSettingActivity.this);
                    } catch (Exception e) {
                    }
                    try {
                        getCurrentFocus().clearFocus();
                    } catch (Exception e) {
                    }
                    isPoiDetailViewClicked = true;
                    addPoiLocation(poiInfo.getLocation());
                    mEditDestination.setText(poiInfo.name);
//                    mStartNodeView.setText(poiInfo.name);
                    destinationLoc = poiInfo.location;
                    isStartItemClicked = true;
                } catch (Exception e) {
                    ToastUtils.showToast("查询失败！");
                } finally {
                    isDestinationResearchClick = false;
                    return;
                }
            }

            showPoiDetailView(true);
            // 注释原因：取消每次afterTextChanged后都进行的刷新操作
//            mBaiduMap.clear();
            // 监听 View 绘制完成后获取view的高度
//            mPoiDetailView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    int padding = 50;
//                    // 添加poi
//                    PoiOverlay overlay = new DestinationSettingActivity.MyPoiOverlay(mBaiduMap);
//                    mBaiduMap.setOnMarkerClickListener(overlay);
//                    overlay.setData(result);
//                    overlay.addToMap();
//                    // 获取 view 的高度
//                    int PaddingBootom = mPoiDetailView.getMeasuredHeight();
//                    // 设置显示在规定宽高中的地图地理范围
//                    overlay.zoomToSpanPaddingBounds(padding,padding,padding,PaddingBootom);
//                    // 加载完后需要移除View的监听，否则会被多次触发
//                    mPoiDetailView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
//                }
//            });
            return;
        }

        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_KEYWORD) {
            // 当输入关键字在本市没有找到，但在其他城市找到时，返回包含该关键字信息的城市列表
            String strInfo = "在";

            for (CityInfo cityInfo : result.getSuggestCityList()) {
                strInfo += cityInfo.city;
                strInfo += ",";
            }
            strInfo += "找到结果";
            Toast.makeText(DestinationSettingActivity.this, strInfo, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetPoiDetailResult(PoiDetailResult result) {

    }

    @Override
    public void onGetPoiDetailResult(PoiDetailSearchResult poiDetailSearchResult) {

    }

    @Override
    public void onGetPoiIndoorResult(PoiIndoorResult result) {

    }

    @Override
    public void onGetSuggestionResult(SuggestionResult suggestionResult) {

    }
    /**
     * poilist 点击处理
     *
     */
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        PoiInfo poiInfo = mAllPoi.get(position);
        if (poiInfo.getLocation() == null) {
            return;
        }
        isPoiDetailViewClicked = true;
        //  按搜索按钮时隐藏软件盘，为了在结果回调时计算 PoiDetailView 控件的高度，把地图中poi展示到合理范围内
        KeybordUtil.closeKeybord(DestinationSettingActivity.this);
        addPoiLocation(poiInfo.getLocation());
        mEditDestination.setText(poiInfo.name);
//        mStartNodeView.setText(poiInfo.name);
        destinationLoc = poiInfo.location;
        isStartItemClicked = true;
    }

    /**
     * 点击子节点list 获取经纬添加poi更新地图
     *
     * @param childrenLocation 子节点经纬度
     */
    @Override
    public void getChildrenLocation(LatLng childrenLocation) {

        addPoiLocation(childrenLocation);
    }

    /**
     * 更新到子节点的位置
     *
     * @param latLng 子节点经纬度
     */
    private void addPoiLocation(LatLng latLng) {
        mBaiduMap.clear();
        showPoiDetailView(false);
//        OverlayOptions markerOptions = new MarkerOptions().position(latLng).icon(mBitmap);
//        mBaiduMap.addOverlay(markerOptions);

        // 行政区划编码 更新
        ReverseGeoCodeOption rgcOption =
                new ReverseGeoCodeOption().location(latLng).radius(500);
        mGeoCoder.reverseGeoCode(rgcOption);

        MarkerOptions markerOptions = new MarkerOptions()
                .position(latLng)// 经纬度
                .icon(mBitmap) // 设置 Marker 覆盖物的图标
//                .perspective(false) // 设置是否开启 marker 覆盖物近大远小效果，默认开启
//                .anchor(0.5f, 0.5f) // 设置 marker 覆盖物的锚点比例，默认（0.5f, 1.0f）水平居中，垂直下对齐
//                .rotate(0) // 设置 marker 覆盖物旋转角度，逆时针
//                .zIndex(7); // 设置 marker 覆盖物的 zIndex
                .clickable(true); // 设置Marker是否可点击
        markerOptions.animateType(MarkerOptions.MarkerAnimateType.grow);
        mMarker = (Marker) (mBaiduMap.addOverlay(markerOptions));
        isMarkerAppeared = true;
        mBaiduMap.setOnMarkerClickListener(new BaiduMap.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker marker) {
//                String districtId = mDistrict;
//                WeatherSearchOption weatherSearchOption = new WeatherSearchOption();
//                weatherSearchOption
//                        .weatherDataType(WeatherDataType.WEATHER_DATA_TYPE_ALL)
//                        .districtID(districtId)
//                        .languageType(LanguageType.LanguageTypeChinese)
//                        .serverType(WeatherServerType.WEATHER_SERVER_TYPE_DEFAULT);
//                mWeatherSearch.setWeatherSearchResultListener(new OnGetWeatherResultListener() {
//                    @Override
//                    public void onGetWeatherResultListener(final WeatherResult weatherResult) {
//                        runOnUiThread(new Runnable() {
//                            @Override
//                            public void run() {
//                                popupWeatherDialog(weatherResult);
//                            }
//                        });
//                    }
//                });
//                mWeatherSearch.request(weatherSearchOption);

                if(!isNotPrincipal) {
                    Button button = new Button(getApplicationContext());
                    button.setBackgroundResource(R.drawable.popup);
                    InfoWindow.OnInfoWindowClickListener listener = null;
                    button.setText("设置目的地");
                    button.setTextColor(Color.BLACK);
                    button.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            popupDestinationDialog();
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

        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLng);
        builder.zoom(18.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        mEditDestination.clearFocus();
//        mStartNodeView.clearFocus();
    }

    protected class MyPoiOverlay extends PoiOverlay {
        MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            Toast.makeText(DestinationSettingActivity.this,poi.address,Toast.LENGTH_LONG).show();
            Log.e("luchen","Hello!");
            return true;
        }
    }


    /**
     * 是否展示详情 view
     *
     */
    private void showPoiDetailView(boolean whetherShow) {
        if (whetherShow) {
            mPoiDetailView.setVisibility(View.VISIBLE);
        } else {
            mPoiDetailView.setVisibility(View.GONE);
        }
    }
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    @Override
    public void onMapClick(LatLng point) {
        mBaiduMap.hideInfoWindow();
        try {
            showPoiDetailView(false);
        } catch (Exception e) {
        }
        try {
            KeybordUtil.closeKeybord(DestinationSettingActivity.this);
        } catch (Exception e) {
        }
        try {
            getCurrentFocus().clearFocus();
        } catch (Exception e) {
        }
    }

    @Override
    public void onMapPoiClick(MapPoi poi) {

    }

    // 查询目的地信息
    public void destinationResearch(View v) {
//        if(mStartNodeView.getText().toString().trim().equals("")) {
        if(mEditDestination.getText().toString().trim().equals("")) {
            ToastUtils.showToast("请输入目的地！");
            return;
        }

        // 获取检索城市
        String cityStr = mEditStartCity.getText().toString().trim();
        // 获取检索关键字
        String keyWordStr = mEditDestination.getText().toString().trim();
//        String keyWordStr = mStartNodeView.getText().toString().trim();
        isDestinationResearchClick = true;
        // 发起请求
        mPoiSearch.searchInCity((new PoiCitySearchOption())
                .city(cityStr)
                .keyword(keyWordStr)
                .pageNum(mLoadIndex) // 分页编号
                .cityLimit(limit)
                .scope(scope));
    }

    // 显示当地天气
    public void showWeather(View v) {
        if(!isMarkerAppeared) {
            ToastUtils.showToast("请搜索您的目的地！");
            return;
        }
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
                        String weatherTitle = "实时天气：" + mEditDestination.getText().toString().trim();
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




                        btn_setAlarm.setOnClickListener(new View.OnClickListener() {
                            @RequiresApi(api = Build.VERSION_CODES.N)
                            @Override
                            public void onClick(View view) {
                                try {
                                    CalendarReminderUtils.addCalendarEvent(DestinationSettingActivity.this,"天目山旅游","明天就要出发去" + myTeam.getDestinationName() + "了哦，赶紧准备一下吧~\n   ——来自提姆游App",
                                            CommonUtils.stringToLong(CommonUtils.dateToString(myTeam.getDepartureDateTime(), "yyyy-MM-dd"),"yyyy-MM-dd") - 1000*3600*6,0);
                                    CalendarReminderUtils.addCalendarEvent(DestinationSettingActivity.this,"天目山旅游","马上就要出发去" + myTeam.getDestinationName() + "了哦，最后检查一下有没有什么东西落下了吧~\n   ——来自提姆游App",
                                            myTeam.getDepartureDateTime().getTime() - 1000*3600*2,0);
                                    ToastUtils.showToast("已为您设置出发前一晚18点整和出发前2小时的日历提醒！");
                                } catch (ParseException e) {
                                    ToastUtils.showToast("设置日历闹钟错误！");
                                }
                            }
                        });

                        if(!drawerLayout.isOpen())
                            drawerLayout.openDrawer(GravityCompat.END);
                        else
                            drawerLayout.closeDrawer(GravityCompat.END);
                    }
                });
            }
        });
        mWeatherSearch.request(weatherSearchOption);
    }

    // 设置目的地
    private void popupDestinationDialog() {
        executorService = Executors.newSingleThreadExecutor();
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                if(DBUtil.updateTeam(myTeam.getTeamID(), "destination_city", mEditStartCity.getText().toString().trim() + "市")
                && DBUtil.updateTeam(myTeam.getTeamID(), "destination_name", mEditDestination.getText().toString().trim())
                && DBUtil.updateTeam(myTeam.getTeamID(), "destination_longitude", destinationLoc.longitude)
                && DBUtil.updateTeam(myTeam.getTeamID(), "destination_latitude", destinationLoc.latitude)) {
                    return "成功更改目的地：" + mEditDestination.getText().toString().trim();
                }
                return "更改目的地失败！";
            }
        });
        try {
            ToastUtils.showToast(String.valueOf(future.get()));
            executorService.shutdown();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
    }

//    private void popupWeatherDialog(WeatherResult weatherResult){
//        if (null == weatherResult) {
//            return;
//        }
//        WeatherSearchRealTime weatherSearchRealTime = weatherResult.getRealTimeWeather();
//        if (null == weatherSearchRealTime) {
//            return;
//        }
//        final AlertDialog.Builder weatherDialog =
//                new AlertDialog.Builder(this);
//        View view = View.inflate(this, R.layout.layout_weather, null);
//        if (null == view) {
//            return;
//        }
//        TextView txtRelativeHumdidity = view.findViewById(R.id.txtRelativeHumidity);
//        String relativeHumidity =
//                "相对湿度：" + weatherSearchRealTime.getRelativeHumidity() + "%";
//        txtRelativeHumdidity.setText(relativeHumidity);
//        TextView txtSensoryTemp = view.findViewById(R.id.txtSensoryTemp);
//        String sensoryTemp = "体感温度：" + String.valueOf(weatherSearchRealTime.getSensoryTemp()) + "℃";
//        txtSensoryTemp.setText(sensoryTemp);
//        TextView txtPhenomenon = view.findViewById(R.id.txtPhenomenon);
//        String phenomenon = "天气现象："+weatherSearchRealTime.getPhenomenon();
//        txtPhenomenon.setText(phenomenon);
//        TextView txtWindDirection = view.findViewById(R.id.txtWindDirection);
//        String windDirection = "风向：" + weatherSearchRealTime.getWindDirection();
//        txtWindDirection.setText(windDirection);
//        TextView txtWindPower = view.findViewById(R.id.txtWindPower);
//        String windPower = "风力：" + weatherSearchRealTime.getWindPower();
//        txtWindPower.setText(windPower);
//        TextView txtTemp = view.findViewById(R.id.txtTemp);
//        String temp = "温度：" + weatherSearchRealTime.getTemperature() + "℃";
//        txtTemp.setText(temp);
//        TextView txtUpdateTime = view.findViewById(R.id.txtUpdateTime);
//        String updateTime = "更新时间：" + weatherSearchRealTime.getUpdateTime();
//        txtUpdateTime.setText(updateTime);
//        weatherDialog.setTitle("实时天气").setView(view).create();
//        weatherDialog.show();
//    }

    // 获取用户、队伍及队伍成员的信息
    private void getInfo() {
        executorService = Executors.newSingleThreadExecutor();
        if(getMyInfo()) {
            if(getTeamInfo()) {
                if(GroupTourApi.getInstance().isOpenLogger())
                    ToastUtils.showToast("个人及队伍信息获取成功！");
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
                me = (Person) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
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
                myTeam = (Team) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }
}
