package com.hyphenate.easeim.section.group.activity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;

import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.db.entity.EmUserEntity;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeui.domain.EaseUser;
import com.hyphenate.easeui.model.EaseEvent;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.bean.Team;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GroupTransferActivity extends GroupMemberAuthorityActivity {
    private Team myTeam;

    public static void actionStart(Context context, String groupId) {
        Intent starter = new Intent(context, GroupTransferActivity.class);
        starter.putExtra("groupId", groupId);
        context.startActivity(starter);
    }

    public static void actionStart(Context context, Team team, String groupId) {
        Intent starter = new Intent(context, GroupTransferActivity.class);
        starter.putExtra("team", team);
        starter.putExtra("groupId", groupId);
        context.startActivity(starter);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(GroupTransferActivity.this, true);
        titleBar.setTitle(getString(R.string.em_chat_group_authority_transfer));
    }

    @Override
    public void getData() {
        myTeam = getIntent().getExtras().getParcelable("team");
        viewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EMGroup>() {
                @Override
                public void onSuccess(EMGroup group) {
                    List<String> adminList = group.getAdminList();
                    if(adminList == null) {
                        adminList = new ArrayList<>();
                    }
                    adapter.setData(EmUserEntity.parse(adminList));
                    viewModel.getMembers(groupId);
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    finishRefresh();
                }
            });
        });
        viewModel.getTransferOwnerObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_OWNER_TRANSFER, EaseEvent.TYPE.GROUP));
                    finish();
                }
            });
        });
        viewModel.getRefreshObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String message) {
                    refreshData();
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
                }
            });
        });
        viewModel.getMemberObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<List<EaseUser>>() {
                @Override
                public void onSuccess(List<EaseUser> data) {
                    adapter.addData(data);
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    finishRefresh();
                }
            });
        });
        viewModel.getMessageChangeObservable().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event == null) {
                return;
            }
            if(event.isGroupChange()) {
                refreshData();
            }else if(event.isGroupLeave() && TextUtils.equals(groupId, event.message)) {
                finish();
            }
        });
        refreshData();
    }

    protected void refreshData() {
        viewModel.getGroup(groupId);
    }

    @Override
    public boolean onItemLongClick(View view, int position) {
        if(isMember()) {
            return false;
        }
        PopupMenu menu = new PopupMenu(mContext, view);
        menu.setGravity(Gravity.CENTER_HORIZONTAL);
        menu.getMenuInflater().inflate(R.menu.demo_group_member_authority_item_menu, menu.getMenu());
        MenuPopupHelper menuPopupHelper = new MenuPopupHelper(mContext, (MenuBuilder) menu.getMenu(), view);
        menuPopupHelper.setForceShowIcon(true);
        menuPopupHelper.setGravity(Gravity.CENTER_HORIZONTAL);
        EaseUser item = adapter.getItem(position);
        if(item == null) {
            return false;
        }
        String username = item.getUsername();
        setMenuInfo(menu.getMenu());
        if(isInAdminList(username)) {
            setMenuItemVisible(menu.getMenu(), R.id.action_group_remove_admin);
            setMenuItemVisible(menu.getMenu(), R.id.action_group_transfer_owner);
        }else {
            menu.getMenu().findItem(R.id.action_group_add_admin).setVisible(isOwner());
            setMenuItemVisible(menu.getMenu(), R.id.action_group_transfer_owner);
        }
        menuPopupHelper.show();
        menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_group_add_admin ://设为管理员
                        addToAdmins(username);
                        break;
                    case R.id.action_group_remove_admin ://移除管理员
                        removeFromAdmins(username);
                        break;
                    case R.id.action_group_transfer_owner ://移交群主
                        transferOwner(username);
                        // TODO: 2021/4/21 在数据库执行更改队伍负责人信息操作
                        executorService = Executors.newSingleThreadExecutor();
                        Future future = executorService.submit(new Callable() {
                            public Object call() throws Exception {
                                Person p = DBUtil.selectPerson(username);

                                if(DBUtil.updateTeam(myTeam.getTeamID(), "principal_name", p.getName())
                                        && DBUtil.updateTeam(myTeam.getTeamID(), "principal_id", p.getId())
                                        && DBUtil.updateTeam(myTeam.getTeamID(), "principal_tel", p.getTel())) {
                                    return "成功更换队伍负责人,ID: " + p.getName() + " 用户名: " + p.getName() + " 手机号: " + p.getTel();
                                }
                                return "更改队伍队伍负责人失败！";
                            }
                        });
                        try {
                            ToastUtils.showToast(String.valueOf(future.get()));
                            executorService.shutdown();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });
        return true;
    }
}
