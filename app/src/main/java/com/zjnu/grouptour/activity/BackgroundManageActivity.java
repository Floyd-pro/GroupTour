package com.zjnu.grouptour.activity;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.ArrayRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.base.WebViewActivity;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.adapter.MsAdapter;
import com.zjnu.grouptour.adapter.adminmanage.ContentAdapter;
import com.zjnu.grouptour.adapter.adminmanage.TopTabAdpater;
import com.zjnu.grouptour.api.GroupTourApi;
import com.zjnu.grouptour.bean.AdminInfoManage;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.bean.Schedule;
import com.zjnu.grouptour.bean.TableBean;
import com.zjnu.grouptour.bean.Team;
import com.zjnu.grouptour.utils.CommonUtils;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;
import com.zjnu.grouptour.view.CustomHorizontalScrollView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author luchen
 * @Date 2021/5/3 14:13
 * @Description 后台用户/队伍信息管理界面
 */
public class BackgroundManageActivity extends AppCompatActivity implements ContentAdapter.OnContentScrollListener, AdapterView.OnItemSelectedListener {

//    @BindView(R.id.tv_left_title)
//    TextView tvLeftTitle;
    @BindView(R.id.rv_tab_right)
    RecyclerView rvTabRight;
    @BindView(R.id.hor_scrollview)
    CustomHorizontalScrollView horScrollview;
    @BindView(R.id.ll_top_root)
    LinearLayout llTopRoot;
    @BindView(R.id.recycler_content)
    RecyclerView recyclerContent;
    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout swipeRefresh;
    private List<AdminInfoManage> mEntities = new ArrayList<>();
    private List<String> rightMoveDatas = new ArrayList<>();
    private List<String> topTabs = new ArrayList<>();
    private final ContentAdapter contentAdapter = new ContentAdapter(this);
    private TopTabAdpater topTabAdpater;

    private static final ArrayList<String> userFieldName = new ArrayList<String>(Arrays.asList("ID", "用户名", "登录密码", "经度", "纬度", "队伍ID", "性别", "昵称", "联系电话", "微信号", "QQ号", "Email", "个性签名", "真实姓名", "身份证号"));
    private static final ArrayList<String> teamFieldName = new ArrayList<String>(Arrays.asList("ID", "名称", "简介", "目的地名称", "目的地城市", "目的地经度", "目的地纬度", "出发时间", "负责人ID", "负责人姓名", "负责人联系电话", "行程表ID"));
    private static final ArrayList<String> scheduleFieldName = new ArrayList<String>(Arrays.asList("ID", "名称", "内容"));

    private ArrayList<Person> users;
    private ArrayList<Team> teams;
    private ArrayList<Schedule> schedules;
    ExecutorService executorService; // 用于把队伍信息 更新到数据库

    // spinner 初始化
    @BindView(R.id.table_scope)
    Spinner table_scope;
    //判断是否为刚进去时触发 onItemSelected 的标志
    private boolean spinnerSelected = false;
    private ArrayList<TableBean> tableData = null;
    private MsAdapter tableAdadpter = null;

    private int selectedNum = 1; // 默认1为用户表，2为队伍表，3为行程表

    @BindView(R.id.img_question)
    ImageView imgQuestion;

    public static void actionStart(Context context) {
        Intent starter = new Intent(context, BackgroundManageActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_background_manage);
        StatusBarUtils.setTranslucentStatusTextMode(BackgroundManageActivity.this, true);
        ButterKnife.bind(this);

        getInfo();

        tableData = new ArrayList<TableBean>();
        bindViews();

        //处理顶部标题部分
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        rvTabRight.setLayoutManager(linearLayoutManager);
        topTabAdpater = new TopTabAdpater(this);
        rvTabRight.setAdapter(topTabAdpater);
        for (String s : userFieldName) {
            topTabs.add(s);
        }
        topTabAdpater.setDatas(topTabs);
        //处理内容部分
        recyclerContent.setLayoutManager(new LinearLayoutManager(this));
        recyclerContent.setHasFixedSize(true);
        recyclerContent.setAdapter(contentAdapter);
        contentAdapter.setOnContentScrollListener(this);

        userInfoRefresh();

        imgQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(BackgroundManageActivity.this)
                        .setTitle("提示")
                        .setMessage("当前暂不支持在App内编辑数据，是否跳转到数据管理平台？")
                        .setPositiveButton("是", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                WebViewActivity.actionStart(BackgroundManageActivity.this, "https://dms.aliyun.com/", false);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("否", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        }).create().show();
            }
        });

        //滚动RV时,同步所有横向位移的item
        recyclerContent.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                List<ContentAdapter.ItemViewHolder> viewHolderCacheList = contentAdapter.getViewHolderCacheList();
                if (null != viewHolderCacheList) {
                    int size = viewHolderCacheList.size();
                    for (int i = 0; i < size; i++) {
                        viewHolderCacheList.get(i).horItemScrollview.scrollTo(contentAdapter.getOffestX(), 0);
                    }
                }

            }
        });

        //同步顶部tab的横向scroll和内容页面的横向滚动
        //同步滚动顶部tab和内容
        horScrollview.setOnCustomScrollChangeListener(new CustomHorizontalScrollView.OnCustomScrollChangeListener() {
            @Override
            public void onCustomScrollChange(CustomHorizontalScrollView listener, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
                //代码重复,可以抽取/////
                contentAdapter.offestX = scrollX;
                List<ContentAdapter.ItemViewHolder> viewHolderCacheList = contentAdapter.getViewHolderCacheList();
                if (null != viewHolderCacheList) {
                    int size = viewHolderCacheList.size();
                    for (int i = 0; i < size; i++) {
                        viewHolderCacheList.get(i).horItemScrollview.scrollTo(scrollX, 0);
                    }
                }
            }

        });

        //下拉刷新
        swipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                switch (selectedNum) {
                    case 1:
                        userInfoRefresh();
                        break;
                    case 2:
                        teamInfoRefresh();
                        break;
                    case 3:
                        scheduleInfoRefresh();
                        break;
                }
                swipeRefresh.setRefreshing(false);
            }
        });

    }

    @Override
    public void onScroll(MotionEvent event) {
        //处理单个item滚动时,顶部tab需要联动
        if (null != horScrollview) horScrollview.onTouchEvent(event);
    }

    // 获取用户、队伍及队伍成员的信息
    private void getInfo() {
        executorService = Executors.newSingleThreadExecutor();
        if(getUsersInfo()) {
            if(getTeamsInfo()) {
                if(getSchedulesInfo()) {
                    if(GroupTourApi.getInstance().isOpenLogger())
                        ToastUtils.showToast("用户、队伍及行程表信息获取成功！");
                } else {
                    if(GroupTourApi.getInstance().isOpenLogger())
                        ToastUtils.showToast("行程表信息获取失败！");
                }
            } else {
                if(GroupTourApi.getInstance().isOpenLogger())
                    ToastUtils.showToast("队伍信息获取失败！");
            }
        } else {
            if(GroupTourApi.getInstance().isOpenLogger())
                ToastUtils.showToast("用户信息获取失败！");
        }
        executorService.shutdown();
    }

    // 获取所有用户的信息
    private boolean getUsersInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                ArrayList<Person> users = DBUtil.selectAllUsers();
                return users;
            }
        });
        try {
            if(future.get() != null) {
                users = (ArrayList<Person>) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取所有队伍的信息
    private boolean getTeamsInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                ArrayList<Team> teams = DBUtil.selectAllTeams();
                return teams;
            }
        });
        try {
            if(future.get() != null) {
                teams = (ArrayList<Team>) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    // 获取所有队伍的行程表信息
    private boolean getSchedulesInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                ArrayList<Schedule> schedules = DBUtil.selectAllSchedules();
                return schedules;
            }
        });
        try {
            if(future.get() != null) {
                schedules = (ArrayList<Schedule>) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void bindViews() {
        tableData.add(new TableBean("用户表"));
        tableData.add(new TableBean("队伍表"));
        tableData.add(new TableBean("行程表"));

        tableAdadpter = new MsAdapter<TableBean>(tableData,R.layout.spinner_table_item) {
            @Override
            public void bindView(MsAdapter.ViewHolder holder, TableBean obj) {
                holder.setText(R.id.name, obj.getTableName());
                holder.setTag(R.id.name, obj.getTableName());
            }
        };

        table_scope.setAdapter(tableAdadpter);
        table_scope.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        switch (parent.getId()){
            case R.id.table_scope:
                if(spinnerSelected){
                    String tableName = ((TableBean) parent.getItemAtPosition(position)).getTableName();
                    ToastUtils.showToast("您当前选择的是：" + tableName);
                    switch (tableName) {
                        case "用户表":
                            selectedNum = 1;
                            userInfoRefresh();
                            break;
                        case "队伍表":
                            selectedNum = 2;
                            teamInfoRefresh();
                            break;
                        case "行程表":
                            selectedNum = 3;
                            scheduleInfoRefresh();
                            break;
                    }
                }else
                    spinnerSelected = true;
                break;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }

    private void userInfoRefresh() {
        topTabs.clear();
        mEntities.clear();
        for (String s : userFieldName) {
            topTabs.add(s);
        }
        topTabAdpater.setDatas(topTabs);
        recyclerContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                AdminInfoManage entity;
                for (int i = 0; i < users.size(); i++) {
                    entity = new AdminInfoManage();
                    entity.setLeftTitle("用户" + i);
                    rightMoveDatas = new ArrayList<String>();
                    String addData = "";
                    for (int j = 0; j < 15; j++) {
                        switch (j) {
                            case 0:
                                addData = String.valueOf(users.get(i).getId());
                                break;
                            case 1:
                                addData = users.get(i).getName();
                                break;
                            case 2:
                                addData = users.get(i).getPsw();
                                break;
                            case 3:
                                addData = String.valueOf(users.get(i).getLongitude());
                                break;
                            case 4:
                                addData = String.valueOf(users.get(i).getLatitude());
                                break;
                            case 5:
                                addData = String.valueOf(users.get(i).getTeamID());
                                break;
                            case 6:
                                addData = users.get(i).getGender();
                                break;
                            case 7:
                                addData = users.get(i).getNickname();
                                break;
                            case 8:
                                addData = users.get(i).getTel();
                                break;
                            case 9:
                                addData = users.get(i).getWechat();
                                break;
                            case 10:
                                addData = users.get(i).getQq();
                                break;
                            case 11:
                                addData = users.get(i).getEmail();
                                break;
                            case 12:
                                addData = users.get(i).getSignature();
                                break;
                            case 13:
                                addData = users.get(i).getRealName();
                                break;
                            case 14:
                                addData = users.get(i).getIdentityNum();
                                break;
                        }
                        rightMoveDatas.add(addData);
                    }
                    entity.setRightDatas(rightMoveDatas);
                    mEntities.add(entity);
                }
                contentAdapter.setDatas(mEntities);
                ToastUtils.showToast("加载完毕,共加载了" + users.size() + "个用户的数据");
            }
        }, 1500);
    }

    private void teamInfoRefresh() {
        topTabs.clear();
        mEntities.clear();
        for (String s : teamFieldName) {
            topTabs.add(s);
        }
        topTabAdpater.setDatas(topTabs);
        recyclerContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                AdminInfoManage entity;
                for (int i = 0; i < teams.size(); i++) {
                    entity = new AdminInfoManage();
                    entity.setLeftTitle("队伍" + i);
                    rightMoveDatas = new ArrayList<String>();
                    String addData = "";
                    for (int j = 0; j < 12; j++) {
                        switch (j) {
                            case 0:
                                addData = String.valueOf(teams.get(i).getTeamID());
                                break;
                            case 1:
                                addData = teams.get(i).getTeamName();
                                break;
                            case 2:
                                addData = teams.get(i).getDescription();
                                break;
                            case 3:
                                addData = teams.get(i).getDestinationName();
                                break;
                            case 4:
                                addData = teams.get(i).getDestinationCity();
                                break;
                            case 5:
                                addData = String.valueOf(teams.get(i).getDestinationLongitude());
                                break;
                            case 6:
                                addData = String.valueOf(teams.get(i).getDestinationLatitude());
                                break;
                            case 7:
                                addData = CommonUtils.dateToString(teams.get(i).getDepartureDateTime(), "yyyy-MM-dd HH:mm:ss");
                                break;
                            case 8:
                                addData = String.valueOf(teams.get(i).getPrincipalID());
                                break;
                            case 9:
                                addData = teams.get(i).getPrincipalName();
                                break;
                            case 10:
                                addData = teams.get(i).getPrincipalTel();
                                break;
                            case 11:
                                addData = String.valueOf(teams.get(i).getScheduleID());
                                break;
                        }
                        rightMoveDatas.add(addData);
                    }
                    entity.setRightDatas(rightMoveDatas);
                    mEntities.add(entity);
                }
                contentAdapter.setDatas(mEntities);
                ToastUtils.showToast("加载完毕,共加载了" + teams.size() + "支队伍的数据");
            }
        }, 1500);
    }

    private void scheduleInfoRefresh() {
        topTabs.clear();
        mEntities.clear();
        for (String s : scheduleFieldName) {
            topTabs.add(s);
        }
        topTabAdpater.setDatas(topTabs);
        recyclerContent.postDelayed(new Runnable() {
            @Override
            public void run() {
                AdminInfoManage entity;
                for (int i = 0; i < schedules.size(); i++) {
                    entity = new AdminInfoManage();
                    entity.setLeftTitle("行程表" + i);
                    rightMoveDatas = new ArrayList<String>();
                    String addData = "";
                    for (int j = 0; j < 3; j++) {
                        switch (j) {
                            case 0:
                                addData = String.valueOf(schedules.get(i).getScheduleID());
                                break;
                            case 1:
                                addData = schedules.get(i).getScheduleName();
                                break;
                            case 2:
                                addData = schedules.get(i).getScheduleInfo();
                                break;
                        }
                        rightMoveDatas.add(addData);
                    }
                    entity.setRightDatas(rightMoveDatas);
                    mEntities.add(entity);
                }
                contentAdapter.setDatas(mEntities);
                ToastUtils.showToast("加载完毕,共加载了" + teams.size() + "个行程表的数据");
            }
        }, 1500);
    }
}
