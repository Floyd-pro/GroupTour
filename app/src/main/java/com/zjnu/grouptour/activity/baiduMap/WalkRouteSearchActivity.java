package com.zjnu.grouptour.activity.baiduMap;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;

import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
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

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.bikenavi.BikeNavigateHelper;
import com.baidu.mapapi.bikenavi.adapter.IBEngineInitListener;
import com.baidu.mapapi.bikenavi.adapter.IBRoutePlanListener;
import com.baidu.mapapi.bikenavi.model.BikeRoutePlanError;
import com.baidu.mapapi.bikenavi.params.BikeNaviLaunchParam;
import com.baidu.mapapi.bikenavi.params.BikeRouteNodeInfo;
import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BitmapDescriptor;
import com.baidu.mapapi.map.BitmapDescriptorFactory;
import com.baidu.mapapi.map.MapPoi;
import com.baidu.mapapi.map.MapStatus;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.MyLocationConfiguration;
import com.baidu.mapapi.map.MyLocationData;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.overlayutil.OverlayManager;
import com.baidu.mapapi.overlayutil.PoiOverlay;
import com.baidu.mapapi.overlayutil.WalkingRouteOverlay;
import com.baidu.mapapi.search.core.CityInfo;
import com.baidu.mapapi.search.core.PoiInfo;
import com.baidu.mapapi.search.core.RouteLine;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.poi.OnGetPoiSearchResultListener;
import com.baidu.mapapi.search.poi.PoiCitySearchOption;
import com.baidu.mapapi.search.poi.PoiDetailResult;
import com.baidu.mapapi.search.poi.PoiDetailSearchResult;
import com.baidu.mapapi.search.poi.PoiIndoorResult;
import com.baidu.mapapi.search.poi.PoiResult;
import com.baidu.mapapi.search.poi.PoiSearch;
import com.baidu.mapapi.search.route.BikingRouteResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.IndoorRouteResult;
import com.baidu.mapapi.search.route.MassTransitRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRoutePlanOption;
import com.baidu.mapapi.search.route.WalkingRouteResult;

import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.ApplicationExlike;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.activity.BaseActivity;
import com.zjnu.grouptour.activity.DestinationSettingActivity;
import com.zjnu.grouptour.search.KeybordUtil;
import com.zjnu.grouptour.search.PoiListAdapter;
import com.zjnu.grouptour.searchroute.NodeUtils;
import com.zjnu.grouptour.searchroute.RouteLineAdapter;
import com.zjnu.grouptour.searchroute.SelectRouteDialog;
import com.zjnu.grouptour.service.LocationService;
import com.zjnu.grouptour.service.Utils;
import com.zjnu.grouptour.utils.ConstantUtils;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.util.List;

import cn.addapp.pickers.entity.City;
import cn.addapp.pickers.entity.County;
import cn.addapp.pickers.entity.Province;

/**
 * @author luchen
 * @Date 2021/4/10 18:20
 * @Description
 */
public class WalkRouteSearchActivity extends BaseActivity implements BaiduMap.OnMapClickListener,
        OnGetRoutePlanResultListener, OnGetPoiSearchResultListener, OnGetSuggestionResultListener,
        AdapterView.OnItemClickListener, PoiListAdapter.OnGetChildrenLocationListener, SensorEventListener {

    private final static String TAG = WalkRouteSearchActivity.class.getSimpleName();

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
    private Marker mMarker;

    // 路线规划 搜索模块
    private RoutePlanSearch mRouteSearch = null;
    private WalkingRouteResult mWalkingRouteResult = null;
    private boolean hasShowDialog = false;
    private NodeUtils mNodeUtils;
    private EditText mEditStartCity;
    private EditText mEditEndCity;
    private AutoCompleteTextView mStartNodeView;
    private AutoCompleteTextView mEndNodeView;
    // 浏览路线节点相关
    private Button mBtnPre = null; // 上一个节点
    private Button mBtnNext = null; // 下一个节点
    private RouteLine mRouteLine = null;
    private OverlayManager mRouteOverlay = null;
    private boolean mUseDefaultIcon = false;

    //导航
    private BikeNaviLaunchParam bikeParam;
    private WalkNaviLaunchParam walkParam;
    /*导航起终点Marker，可拖动改变起终点的坐标*/
    private Marker mStartMarker;
    private Marker mEndMarker;
    private BitmapDescriptor bdStart = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_start);
    private BitmapDescriptor bdEnd = BitmapDescriptorFactory
            .fromResource(R.drawable.icon_end);

    private static int setSensitivity = LocationClientOption.LOC_SENSITIVITY_HIGHT;
    private static boolean limit = true;
    private static int scope = 2;
    private static boolean isOnResume = false; // 用作 界面出现时，根据用户定位刷新地图
    public static boolean isNaviBack = false; // 是否从导航界面返回
    private static boolean isStartTextChanged = true; // 记录用户的前一次操作，true为刚刚改动了起点，false为刚刚改动了终点
    private static boolean isStartItemClicked = false; // 处理用户在未点击ListView中显示的查询结果的条件下点击搜索按钮，导致路线查询无结果的bug
    private static boolean isEndItemClicked = false;
    private static boolean isRepeatEmpty = false; // 是否重复清空了AutoCompleteTextView中的文本
    private static LatLng startLoc, endLoc; // 保存点击PoiDetailView中某一项的经纬度信息 或是 刚打开界面时用户定位的经纬度信息
    private static boolean isPoiDetailViewClicked = false;  // 防止点击PoiDetailView后 地图重复刷新bug

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_walking_route);
        StatusBarUtils.setStatusBarMode(WalkRouteSearchActivity.this, true, R.color.teal_200);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN); // 为了在软键盘弹出时，不把布局顶上去

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
        tv_locResult = (TextView) findViewById(R.id.tv_locResult);
        tv_locResult.setMovementMethod(ScrollingMovementMethod.getInstance());
        mEditStartCity = (EditText) findViewById(R.id.st_city);
        mEditEndCity = (EditText) findViewById(R.id.ed_city);
        mStartNodeView = (AutoCompleteTextView) findViewById(R.id.st_node);
        mEndNodeView = (AutoCompleteTextView) findViewById(R.id.ed_node);

        mBtnPre = (Button) findViewById(R.id.pre);
        mBtnNext = (Button) findViewById(R.id.next);
//        mBtnPre.setVisibility(View.INVISIBLE);
//        mBtnNext.setVisibility(View.INVISIBLE);

        // 地图点击事件处理
        mBaiduMap.setOnMapClickListener(this);

        // 初始化搜索模块，注册事件监听
        mRouteSearch = RoutePlanSearch.newInstance();
        mRouteSearch.setOnGetRoutePlanResultListener(this);
        mNodeUtils = new NodeUtils(this,  mBaiduMap);

        // 创建poi检索实例，注册搜索事件监听
        mPoiSearch = PoiSearch.newInstance();
        mPoiSearch.setOnGetPoiSearchResultListener(this);
        // 默认起点 天安门，终点 北京大学
        startLoc = new LatLng(39.915071, 116.403907);
        endLoc = new LatLng(39.998877, 116.316833);
        // 展示父子节点控件
        mPoiDetailView = (RelativeLayout) findViewById(R.id.poi_detail);
        mPoiList = (ListView) findViewById(R.id.poi_list);
        mPoiList.setOnItemClickListener(this);

        mEditStartCity.setFocusable(false);
        mEditStartCity.setKeyListener(null);
        mEditStartCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddressPickTask task = new AddressPickTask(WalkRouteSearchActivity.this);
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
                    }
                });
                task.execute("浙江省", "杭州市");
            }
        });

        mStartNodeView.addTextChangedListener(new TextWatcher() {

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
                if(!isOnResume || isNaviBack) {
                    isNaviBack = false;
                    isOnResume = false;
                    //  按搜索按钮时隐藏软键盘，为了在结果回调时计算 PoiDetailView 控件的高度，把地图中poi展示到合理范围内
//                KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);
                    if(editable.toString().equals("") && !isRepeatEmpty) {
                        // TODO: 2021/4/11 显示用户所在区域的推荐搜索
                        Toast.makeText(WalkRouteSearchActivity.this, "您想从哪里出发呢？", Toast.LENGTH_LONG).show();
                        return;
                    }else if(editable.toString().trim().equals("")) {
                        // 重置浏览节点的路线数据
                        mRouteLine = null;
//                        mBtnPre.setVisibility(View.INVISIBLE);
//                        mBtnNext.setVisibility(View.INVISIBLE);
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
                    isStartTextChanged = true;
                }
            }
        });

        mEditEndCity.setFocusable(false);
        mEditEndCity.setKeyListener(null);
        mEditEndCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AddressPickTask task = new AddressPickTask(WalkRouteSearchActivity.this);
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
                        mEditEndCity.setText(cityName);
                    }
                });
                task.execute("浙江省", "杭州市");
            }
        });

        mEndNodeView.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                if(!isOnResume || isNaviBack) {
                    isNaviBack = false;
                    isOnResume = false;
                    //  按搜索按钮时隐藏软键盘，为了在结果回调时计算 PoiDetailView 控件的高度，把地图中poi展示到合理范围内
//                KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);
                    if(editable.toString().equals("") && !isRepeatEmpty) {
                        // TODO: 2021/4/11 显示用户所在区域的推荐搜索
                        Toast.makeText(WalkRouteSearchActivity.this, "您想到哪里去呢？", Toast.LENGTH_LONG).show();
                        return;
                    }else if(editable.toString().trim().equals("")) {
                        // 重置浏览节点的路线数据
                        mRouteLine = null;
//                        mBtnPre.setVisibility(View.INVISIBLE);
//                        mBtnNext.setVisibility(View.INVISIBLE);
                        // 清除之前的覆盖物
                        mBaiduMap.clear();
                        return;
                    }
                    // 获取检索城市
                    String cityStr = mEditEndCity.getText().toString().trim();
                    // 获取检索关键字
                    String keyWordStr = editable.toString().trim();
                    // 发起请求
                    mPoiSearch.searchInCity((new PoiCitySearchOption())
                            .city(cityStr)
                            .keyword(keyWordStr)
                            .pageNum(mLoadIndex) // 分页编号
                            .cityLimit(limit)
                            .scope(scope));
                    isStartTextChanged = false;
                }
            }
        });

        mStartNodeView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(view.isFocused()) {
                    if (mStartNodeView.getText().toString().trim().equals("")) {
                        // TODO: 2021/4/11 显示用户所在区域的推荐搜索
                        Toast.makeText(WalkRouteSearchActivity.this, "您想从哪里出发呢？", Toast.LENGTH_LONG).show();
                        isRepeatEmpty = true;
                    }
                }else if(!isStartItemClicked) {
                    startLoc = mAllPoi.get(0).location; // 用作 起点输入完毕时，更新起点数据
                }
            }
        });

        mEndNodeView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View view, boolean b) {
                if(view.isFocused()) {
                    if (mEndNodeView.getText().toString().trim().equals("")) {
                        // TODO: 2021/4/11 显示用户所在区域的推荐搜索
                        Toast.makeText(WalkRouteSearchActivity.this, "您想到哪里去呢？", Toast.LENGTH_LONG).show();
                        isRepeatEmpty = true;
                    }
                }else if(!isEndItemClicked) {
                    endLoc = mAllPoi.get(0).location; // 用作 终点输入完毕时，更新终点数据
                }
            }
        });

        // -----------location config ------------
        locationService = ((ApplicationExlike) getApplication()).locationService;
        mOption = new LocationClientOption();
        mOption = locationService.getDefaultLocationClientOption();
        // 打开gps
        mOption.setOpenGps(true);
        // 设置坐标类型
        mOption.setCoorType("bd09ll");
        //可选，默认0，即仅定位一次，设置发起连续定位请求的间隔需要大于等于1000ms才是有效的
        mOption.setScanSpan(2000); // 每3秒更新一次用户当前位置
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
                Toast.makeText(WalkRouteSearchActivity.this, "定位模式取值错误！", Toast.LENGTH_LONG).show();
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

        /*骑行导航入口*/
        Button btn_bikeNavi = (Button) findViewById(R.id.btn_bikeNavi);
        btn_bikeNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 重置浏览节点的路线数据
                mRouteLine = null;
//                mBtnPre.setVisibility(View.INVISIBLE);
//                mBtnNext.setVisibility(View.INVISIBLE);
                // 清除之前的覆盖物
//                mBaiduMap.clear();
                if(mStartNodeView.getText().toString().trim().equals("")) {
                    Toast.makeText(WalkRouteSearchActivity.this, "请输入起点信息！", Toast.LENGTH_LONG).show();
                    return;
                }else if(mEndNodeView.getText().toString().trim().equals("")) {
                    Toast.makeText(WalkRouteSearchActivity.this, "请输入终点信息！", Toast.LENGTH_LONG).show();
                    return;
                }
                /**
                 * 更新起终点参数 防止用户在未点击ListView中显示的查询结果的条件下直接点击导航按钮，导致导航信息出错
                 */
                // 起点参数
                if(isStartItemClicked && isStartTextChanged) {
                    isStartItemClicked = false;
                }
                else if(isStartTextChanged) {
                    startLoc = mAllPoi.get(0).location;
                }
                // 终点参数
                if(isEndItemClicked && !isStartTextChanged) {
                    isEndItemClicked = false;
                }
                else if(!isStartTextChanged) {
                    endLoc = mAllPoi.get(0).location;
                }

                showPoiDetailView(false);
                KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);

                /*构造导航起终点参数对象*/
                BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
                bikeStartNode.setLocation(startLoc);
                BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
                bikeEndNode.setLocation(endLoc);
                bikeParam = new BikeNaviLaunchParam().startNodeInfo(bikeStartNode).endNodeInfo(bikeEndNode);

                /* 初始化起终点Marker */
//                initOverlay();

                startBikeNavi();
                // 清除之前的覆盖物
//                mBaiduMap.clear();
                if(getCurrentFocus() != null) {
                    getCurrentFocus().clearFocus();
                }
            }
        });

        /*普通步行导航入口*/
        Button btn_walkNavi = (Button) findViewById(R.id.btn_walkNavi);
        btn_walkNavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 重置浏览节点的路线数据
                mRouteLine = null;
//                mBtnPre.setVisibility(View.INVISIBLE);
//                mBtnNext.setVisibility(View.INVISIBLE);
                // 清除之前的覆盖物
//                mBaiduMap.clear();
                if(mStartNodeView.getText().toString().trim().equals("")) {
                    Toast.makeText(WalkRouteSearchActivity.this, "请输入起点信息！", Toast.LENGTH_LONG).show();
                    return;
                }else if(mEndNodeView.getText().toString().trim().equals("")) {
                    Toast.makeText(WalkRouteSearchActivity.this, "请输入终点信息！", Toast.LENGTH_LONG).show();
                    return;
                }
                /**
                 * 更新起终点参数 防止用户在未点击ListView中显示的查询结果的条件下直接点击导航按钮，导致导航信息出错
                 */
                // 起点参数
                if(isStartItemClicked && isStartTextChanged) {
                    isStartItemClicked = false;
                }
                else if(isStartTextChanged) {
                    startLoc = mAllPoi.get(0).location;
                }
                // 终点参数
                if(isEndItemClicked && !isStartTextChanged) {
                    isEndItemClicked = false;
                }
                else if(!isStartTextChanged) {
                    endLoc = mAllPoi.get(0).location;
                }

                showPoiDetailView(false);
                KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);

                /*构造导航起终点参数对象*/
                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(startLoc);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(endLoc);
                walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);

                /* 初始化起终点Marker */
//                initOverlay();

                walkParam.extraNaviMode(0);
                startWalkNavi();
                // 清除之前的覆盖物
//                mBaiduMap.clear();
                if(getCurrentFocus() != null) {
                    getCurrentFocus().clearFocus();
                }
            }
        });

        /*AR步行导航入口*/
        Button btn_walkNaviAR = (Button) findViewById(R.id.btn_walkNaviAR);
        btn_walkNaviAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(WalkRouteSearchActivity.this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
//                        if(checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
//                            Toast.makeText(WalkRouteSearchActivity.this, "获取相机权限失败！", Toast.LENGTH_LONG).show();
//                        if(!shouldShowRequestPermissionRationale(Manifest.permission.CAMERA))
//                            Toast.makeText(WalkRouteSearchActivity.this, "获取相机权限失败！", Toast.LENGTH_LONG).show();
                    }
                }
                // 重置浏览节点的路线数据
                mRouteLine = null;
//                mBtnPre.setVisibility(View.INVISIBLE);
//                mBtnNext.setVisibility(View.INVISIBLE);
                // 清除之前的覆盖物
//                mBaiduMap.clear();
                if(mStartNodeView.getText().toString().trim().equals("")) {
                    Toast.makeText(WalkRouteSearchActivity.this, "请输入起点信息！", Toast.LENGTH_LONG).show();
                    return;
                }else if(mEndNodeView.getText().toString().trim().equals("")) {
                    Toast.makeText(WalkRouteSearchActivity.this, "请输入终点信息！", Toast.LENGTH_LONG).show();
                    return;
                }
                /**
                 * 更新起终点参数 防止用户在未点击ListView中显示的查询结果的条件下直接点击导航按钮，导致导航信息出错
                 */
                // 起点参数
                if(isStartItemClicked && isStartTextChanged) {
                    isStartItemClicked = false;
                }
                else if(isStartTextChanged) {
                    startLoc = mAllPoi.get(0).location;
                }
                // 终点参数
                if(isEndItemClicked && !isStartTextChanged) {
                    isEndItemClicked = false;
                }
                else if(!isStartTextChanged) {
                    endLoc = mAllPoi.get(0).location;
                }

                showPoiDetailView(false);
                KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);

                /*构造导航起终点参数对象*/
                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(startLoc);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(endLoc);
                walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);

                /* 初始化起终点Marker */
//                initOverlay();

                walkParam.extraNaviMode(1);
                startWalkNavi();
                // 清除之前的覆盖物
//                mBaiduMap.clear();
                if(getCurrentFocus() != null) {
                    getCurrentFocus().clearFocus();
                }
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        //注册监听
//        int type = getIntent().getIntExtra("from", 0);
//        if (type == 0) {
//            locationService.setLocationOption(locationService.getDefaultLocationClientOption());
//        } else if (type == 1) {
//            locationService.start();
//        }
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
        if(mStartMarker != null && mEndMarker != null) {
            mStartMarker = null;
            mEndMarker = null;
            // 清除之前的覆盖物
            mBaiduMap.clear();
        }
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
        // 取消注册传感器监听
        mSensorManager.unregisterListener(this);
        // 释放bitmap
        mBitmap.recycle();
        // 释放检索对象
        if (mRouteSearch != null) {
            mRouteSearch.destroy();
        }
//        // 退出时销毁定位
//        mLocClient.stop();
        // 关闭定位图层
        mBaiduMap.setMyLocationEnabled(false);
        // 清空地图所有的覆盖物
        mBaiduMap.clear();
        // 释放地图组件
        mMapView.onDestroy();
        bdStart.recycle();
        bdEnd.recycle();
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
                    mEditStartCity.setText(location.getCity().substring(0, location.getCity().lastIndexOf("市")));
                    mEditEndCity.setText(location.getCity().substring(0, location.getCity().lastIndexOf("市")));
                    if (location.getPoiList() != null && !location.getPoiList().isEmpty())
                        mStartNodeView.setText(location.getPoiList().get(0).getName());
                    mEndNodeView.setText("");

                    mBaiduMap.clear();
                    showPoiDetailView(false);
                    // 用户打开界面时的位置
                    LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                    MapStatus.Builder builder = new MapStatus.Builder();
                    builder.target(latLng);
                    builder.zoom(19.0f); // 调节地图比例尺
                    mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
                    startLoc = new LatLng(location.getLatitude(), location.getLongitude());

//                    // 默认 11级
//                    float zoom = 11.0f;
//                    // 该Intent是OfflineDemo中查看离线地图调起的
//                    Intent intent = getIntent();
//                    if (null != intent) {
//                        latLng = new LatLng(intent.getDoubleExtra("y", 39.915071),
//                                intent.getDoubleExtra("x", 116.403907));
//                        zoom = intent.getFloatExtra("level", 11.0f);
//                    }
//
//                    builder.target(latLng).zoom(zoom);
//                    MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newMapStatus(builder.build());
//
//                    // 设置地图状态
//                    mBaiduMap.setMapStatus(mapStatusUpdate);

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
            Toast.makeText(WalkRouteSearchActivity.this, "开启定位图层", Toast.LENGTH_LONG).show();
        }else{
            mBaiduMap.setMyLocationEnabled(true);
            mBaiduMap.setMyLocationData(myLocationData);
            Toast.makeText(WalkRouteSearchActivity.this, "关闭定位图层", Toast.LENGTH_LONG).show();
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
        KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);
        String cityStr, keyWordStr;
        if(isStartTextChanged) {
            // 获取检索城市
            cityStr = mEditStartCity.getText().toString();
            // 获取检索关键字
            keyWordStr = mStartNodeView.getText().toString();
        }else {
            // 获取检索城市
            cityStr = mEditEndCity.getText().toString();
            // 获取检索关键字
            keyWordStr = mEndNodeView.getText().toString();
        }
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
            Toast.makeText(WalkRouteSearchActivity.this, "未找到结果", Toast.LENGTH_LONG).show();
            return;
        }

        if (result.error == SearchResult.ERRORNO.NO_ERROR) {
            showPoiDetailView(true);
            // 注释原因：取消每次afterTextChanged后都进行的刷新操作
//            mBaiduMap.clear();
//            // 监听 View 绘制完成后获取view的高度
//            mPoiDetailView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
//                @Override
//                public void onGlobalLayout() {
//                    int padding = 50;
//                    // 添加poi
//                    PoiOverlay overlay = new WalkRouteSearchActivity.MyPoiOverlay(mBaiduMap);
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

            // 获取poi结果
            mAllPoi = result.getAllPoi();
            PoiListAdapter poiListAdapter = new PoiListAdapter(this, mAllPoi);
            poiListAdapter.setOnGetChildrenLocationListener(this);
            // 把poi结果添加到适配器
            mPoiList.setAdapter(poiListAdapter);

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
            Toast.makeText(WalkRouteSearchActivity.this, strInfo, Toast.LENGTH_LONG).show();
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
        KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);
        addPoiLoction(poiInfo.getLocation());
        if(isStartTextChanged) {
            mStartNodeView.setText(poiInfo.name);
            startLoc = poiInfo.location;
            isStartItemClicked = true;
        }else {
            mEndNodeView.setText(poiInfo.name);
            endLoc = poiInfo.location;
            isEndItemClicked = true;
        }
    }

    /**
     * 点击子节点list 获取经纬添加poi更新地图
     *
     * @param childrenLocation 子节点经纬度
     */
    @Override
    public void getChildrenLocation(LatLng childrenLocation) {

        addPoiLoction(childrenLocation);
    }


    /**
     * 更新到子节点的位置
     *
     * @param latLng 子节点经纬度
     */
    private void addPoiLoction(LatLng latLng) {
        mBaiduMap.clear();
        showPoiDetailView(false);
        OverlayOptions markerOptions = new MarkerOptions().position(latLng).icon(mBitmap);
        mBaiduMap.addOverlay(markerOptions);
        MapStatus.Builder builder = new MapStatus.Builder();
        builder.target(latLng);
        builder.zoom(18.0f);
        mBaiduMap.setMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));
        if(isStartTextChanged)
            mStartNodeView.clearFocus();
        else
            mEndNodeView.clearFocus();
    }


    protected class MyPoiOverlay extends PoiOverlay {
        MyPoiOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public boolean onPoiClick(int index) {
            super.onPoiClick(index);
            PoiInfo poi = getPoiResult().getAllPoi().get(index);
            Toast.makeText(WalkRouteSearchActivity.this,poi.address,Toast.LENGTH_LONG).show();
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

    // DESCRIPTION: 2021/4/11 以下方法与 路线规划 相关
    /**
     * 发起路线规划搜索示例
     */
    public void searchButtonProcess(View v) {
        // 重置浏览节点的路线数据
        mRouteLine = null;
//        mBtnPre.setVisibility(View.INVISIBLE);
//        mBtnNext.setVisibility(View.INVISIBLE);
        // 清除之前的覆盖物
        mBaiduMap.clear();
        if(mStartNodeView.getText().toString().trim().equals("")) {
            Toast.makeText(WalkRouteSearchActivity.this, "请输入起点信息！", Toast.LENGTH_LONG).show();
            return;
        }else if(mEndNodeView.getText().toString().trim().equals("")) {
            Toast.makeText(WalkRouteSearchActivity.this, "请输入终点信息！", Toast.LENGTH_LONG).show();
            return;
        }

        PlanNode startNode, endNode;
        startNode = PlanNode.withLocation(startLoc);
        endNode = PlanNode.withLocation(endLoc);
        // 起点参数
//        PlanNode startNode = PlanNode.withCityNameAndPlaceName(mEditStartCity.getText().toString().trim(),
//                mStartNodeView.getText().toString().trim());
        if(isStartItemClicked && isStartTextChanged) {
            startNode = PlanNode.withLocation(startLoc);
            isStartItemClicked = false;
        }
        else if(isStartTextChanged) {
            startLoc = mAllPoi.get(0).location;
            startNode = PlanNode.withLocation(startLoc);
        }
        // 终点参数
//        PlanNode endNode = PlanNode.withCityNameAndPlaceName(mEditEndCity.getText().toString().trim(),
//                mEndNodeView.getText().toString().trim());
        if(isEndItemClicked && !isStartTextChanged) {
            endNode = PlanNode.withLocation(endLoc);
            isEndItemClicked = false;
        }
        else if(!isStartTextChanged) {
            endLoc = mAllPoi.get(0).location;
            endNode = PlanNode.withLocation(endLoc);
        }

        // 实际使用中请对起点终点城市进行正确的设定
        mRouteSearch.walkingSearch((new WalkingRoutePlanOption())
                .from(startNode) // 起点
                .to(endNode)); // 终点

        showPoiDetailView(false);
        KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);
    }

    /**
     * 节点浏览示例
     */
    public void nodeClick(View view) {
        if (null != mRouteLine) {
            mNodeUtils.browseRoutNode(view,mRouteLine);
        }
    }

    /**
     * 切换路线图标，刷新地图使其生效
     * 注意： 起终点图标使用中心对齐.
     */
    public void changeRouteIcon(View v) {
        if (mRouteOverlay == null) {
            return;
        }
        if (mUseDefaultIcon) {
            ((Button) v).setText("自定义起终点图标");
            Toast.makeText(this, "将使用系统起终点图标", Toast.LENGTH_SHORT).show();
        } else {
            ((Button) v).setText("系统起终点图标");
            Toast.makeText(this, "将使用自定义起终点图标", Toast.LENGTH_SHORT).show();
        }
        mUseDefaultIcon = !mUseDefaultIcon;
        mRouteOverlay.removeFromMap();
        mRouteOverlay.addToMap();
    }


    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
    }

    /**
     * 步行路线结果回调
     *
     * @param result  步行路线结果
     */
    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult result) {
        if (null == result) {
            return;
        }

        if (result.error == SearchResult.ERRORNO.AMBIGUOUS_ROURE_ADDR) {
            // 起终点或途经点地址有岐义，通过以下接口获取建议查询信息
            // result.getSuggestAddrInfo()
            AlertDialog.Builder builder = new AlertDialog.Builder(WalkRouteSearchActivity.this);
            builder.setTitle("提示");
            builder.setMessage("检索地址有歧义，请重新设置。\n可通过getSuggestAddrInfo()接口获得建议查询信息");
            builder.setPositiveButton("确认", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.create().show();
            return;
        }

        if (result.error != SearchResult.ERRORNO.NO_ERROR) {
            Toast.makeText(WalkRouteSearchActivity.this, "抱歉，未找到结果", Toast.LENGTH_SHORT).show();
        } else {
//            mBtnPre.setVisibility(View.VISIBLE);
//            mBtnNext.setVisibility(View.VISIBLE);

            if (result.getRouteLines().size() > 1) {
                mWalkingRouteResult = result;
                if (!hasShowDialog) {
                    SelectRouteDialog selectRouteDialog = new SelectRouteDialog(WalkRouteSearchActivity.this,
                            result.getRouteLines(), RouteLineAdapter.Type.WALKING_ROUTE);
                    selectRouteDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            hasShowDialog = false;
                        }
                    });
                    selectRouteDialog.setOnItemInDlgClickLinster(new SelectRouteDialog.OnItemInDlgClickListener() {
                        public void onItemClick(int position) {
                            mRouteLine = mWalkingRouteResult.getRouteLines().get(position);
                            WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                            mBaiduMap.setOnMarkerClickListener(overlay);
                            mRouteOverlay = overlay;
                            overlay.setData(mWalkingRouteResult.getRouteLines().get(position));
                            overlay.addToMap();
                            overlay.zoomToSpan();
                        }

                    });
                    selectRouteDialog.show();
                    hasShowDialog = true;
                }
            } else if (result.getRouteLines().size() == 1) {
                // 直接显示
                mRouteLine = result.getRouteLines().get(0);
                WalkingRouteOverlay overlay = new MyWalkingRouteOverlay(mBaiduMap);
                mBaiduMap.setOnMarkerClickListener(overlay);
                mRouteOverlay = overlay;
                overlay.setData(result.getRouteLines().get(0));
                overlay.addToMap();
                overlay.zoomToSpan();

            } else {
                Log.d("route result", "结果数<0");
            }
        }
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult result) {
    }

    @Override
    public void onGetMassTransitRouteResult(MassTransitRouteResult result) {
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult result) {
    }

    @Override
    public void onGetIndoorRouteResult(IndoorRouteResult indoorRouteResult) {

    }

    @Override
    public void onGetBikingRouteResult(BikingRouteResult result) {

    }

    private class MyWalkingRouteOverlay extends WalkingRouteOverlay {

        private MyWalkingRouteOverlay(BaiduMap baiduMap) {
            super(baiduMap);
        }

        @Override
        public BitmapDescriptor getStartMarker() {
            if (mUseDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_st);
            }
            return null;
        }

        @Override
        public BitmapDescriptor getTerminalMarker() {
            if (mUseDefaultIcon) {
                return BitmapDescriptorFactory.fromResource(R.drawable.icon_en);
            }
            return null;
        }
    }

    @Override
    public void onMapClick(LatLng point) {
        mBaiduMap.hideInfoWindow();
        showPoiDetailView(false);
        KeybordUtil.closeKeybord(WalkRouteSearchActivity.this);
        getCurrentFocus().clearFocus();
    }

    @Override
    public void onMapPoiClick(MapPoi poi) {

    }

    // DESCRIPTION: 2021/4/11 以下方法与 导航 相关
    /**
     * 初始化导航起终点Marker
     */
    public void initOverlay() {

        MarkerOptions ooA = new MarkerOptions().position(startLoc).icon(bdStart)
                .zIndex(9).draggable(true);

        mStartMarker = (Marker) (mBaiduMap.addOverlay(ooA));
        mStartMarker.setDraggable(true);
        MarkerOptions ooB = new MarkerOptions().position(endLoc).icon(bdEnd)
                .zIndex(5);
        mEndMarker = (Marker) (mBaiduMap.addOverlay(ooB));
        mEndMarker.setDraggable(true);

        mBaiduMap.setOnMarkerDragListener(new BaiduMap.OnMarkerDragListener() {
            public void onMarkerDrag(Marker marker) {
            }

            public void onMarkerDragEnd(Marker marker) {
                if(marker == mStartMarker){
                    startLoc = marker.getPosition();
                }else if(marker == mEndMarker){
                    endLoc = marker.getPosition();
                }

                BikeRouteNodeInfo bikeStartNode = new BikeRouteNodeInfo();
                bikeStartNode.setLocation(startLoc);
                BikeRouteNodeInfo bikeEndNode = new BikeRouteNodeInfo();
                bikeEndNode.setLocation(endLoc);
                bikeParam = new BikeNaviLaunchParam().startNodeInfo(bikeStartNode).endNodeInfo(bikeEndNode);

                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(startLoc);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(endLoc);
                walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);

            }

            public void onMarkerDragStart(Marker marker) {
            }
        });
    }

    /**
     * 开始骑行导航
     */
    private void startBikeNavi() {
        Log.d(TAG, "startBikeNavi");
        try {
            BikeNavigateHelper.getInstance().initNaviEngine(this, new IBEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d(TAG, "BikeNavi engineInitSuccess");
                    routePlanWithBikeParam();
                }

                @Override
                public void engineInitFail() {
                    Log.d(TAG, "BikeNavi engineInitFail");
                    BikeNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "startBikeNavi Exception");
            e.printStackTrace();
        }
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
     * 发起骑行导航算路
     */
    private void routePlanWithBikeParam() {
        BikeNavigateHelper.getInstance().routePlanWithRouteNode(bikeParam, new IBRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d(TAG, "BikeNavi onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {
                Log.d(TAG, "BikeNavi onRoutePlanSuccess");
                Intent intent = new Intent();
                intent.setClass(WalkRouteSearchActivity.this, BikeNaviGuideActivity.class);
                startActivity(intent);
            }

            @Override
            public void onRoutePlanFail(BikeRoutePlanError error) {
                Log.d(TAG, "BikeNavi onRoutePlanFail");
            }

        });
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

                Intent intent = new Intent();
                intent.setClass(WalkRouteSearchActivity.this, WalkNaviGuideActivity.class);
                startActivity(intent);

            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                Log.d(TAG, "WalkNavi onRoutePlanFail");
                Toast.makeText(WalkRouteSearchActivity.this, error.name(), Toast.LENGTH_LONG).show();
            }

        });
    }
}
