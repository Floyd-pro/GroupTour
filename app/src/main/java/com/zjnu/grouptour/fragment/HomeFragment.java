package com.zjnu.grouptour.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.location.BDAbstractLocationListener;
import com.baidu.location.BDLocation;
import com.baidu.location.LocationClient;
import com.baidu.location.LocationClientOption;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.geocode.GeoCodeResult;
import com.baidu.mapapi.search.geocode.GeoCoder;
import com.baidu.mapapi.search.geocode.OnGetGeoCoderResultListener;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeOption;
import com.baidu.mapapi.search.geocode.ReverseGeoCodeResult;
import com.baidu.mapapi.search.weather.LanguageType;
import com.baidu.mapapi.search.weather.OnGetWeatherResultListener;
import com.baidu.mapapi.search.weather.WeatherDataType;
import com.baidu.mapapi.search.weather.WeatherResult;
import com.baidu.mapapi.search.weather.WeatherSearch;
import com.baidu.mapapi.search.weather.WeatherSearchForecasts;
import com.baidu.mapapi.search.weather.WeatherSearchOption;
import com.baidu.mapapi.search.weather.WeatherServerType;
import com.baidu.mapapi.utils.DistanceUtil;
import com.hyphenate.EMCallBack;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.common.widget.ArrowItemView;
import com.hyphenate.easeim.section.base.WebViewActivity;
import com.hyphenate.easeim.section.dialog.DemoDialogFragment;
import com.hyphenate.easeim.section.dialog.SimpleDialogFragment;
import com.hyphenate.easeim.section.login.activity.LoginActivity;
import com.hyphenate.easeim.section.me.activity.AboutHxActivity;
import com.hyphenate.easeim.section.me.activity.DeveloperSetActivity;
import com.hyphenate.easeim.section.me.activity.FeedbackActivity;
import com.hyphenate.easeim.section.me.activity.SetIndexActivity;
import com.youth.banner.Banner;
import com.youth.banner.config.IndicatorConfig;
import com.youth.banner.indicator.RectangleIndicator;
import com.youth.banner.listener.OnBannerListener;
import com.youth.banner.transformer.AlphaPageTransformer;
import com.youth.banner.util.BannerUtils;
import com.zjnu.grouptour.activity.UserDetailActivity;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.zjnu.grouptour.R;
import com.hyphenate.easeim.section.base.BaseInitFragment;
import com.zjnu.grouptour.TestMainActivity;
import com.zjnu.grouptour.activity.baiduMap.LocMapActivity;
import com.zjnu.grouptour.adapter.ImageTitleAdapter;
import com.zjnu.grouptour.adapter.WeatherForcastRecycleAdapter;
import com.zjnu.grouptour.api.GroupTourApi;
import com.zjnu.grouptour.bean.BannerDataBean;
import com.zjnu.grouptour.bean.GroupmateInfo;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.bean.Team;
import com.zjnu.grouptour.bean.WeatherInfo;
import com.zjnu.grouptour.utils.DBUtil;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author luchen
 * @Date 2021/4/27 11:00
 * @Description 首页Fragment
 */

public class HomeFragment extends BaseInitFragment implements View.OnClickListener, OnBannerListener {
    private ConstraintLayout clUser;
    private TextView name;
    private ArrowItemView itemCommonSet;
    private ArrowItemView itemFeedback;
    private ArrowItemView itemAboutHx;
    private ArrowItemView itemDeveloperSet;
    private ArrowItemView itemBaiduMap;
    private Button mBtnLogout;

    private Banner banner;
    private ConstraintLayout clMyDestination;
    private TextView tvDestinationName;
    private TextView tvTeammateNum;
    private TextView tvDistance;

    private Handler mHandler;
    ExecutorService executorService;
    Future future;
    private Person me;
    private Team myTeam;
    private LatLng destinationLoc;
    private ArrayList<GroupmateInfo> groupmates;

    public LocationClient mLocationClient = null;
    private MyLocationListener myListener = new MyLocationListener();

    // 目的地天气预报
    private WeatherSearch mWeatherSearch;
    private GeoCoder mGeoCoder;
    private String mDistrict;
    private RecyclerView weatherRecyclerView;
    private WeatherForcastRecycleAdapter weatherAdapter;
    private TextView tv_forcast;

    // 当前所在地天气预报
    private WeatherSearch mCurrentWeatherSearch;
    private GeoCoder mCurrentGeoCoder;
    private String mCurrentDistrict;
    private RecyclerView currentWeatherRecyclerView;
    private WeatherForcastRecycleAdapter currentWeatherAdapter;
    private TextView tv_localForcast;

    @Override
    protected int getLayoutId() {
        return R.layout.fragment_home;
    }
    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        clUser = findViewById(R.id.cl_user);
        name = findViewById(R.id.name);
        itemCommonSet = findViewById(R.id.item_common_set);
        itemFeedback = findViewById(R.id.item_feedback);
        itemAboutHx = findViewById(R.id.item_about_hx);
        itemDeveloperSet = findViewById(R.id.item_developer_set);
        itemBaiduMap = findViewById(R.id.item_baidu_map);
        mBtnLogout = findViewById(R.id.btn_logout);
        clMyDestination = findViewById(R.id.cl_myDestination);
        tvDestinationName = findViewById(R.id.tv_destinationName);
        tvTeammateNum = findViewById(R.id.tv_teammate_num);
        tvDistance = findViewById(R.id.tv_distance);
        tv_forcast = findViewById(R.id.tv_forcast);
        tv_localForcast = findViewById(R.id.tv_localForcast);
        weatherRecyclerView = findViewById(R.id.rv_weather);
        currentWeatherRecyclerView = findViewById(R.id.rv_localWeather);

        name.setText(DemoHelper.getInstance().getCurrentUser());

        banner = findViewById(R.id.banner);
        banner.setAdapter(new ImageTitleAdapter(BannerDataBean.getTestData()));
        banner.setIndicator(new RectangleIndicator(mContext));
        banner.setBannerGalleryEffect(18, 5); // 画廊效果
        banner.addPageTransformer(new AlphaPageTransformer()); // 画廊附加透明效果
        banner.setIndicatorGravity(IndicatorConfig.Direction.RIGHT);
        banner.setIndicatorSelectedWidth((int) BannerUtils.dp2px(12));
        banner.setIndicatorSpace((int) BannerUtils.dp2px(4));
        banner.setIndicatorRadius(0);
//        banner.setIndicatorMargins(new IndicatorConfig.Margins(0, 0,
//                BannerConfig.INDICATOR_MARGIN, (int) BannerUtils.dp2px(12)));
        banner.addBannerLifecycleObserver(this);
    }

    @Override
    protected void initListener() {
        super.initListener();
        mBtnLogout.setOnClickListener(this);
        clUser.setOnClickListener(this);
        itemCommonSet.setOnClickListener(this);
        itemFeedback.setOnClickListener(this);
        itemAboutHx.setOnClickListener(this);
        itemDeveloperSet.setOnClickListener(this);
        itemBaiduMap.setOnClickListener(this);

        banner.setOnBannerListener(this);
        clMyDestination.setOnClickListener(this);

        mLocationClient = new LocationClient(mContext);
        mLocationClient.registerLocationListener(myListener);

        LocationClientOption option = new LocationClientOption();
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);
        option.setCoorType("bd09ll");
        option.setScanSpan(1000);
        option.setOpenGps(true);
        option.setLocationNotify(true);
        option.setIgnoreKillProcess(false);
        option.SetIgnoreCacheException(false);
        option.setWifiCacheTimeOut(5*60*1000);
        option.setEnableSimulateGps(false);
        option.setNeedNewVersionRgc(true);
        mLocationClient.setLocOption(option);

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
                // 已经获取到编码，通知mHandler可以请求天气预报数据了
                mHandler.sendEmptyMessage(2);
            }
        });

        mCurrentWeatherSearch = WeatherSearch.newInstance();
        mCurrentGeoCoder = GeoCoder.newInstance();
        mCurrentGeoCoder.setOnGetGeoCodeResultListener(new OnGetGeoCoderResultListener(){
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
                mCurrentDistrict = String.valueOf(adCode);
                // 已经获取到编码，通知mHandler可以请求天气预报数据了
                mHandler.sendEmptyMessage(3);
            }
        });
    }

    // 请求 目的地 未来5天天气预报数据
    public void requestWeather() {
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

                        if (null == weatherResult) {
                            ToastUtils.showToast("没有搜索到目的地天气！");
                            return;
                        }
//                        WeatherSearchRealTime weatherSearchRealTime = weatherResult.getRealTimeWeather();
//                        if (null == weatherSearchRealTime) {
//                            return;
//                        }

                        List<WeatherSearchForecasts> list = weatherResult.getForecasts();
                        ArrayList<WeatherInfo> weatherList = new ArrayList<>();

                        if(list != null) {
                            for(WeatherSearchForecasts f : list) {
                                // TODO: 2021/4/24 把天气信息传到adapter中
                                WeatherInfo w = new WeatherInfo();
                                int month = Integer.parseInt(f.getDate().substring(f.getDate().indexOf("-")+1, f.getDate().lastIndexOf("-")));
                                int day = Integer.parseInt(f.getDate().substring(f.getDate().lastIndexOf("-")+1, f.getDate().length()));
                                w.setDate(month + "月" + day + "日");
                                w.setDayOfWeek(f.getWeek());
                                w.setMinTemp(f.getLowestTemp());
                                w.setMaxTemp(f.getHighestTemp());
                                w.setWeatherPhenomenon(f.getPhenomenonDay());
                                w.setWind(f.getWindDirectionDay() + " " + f.getWindPowerDay());

                                if(f.getPhenomenonDay().contains("雨"))
                                    w.setImgWeather(getResources().getDrawable(R.drawable.rainy));
                                if(f.getPhenomenonDay().contains("雪"))
                                    w.setImgWeather(getResources().getDrawable(R.drawable.snowy));
                                if(f.getPhenomenonDay().contains("雾") || f.getPhenomenonDay().contains("霾"))
                                    w.setImgWeather(getResources().getDrawable(R.drawable.fog));
                                if(f.getPhenomenonDay().contains("沙") || f.getPhenomenonDay().contains("尘") || f.getPhenomenonDay().contains("卷"))
                                    w.setImgWeather(getResources().getDrawable(R.drawable.tornado));
                                switch (f.getPhenomenonDay()) {
                                    case "晴":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.sun));
                                        break;
                                    case "多云":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.cloudy));
                                        break;
                                    case "阴":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.overcast));
                                        break;
                                    case "雷阵雨":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.rain_storm));
                                        break;
                                    case "雨夹雪":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.sleet));
                                        break;
                                }
                                weatherList.add(w);
                            }
                            weatherAdapter = new WeatherForcastRecycleAdapter(mContext, weatherList);
                            LinearLayoutManager manager = new LinearLayoutManager(mContext);
                            manager.setOrientation(LinearLayoutManager.HORIZONTAL);
                            weatherRecyclerView.setLayoutManager(manager);
                            weatherRecyclerView.setAdapter(weatherAdapter);
                        }

                    }
                });
            }
        });
        mWeatherSearch.request(weatherSearchOption);
    }

    // 请求 目的地 未来5天天气预报数据
    public void requestCurrentWeather() {
        String districtId = mCurrentDistrict;
        WeatherSearchOption weatherSearchOption = new WeatherSearchOption();
        weatherSearchOption
                .weatherDataType(WeatherDataType.WEATHER_DATA_TYPE_ALL)
                .districtID(districtId)
                .languageType(LanguageType.LanguageTypeChinese)
                .serverType(WeatherServerType.WEATHER_SERVER_TYPE_DEFAULT);
        mCurrentWeatherSearch.setWeatherSearchResultListener(new OnGetWeatherResultListener() {
            @Override
            public void onGetWeatherResultListener(final WeatherResult weatherResult) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        if (null == weatherResult) {
                            ToastUtils.showToast("没有搜索当前所在地天气！");
                            return;
                        }
//                        WeatherSearchRealTime weatherSearchRealTime = weatherResult.getRealTimeWeather();
//                        if (null == weatherSearchRealTime) {
//                            return;
//                        }

                        List<WeatherSearchForecasts> list = weatherResult.getForecasts();
                        ArrayList<WeatherInfo> weatherList = new ArrayList<>();

                        if(list != null) {
                            for(WeatherSearchForecasts f : list) {
                                // TODO: 2021/4/24 把天气信息传到adapter中
                                WeatherInfo w = new WeatherInfo();
                                int month = Integer.parseInt(f.getDate().substring(f.getDate().indexOf("-")+1, f.getDate().lastIndexOf("-")));
                                int day = Integer.parseInt(f.getDate().substring(f.getDate().lastIndexOf("-")+1, f.getDate().length()));
                                w.setDate(month + "月" + day + "日");
                                w.setDayOfWeek(f.getWeek());
                                w.setMinTemp(f.getLowestTemp());
                                w.setMaxTemp(f.getHighestTemp());
                                w.setWeatherPhenomenon(f.getPhenomenonDay());
                                w.setWind(f.getWindDirectionDay() + " " + f.getWindPowerDay());

                                if(f.getPhenomenonDay().contains("雨"))
                                    w.setImgWeather(getResources().getDrawable(R.drawable.rainy));
                                if(f.getPhenomenonDay().contains("雪"))
                                    w.setImgWeather(getResources().getDrawable(R.drawable.snowy));
                                if(f.getPhenomenonDay().contains("雾") || f.getPhenomenonDay().contains("霾"))
                                    w.setImgWeather(getResources().getDrawable(R.drawable.fog));
                                if(f.getPhenomenonDay().contains("沙") || f.getPhenomenonDay().contains("尘") || f.getPhenomenonDay().contains("卷"))
                                    w.setImgWeather(getResources().getDrawable(R.drawable.tornado));
                                switch (f.getPhenomenonDay()) {
                                    case "晴":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.sun));
                                        break;
                                    case "多云":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.cloudy));
                                        break;
                                    case "阴":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.overcast));
                                        break;
                                    case "雷阵雨":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.rain_storm));
                                        break;
                                    case "雨夹雪":
                                        w.setImgWeather(getResources().getDrawable(R.drawable.sleet));
                                        break;
                                }
                                weatherList.add(w);
                            }
                            currentWeatherAdapter = new WeatherForcastRecycleAdapter(mContext, weatherList);
                            LinearLayoutManager manager = new LinearLayoutManager(mContext);
                            manager.setOrientation(LinearLayoutManager.HORIZONTAL);
                            currentWeatherRecyclerView.setLayoutManager(manager);
                            currentWeatherRecyclerView.setAdapter(currentWeatherAdapter);
                        }

                    }
                });
            }
        });
        mCurrentWeatherSearch.request(weatherSearchOption);
    }

    @Override
    public void onResume() {
        super.onResume();
        mHandler = new MyHandler();
        mHandler.sendEmptyMessage(1);
        mLocationClient.start();
    }

    @Override
    public void onStop() {
        super.onStop();
        mHandler = null;
        mLocationClient.stop();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_logout :
                logout();
                break;
            case R.id.cl_user:
                UserDetailActivity.actionStart(mContext);
                break;
            case R.id.item_common_set:
                SetIndexActivity.actionStart(mContext);
                break;
            case R.id.item_feedback:
                FeedbackActivity.actionStart(mContext);
                break;
            case R.id.item_about_hx:
                AboutHxActivity.actionStart(mContext);
                break;
            case R.id.item_developer_set:
                DeveloperSetActivity.actionStart(mContext);
                break;
            case R.id.item_baidu_map:
                TestMainActivity.actionStart(mContext);
                break;
            case R.id.cl_myDestination:
                LocMapActivity.actionStart(mContext, me, myTeam, groupmates);
                break;
        }
    }

    @Override
    public void OnBannerClick(Object data, int position) {
        //    WebViewActivity.actionStart(mContext, getString(R.string.em_register_service_agreement_url)); 轮播图单击事件处理
        switch (position) {
            case 0:
                WebViewActivity.actionStart(mContext, "https://baike.baidu.com/item/%E8%A5%BF%E6%B9%96", false);
                break;
            case 1:
                WebViewActivity.actionStart(mContext, "https://baike.baidu.com/item/%E5%BC%A0%E5%AE%B6%E7%95%8C", false);
                break;
            case 2:
                WebViewActivity.actionStart(mContext, "https://baike.baidu.com/item/%E7%8B%AE%E5%AD%90%E6%9E%97", false);
                break;
            case 3:
                WebViewActivity.actionStart(mContext, "https://baike.baidu.com/item/%E8%AF%BA%E6%97%A5%E6%9C%97%E7%80%91%E5%B8%83", false);
                break;
            case 4:
                WebViewActivity.actionStart(mContext, "https://baike.baidu.com/item/%E5%86%85%E8%92%99%E5%8F%A4%E8%8D%89%E5%8E%9F", false);
                break;
            case 5:
                WebViewActivity.actionStart(mContext, "https://baike.baidu.com/item/%E9%BB%84%E5%B1%B1%E4%BA%91%E6%B5%B7", false);
                break;
        }
    }

    public class MyLocationListener extends BDAbstractLocationListener {
        @Override
        public void onReceiveLocation(BDLocation location){

            double dis = DistanceUtil.getDistance(destinationLoc, new LatLng(location.getLatitude(), location.getLongitude()));
            tvDistance.setText("距离" + String.format("%.1f", dis/1000) + "km");
        }
    }

    private class MyHandler extends Handler {
        // TODO: 2021/4/17 虚引用 处理消息 待重写
        WeakReference<HomeFragment> weakReference;

        public MyHandler() {

        }

        public MyHandler(HomeFragment activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            getInfo();
                        }
                    });
                    break;
                case 2: // 收到 地理编码已获得 的消息，开始请求 目的地的天气数据
                    requestWeather();
                    break;
                case 3: // 收到 地理编码已获得 的消息，开始请求 当前所在地的天气数据
                    requestCurrentWeather();
                default:
//                    ToastUtils.showToast("HomeFragment: 此消息未进行处理");
                    break;
            }
        }
    }

    // 获取用户、队伍及队伍成员的信息
    private void getInfo() {
        executorService = Executors.newSingleThreadExecutor();
        if(getMyInfo()) {
            if(getTeamInfo()) {
                if(getGroupmatesInfo()) {
                    if(myTeam.getDestinationName() != null) {
                        tvDestinationName.setText(myTeam.getDestinationName());
                        tvDestinationName.setTextColor(getResources().getColor(R.color.black));
                        tv_forcast.setText("天气情况一览-" + myTeam.getDestinationName());
                    }
                    if(myTeam.getDestinationLatitude() != 0 && myTeam.getDestinationLongitude() != 0) {
                        destinationLoc = new LatLng(myTeam.getDestinationLatitude(), myTeam.getDestinationLongitude());
                    }
                    if(groupmates != null)
                        tvTeammateNum.setText("附近有" + (groupmates.size() - 1) + "个队友");
                    if(GroupTourApi.getInstance().isOpenLogger())
                        ToastUtils.showToast("个人、队伍及队员信息获取成功！");

                    // 根据目的地经纬度坐标，去请求获取行政区划编码
                    ReverseGeoCodeOption rgcOption =
                            new ReverseGeoCodeOption().location(destinationLoc).radius(500);
                    mGeoCoder.reverseGeoCode(rgcOption);

                    // 根据用户当前所在地经纬度坐标，去请求获取行政区划编码
                    ReverseGeoCodeOption rgcCurrentOption =
                            new ReverseGeoCodeOption().location(new LatLng(me.getLatitude(), me.getLongitude())).radius(500);
                    mCurrentGeoCoder.reverseGeoCode(rgcCurrentOption);
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
        future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Person p = DBUtil.selectPerson(DemoHelper.getInstance().getCurrentUser());
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
        return false;
    }


    // 获取当前用户所在队伍的信息
    private boolean getTeamInfo() {
        future = executorService.submit(new Callable() {
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
        future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                ArrayList<GroupmateInfo> mates = DBUtil.selectGroupmates(me.getTeamID());
                return mates;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新队伍成员信息
                groupmates = (ArrayList<GroupmateInfo>) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void logout() {
        new SimpleDialogFragment.Builder(mContext)
                .setTitle(R.string.em_login_out_hint)
                .showCancelButton(true)
                .setOnConfirmClickListener(R.string.em_dialog_btn_confirm, new DemoDialogFragment.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        DemoHelper.getInstance().logout(true, new EMCallBack() {
                            @Override
                            public void onSuccess() {
                                LoginActivity.startAction(mContext);
                                mContext.finish();
                            }

                            @Override
                            public void onError(int code, String error) {
                                EaseThreadManager.getInstance().runOnMainThread(()-> showToast(error));
                            }

                            @Override
                            public void onProgress(int progress, String status) {

                            }
                        });
                    }
                })
                .show();
    }
}
