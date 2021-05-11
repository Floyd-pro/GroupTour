package com.zjnu.grouptour;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.baidu.mapapi.SDKInitializer;
import com.zjnu.grouptour.activity.BaseActivity;
import com.zjnu.grouptour.activity.baiduMap.LocMapActivity;
import com.zjnu.grouptour.activity.baiduMap.LocationActivity;
import com.zjnu.grouptour.activity.baiduMap.TestMapActivity;
import com.zjnu.grouptour.activity.baiduMap.WalkRouteSearchActivity;
import com.zjnu.grouptour.bean.PlusBean;
import com.zjnu.grouptour.dialog.PlusBottomSheetDialog;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

public class TestMainActivity extends BaseActivity implements View.OnClickListener {

    //地图Demo内的变量
    private static final String TAG = TestMainActivity.class.getSimpleName();
    private SDKReceiver mReceiver;

    //定位Demo内的变量
    private final int SDK_PERMISSION_REQUEST = 0;
    private String permissionInfo;
    private Button btn_loc, btn_testMap, btn_locMap, btn_walkRouteSearch, btn_connDB;

    private Handler mHandler;
    private static final int SELECT = 1;

    public static void actionStart(Context context) {
        Intent starter = new Intent(context, TestMainActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        StatusBarUtils.setStatusBarMode(TestMainActivity.this, true, R.color.white);

        btn_loc = (Button) findViewById(R.id.btn_loc);
        btn_testMap = (Button) findViewById(R.id.btn_testMap);
        btn_locMap = (Button) findViewById(R.id.btn_locMap);
        btn_walkRouteSearch = (Button) findViewById(R.id.btn_walkRouteSearch);
        btn_connDB = (Button) findViewById(R.id.btn_connDB);
        btn_loc.setOnClickListener(this);
        btn_testMap.setOnClickListener(this);
        btn_locMap.setOnClickListener(this);
        btn_walkRouteSearch.setOnClickListener(this);
        btn_connDB.setOnClickListener(this);

        requestPermissions();

        // 注册 SDK 广播监听者
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        mHandler = new MyHandler(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 取消监听 SDK 广播
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_loc:
                Intent intent = new Intent(TestMainActivity.this, LocationActivity.class);
//                intent.putExtra("from", 0);
                startActivity(intent);
                break;
            case R.id.btn_testMap:
                intent = new Intent(TestMainActivity.this, TestMapActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_locMap:
                intent = new Intent(TestMainActivity.this, LocMapActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_walkRouteSearch:
                intent = new Intent(TestMainActivity.this, WalkRouteSearchActivity.class);
                startActivity(intent);
                break;
            case R.id.btn_connDB:
//                new Thread(new Runnable(){
//                    public void run(){
//                        String str = "";
//                        try{
//                            // 注册 JDBC 驱动
//                            Class.forName(JDBC_DRIVER);
//
//                            // 打开链接
//                            System.out.println("连接数据库...");
//                            conn = DriverManager.getConnection(DB_URL,USER,PASS);
//
//                            // 执行查询
//                            System.out.println(" 实例化Statement对象...");
//                            stmt = conn.createStatement();
//                            String sql;
//                            sql = "SELECT id, name, url FROM websites";
//                            ResultSet rs = stmt.executeQuery(sql);
//
//                            // 展开结果集数据库
//                            while(rs.next()){
//                                // 通过字段检索
//                                int id  = rs.getInt("id");
//                                String name = rs.getString("name");
//                                String url = rs.getString("url");
//
//                                // 输出数据
//                                str += ("ID: " + id);
//                                str += (", 站点名称: " + name);
//                                str += (", 站点 URL: " + url);
//                                str += ("\n");
//                            }
//                            // 完成后关闭
//                            rs.close();
//                            stmt.close();
//                            conn.close();
//                        }catch(SQLException se){
//                            // 处理 JDBC 错误
//                            se.printStackTrace();
//                        }catch(Exception e){
//                            // 处理 Class.forName 错误
//                            e.printStackTrace();
//                        }finally{
//                            // 关闭资源
//                            try{
//                                if(stmt!=null) stmt.close();
//                            }catch(SQLException se2){
//                            }// 什么都不做
//                            try{
//                                if(conn!=null) conn.close();
//                            }catch(SQLException se){
//                                se.printStackTrace();
//                            }
//                        }
//                        str += ("Goodbye!");
//                        MyLogUtil.LogI(str);
////                        Toast.makeText(TestMainActivity.this, str, Toast.LENGTH_LONG).show(); // 子线程无法Toast，属于UI操作
//                    }
//                }).start();
                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        String sqlResult = DBUtil.execute("SELECT * FROM user");
                        Message msg = new Message();
                        msg.what = SELECT;
                        Bundle bundle = new Bundle();
                        bundle.putString("sqlResult", sqlResult);
                        msg.setData(bundle);
                        mHandler.sendMessage(msg);
                    }
                }).start();
                break;
            default:
                break;
        }
    }

    private static class MyHandler extends Handler {
        // TODO: 2021/4/17 虚引用 处理消息 待重写
        WeakReference<TestMainActivity> weakReference;

        public MyHandler(TestMainActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case SELECT:
                    if(weakReference.get() != null)
                        Toast.makeText(weakReference.get(), msg.getData().getString("sqlResult"), Toast.LENGTH_LONG).show();
                    break;
                case 2:
                    break;
                default:
                    if(weakReference.get() != null)
                        Toast.makeText(weakReference.get(), "此消息未进行处理：" + msg.toString(), Toast.LENGTH_LONG).show();
                    break;
            }
        }
    }

    /**
     * Android6.0之后需要动态申请权限
     */
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            ArrayList<String> permissionsList = new ArrayList<String>();

            /***
             * 定位权限为必须权限，用户如果禁止，则每次进入都会申请
             */
            // 定位精确位置
            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
            if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                permissionsList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
            }

            String[] permissions = {
                    Manifest.permission.ACCESS_NETWORK_STATE,
                    Manifest.permission.INTERNET,
                    Manifest.permission.ACCESS_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_STATE,
                    Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
                    Manifest.permission.RECORD_AUDIO,
                    Manifest.permission.MODIFY_AUDIO_SETTINGS,
                    Manifest.permission.WRITE_SETTINGS,
            };
            for (String perm : permissions) {
                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
                    permissionsList.add(perm);
                    // 进入到这里代表没有权限.
                }
            }
            /*
             * 读写权限和电话状态权限非必要权限(建议授予)只会申请一次，用户同意或者禁止，只会弹一次
             */
            // 读写权限
            if (addPermission(permissionsList, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
            }
            if (addPermission(permissionsList, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                permissionInfo += "Manifest.permission.READ_EXTERNAL_STORAGE Deny \n";
            }
            // 读取电话状态权限
            if (addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE)) {
                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
            }

            if (permissionsList.size() > 0) {
                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), SDK_PERMISSION_REQUEST);
            }
        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        // 如果应用没有获得对应权限,则添加到列表中,准备批量申请
        if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
            if (shouldShowRequestPermissionRationale(permission)) {
                return true;
            } else {
                permissionsList.add(permission);
                return false;
            }
        } else {
            return true;
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        // TODO Auto-generated method stub
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 构造广播监听类，监听 SDK key 验证以及网络异常广播
     */
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Log.e("luchen", "key 验证出错! 错误码 :" + intent.getIntExtra
                        (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        +  " ; 请在 AndroidManifest.xml 文件中检查 key 设置");
            } else if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Log.e("luchen", "key 验证成功! 功能可以正常使用");
            } else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Log.e("luchen", "网络出错");
            }
        }
    }
}