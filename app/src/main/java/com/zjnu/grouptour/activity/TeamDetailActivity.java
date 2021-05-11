package com.zjnu.grouptour.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;

import androidx.annotation.Nullable;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMPushConfigs;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.common.widget.ArrowItemView;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.exceptions.HyphenateException;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.api.GroupTourApi;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.bean.Team;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author luchen
 * @Date 2021/4/20 20:28
 * @Description 查看队伍信息
 */
public class TeamDetailActivity extends BaseInitActivity implements View.OnClickListener {
    private EaseTitleBar titleBar;

    private ArrowItemView itemTeamName, itemDestinationName, itemPrincipalName, itemSchedule;
    private Team myTeam;
    private ExecutorService executorService;

    private boolean isPrincipal; // 是否为负责人

//    public static void actionStart(Context context) {
//        Intent intent = new Intent(context, TeamDetailActivity.class);
//        context.startActivity(intent);
//    }

    public static void actionStart(Context context, String name) {
        Intent intent = new Intent(context, TeamDetailActivity.class);
        intent.putExtra("teamName", name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(TeamDetailActivity.this, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        executorService = Executors.newSingleThreadExecutor();
        if(GroupTourApi.getInstance().isOpenLogger() && getTeamInfo())
            ToastUtils.showToast("队伍信息获取成功！");
        else
            ToastUtils.showToast("队伍信息获取失败！");
    }

    @Override
    protected void onPause() {
        super.onPause();
        executorService.shutdown();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_team_detail;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        itemTeamName = findViewById(R.id.item_team_name);
        itemDestinationName = findViewById(R.id.item_destination_name);
        itemPrincipalName = findViewById(R.id.item_principal_name);
        itemSchedule = findViewById(R.id.item_schedule);
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(new EaseTitleBar.OnBackPressListener() {
            @Override
            public void onBackPress(View view) {
                onBackPressed();
            }
        });
        itemTeamName.setOnClickListener(this);
        itemDestinationName.setOnClickListener(this);
        itemPrincipalName.setOnClickListener(this);
        itemSchedule.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.item_team_name:
                Team team = new Team(myTeam.getTeamID(), myTeam.getTeamName(), myTeam.getDescription(),
                        myTeam.getDestinationName(), myTeam.getDestinationCity(), myTeam.getDestinationLongitude(), myTeam.getDestinationLatitude(),
                        myTeam.getDepartureDateTime(), myTeam.getPrincipalID(), myTeam.getPrincipalName(),
                        myTeam.getPrincipalTel(), myTeam.getScheduleID());
                TeamInfoChangeActivity.actionStart(mContext, team, "team_name");
                break;
            case R.id.item_destination_name:
                team = new Team(myTeam.getTeamID(), myTeam.getTeamName(), myTeam.getDescription(),
                        myTeam.getDestinationName(), myTeam.getDestinationCity(), myTeam.getDestinationLongitude(), myTeam.getDestinationLatitude(),
                        myTeam.getDepartureDateTime(), myTeam.getPrincipalID(), myTeam.getPrincipalName(),
                        myTeam.getPrincipalTel(), myTeam.getScheduleID());
                TeamInfoChangeActivity.actionStart(mContext, team, "destination_name");
                break;
            case R.id.item_principal_name:
                team = new Team(myTeam.getTeamID(), myTeam.getTeamName(), myTeam.getDescription(),
                        myTeam.getDestinationName(), myTeam.getDestinationCity(), myTeam.getDestinationLongitude(), myTeam.getDestinationLatitude(),
                        myTeam.getDepartureDateTime(), myTeam.getPrincipalID(), myTeam.getPrincipalName(),
                        myTeam.getPrincipalTel(), myTeam.getScheduleID());
                TeamInfoChangeActivity.actionStart(mContext, team, "principal_name");
                break;
            case R.id.item_schedule:
                // TODO: 2021/4/20 转到行程表界面

                break;
            default:
                break;
        }
    }

    @Override
    protected void initData() {
        super.initData();
//            isPrincipal = true;
        itemTeamName.getTvContent().setText(getIntent().getStringExtra("teamName"));
        itemTeamName.setArrowVisible(true);
        itemDestinationName.setArrowVisible(true);
        itemPrincipalName.setArrowVisible(true);
        itemSchedule.setArrowVisible(true);
        // TODO: 2021/4/20 群主和副负责人权限区分 待优化

//            itemHxId.getTvContent().setText(DemoHelper.getInstance().getCurrentUser());
//            getNickname();
//            LiveDataBus.get().with(DemoConstant.REFRESH_NICKNAME, Boolean.class).observe(this, event -> {
//                if(event == null) {
//                    return;
//                }
//                if(event) { // 这里是进不去的
//                    getNickname();
//                }
//            });

    }

//    private void getNickname() {
//        EMPushConfigs configs = null;
//        try {
//            configs = EMClient.getInstance().pushManager().getPushConfigsFromServer();
//            String nickname = configs.getDisplayNickname();
//            if(!TextUtils.isEmpty(nickname)) {
//                itemNickname.getTvContent().setText(nickname);
//            }
//        } catch (HyphenateException e) {
//            e.printStackTrace();
//        }
//    }

    private boolean getTeamInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Team t = DBUtil.selectTeam(itemTeamName.getTvContent().getText().toString());
                return t;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新用户信息
                myTeam = (Team) future.get();
                itemTeamName.getTvContent().setText(myTeam.getTeamName());
                itemDestinationName.getTvContent().setText(myTeam.getDestinationName());
                itemPrincipalName.getTvContent().setText(String.valueOf(myTeam.getPrincipalName()));
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        } finally {
            executorService.shutdown();
        }
        return false;
    }
}

