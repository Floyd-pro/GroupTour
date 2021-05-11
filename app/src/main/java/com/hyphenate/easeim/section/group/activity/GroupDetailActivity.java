package com.hyphenate.easeim.section.group.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMGroup;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.widget.ArrowItemView;
import com.hyphenate.easeim.common.widget.SwitchItemView;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.dialog.DemoDialogFragment;
import com.hyphenate.easeim.section.dialog.EditTextDialogFragment;
import com.hyphenate.easeim.section.dialog.SimpleDialogFragment;
import com.hyphenate.easeim.section.group.GroupHelper;
import com.hyphenate.easeim.section.group.fragment.GroupEditFragment;
import com.hyphenate.easeim.section.group.viewmodels.GroupDetailViewModel;
import com.hyphenate.easeim.section.search.SearchGroupChatActivity;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.utils.EaseCommonUtils;
import com.hyphenate.easeui.widget.EaseImageView;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.zjnu.grouptour.activity.DestinationSettingActivity;
import com.zjnu.grouptour.api.GroupTourApi;
import com.zjnu.grouptour.bean.Schedule;
import com.zjnu.grouptour.bean.Team;
import com.zjnu.grouptour.dialog.DatetimeDialogFragment;
import com.zjnu.grouptour.utils.CommonUtils;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class GroupDetailActivity extends BaseInitActivity implements EaseTitleBar.OnBackPressListener, View.OnClickListener, SwitchItemView.OnCheckedChangeListener {
    private static final int REQUEST_CODE_ADD_USER = 0;
    private EaseTitleBar titleBar;
    private EaseImageView ivGroupAvatar;
    private TextView tvGroupName;
    private TextView tvGroupIntroduction;
    private TextView tvGroupMemberTitle;
    private TextView tvGroupMemberNum;
    private TextView tvGroupInvite;
    private ArrowItemView itemGroupName;
    private ArrowItemView itemGroupShareFile;
    private ArrowItemView itemGroupNotice;
    private ArrowItemView itemGroupIntroduction;
    private ArrowItemView itemGroupMemberManage;
    private ArrowItemView itemGroupHistory;
    private ArrowItemView itemGroupClearHistory;
    private SwitchItemView itemGroupNotDisturb;
    private SwitchItemView itemGroupOffPush;
    private SwitchItemView itemGroupTop;
    private TextView tvGroupRefund;
    private String groupId;
    private EMGroup group;
    private GroupDetailViewModel viewModel;
    private EMConversation conversation;

    private ArrowItemView itemGroupSchedule;
    private ArrowItemView itemGroupDestination;
    private ArrowItemView itemGroupDepartureDatetime;
    private ArrowItemView itemGroupDestinationSetting;
    private ArrowItemView itemGroupDepartureDatetimeSetting;
    SimpleDateFormat formatDatetime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    SimpleDateFormat formatDate = new SimpleDateFormat("yyyy-MM-dd");
    SimpleDateFormat formatTime = new SimpleDateFormat("HH:mm:ss");
    SimpleDateFormat formatHourMinute = new SimpleDateFormat("HH:mm");
    private String departureTime;
    private String departureDate;
    private boolean isTimeUpdated = false;
    private boolean isDateUpdated = false;
    private Handler mHandler;

//    private ConstraintLayout clGroupInfo;
    ExecutorService executorService; // 用于把队伍信息 更新到数据库
    private Team myTeam;
    private Schedule mSchedule;

    public static void actionStart(Context context, String groupId) {
        Intent intent = new Intent(context, GroupDetailActivity.class);
        intent.putExtra("groupId", groupId);
        context.startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        executorService = Executors.newSingleThreadExecutor();
        if(GroupTourApi.getInstance().isOpenLogger() && getMyTeamInfo() && getScheduleInfo()) {
            ToastUtils.showToast("队伍资料获取成功！");
        }
        else {
            ToastUtils.showToast("队伍资料获取失败！");
        }
        executorService.shutdown();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_chat_group_detail;
    }

    @Override
    protected void initIntent(Intent intent) {
        super.initIntent(intent);
        groupId = intent.getStringExtra("groupId");
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(GroupDetailActivity.this, true);
        titleBar = findViewById(R.id.title_bar);
        ivGroupAvatar = findViewById(R.id.iv_group_avatar);
        tvGroupName = findViewById(R.id.tv_group_name);
        tvGroupIntroduction = findViewById(R.id.tv_group_introduction);
        tvGroupMemberTitle = findViewById(R.id.tv_group_member_title);
        tvGroupMemberNum = findViewById(R.id.tv_group_member_num);
        tvGroupInvite = findViewById(R.id.tv_group_invite);
        itemGroupName = findViewById(R.id.item_group_name);
        itemGroupShareFile = findViewById(R.id.item_group_share_file);
        itemGroupNotice = findViewById(R.id.item_group_notice);
        itemGroupIntroduction = findViewById(R.id.item_group_introduction);
        itemGroupHistory = findViewById(R.id.item_group_history);
        itemGroupClearHistory = findViewById(R.id.item_group_clear_history);
        itemGroupNotDisturb = findViewById(R.id.item_group_not_disturb);
        itemGroupOffPush = findViewById(R.id.item_group_off_push);
        itemGroupTop = findViewById(R.id.item_group_top);
        tvGroupRefund = findViewById(R.id.tv_group_refund);
        itemGroupMemberManage = findViewById(R.id.item_group_member_manage);

        itemGroupSchedule = findViewById(R.id.item_group_schedule);
        itemGroupDestination = findViewById(R.id.item_group_destination);
        itemGroupDepartureDatetime = findViewById(R.id.item_group_depature_datetime);
        itemGroupDestinationSetting = findViewById(R.id.item_group_destination_setting);
        itemGroupDepartureDatetimeSetting = findViewById(R.id.item_group_departure_datetime_setting);
        group = DemoHelper.getInstance().getGroupManager().getGroup(groupId);
        mHandler = new MyHandler();
//        clGroupInfo = findViewById(R.id.cl_group_info);
        initGroupView();
    }

    @Override
    protected void initListener() {
        super.initListener();
        titleBar.setOnBackPressListener(this);
        tvGroupMemberTitle.setOnClickListener(this);
        tvGroupMemberNum.setOnClickListener(this);
        tvGroupInvite.setOnClickListener(this);
        itemGroupName.setOnClickListener(this);
        itemGroupShareFile.setOnClickListener(this);
        itemGroupNotice.setOnClickListener(this);
        itemGroupIntroduction.setOnClickListener(this);
        itemGroupHistory.setOnClickListener(this);
        itemGroupClearHistory.setOnClickListener(this);
        itemGroupNotDisturb.setOnCheckedChangeListener(this);
        itemGroupOffPush.setOnCheckedChangeListener(this);
        itemGroupTop.setOnCheckedChangeListener(this);
        tvGroupRefund.setOnClickListener(this);
        itemGroupMemberManage.setOnClickListener(this);
        itemGroupDestinationSetting.setOnClickListener(this);
        itemGroupDepartureDatetimeSetting.setOnClickListener(this);
//        clGroupInfo.setOnClickListener(this);
        itemGroupSchedule.setOnClickListener(this);
    }

    private void initGroupView() {
        if(group == null) {
            finish();
            return;
        }
        tvGroupName.setText(group.getGroupName());
        itemGroupName.getTvContent().setText(group.getGroupName());
        tvGroupMemberNum.setText(getString(R.string.em_chat_group_detail_member_num, group.getMemberCount()));
        tvGroupRefund.setText(getResources().getString(isOwner() ? R.string.em_chat_group_detail_dissolve : R.string.em_chat_group_detail_refund));
        tvGroupIntroduction.setText(group.getDescription());
        //itemGroupNotDisturb.getSwitch().setChecked(group.isMsgBlocked());
        conversation = DemoHelper.getInstance().getConversation(groupId, EMConversation.EMConversationType.GroupChat, true);
        String extField = conversation.getExtField();
        itemGroupTop.getSwitch().setChecked(!TextUtils.isEmpty(extField) && EaseCommonUtils.isTimestamp(extField));
        tvGroupInvite.setVisibility(group.getMemberCount() <= 0 ? View.VISIBLE : View.GONE);
        tvGroupInvite.setVisibility(isCanInvite() ? View.VISIBLE : View.GONE);
        //itemGroupNotDisturb.getSwitch().setChecked(group.isMsgBlocked());
        itemGroupMemberManage.setVisibility((isOwner() || isAdmin()) ? View.VISIBLE : View.GONE);

        itemGroupDestination.setVisibility((isOwner() || isAdmin()) ? View.GONE : View.VISIBLE);
        itemGroupDepartureDatetime.setVisibility((isOwner() || isAdmin()) ? View.GONE : View.VISIBLE);
        itemGroupDestinationSetting.setVisibility((isOwner() || isAdmin()) ? View.VISIBLE : View.GONE);
        itemGroupDepartureDatetimeSetting.setVisibility((isOwner() || isAdmin()) ? View.VISIBLE : View.GONE);

        itemGroupIntroduction.getTvContent().setText(group.getDescription());

        makeTextSingleLine(itemGroupNotice.getTvContent());
        makeTextSingleLine(itemGroupIntroduction.getTvContent());

        List<String> disabledIds = DemoHelper.getInstance().getPushManager().getNoPushGroups();
        itemGroupNotDisturb.getSwitch().setChecked(disabledIds != null && disabledIds.contains(groupId));
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(GroupDetailViewModel.class);
        viewModel.getGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EMGroup>() {
                @Override
                public void onSuccess(EMGroup data) {
                    group = data;
                    initGroupView();
                }
            });
        });
        viewModel.getAnnouncementObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    if(myTeam.getDestinationName() == null) {
                        itemGroupNotice.getTvContent().setText(data);
                    }else {
                        itemGroupNotice.getTvContent().setText(data);
                    }
                }
            });
        });
        viewModel.getRefreshObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<String>() {
                @Override
                public void onSuccess(String data) {
                    loadGroup();
                }
            });
        });
        viewModel.getMessageChangeObservable().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, event -> {
            if(event.isGroupLeave() && TextUtils.equals(groupId, event.message)) {
                finish();
                return;
            }
            if(event.isGroupChange()) {
                loadGroup();
            }
        });
        viewModel.getLeaveGroupObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    finish();
                    LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_LEAVE, EaseEvent.TYPE.GROUP, groupId));
                }
            });
        });
        viewModel.blockGroupMessageObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    //itemGroupNotDisturb.getSwitch().setChecked(true);
                }
            });
        });
        viewModel.unblockGroupMessage().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    //itemGroupNotDisturb.getSwitch().setChecked(false);
                }
            });
        });
        viewModel.offPushObservable().observe(this, response -> {
            if(response) {
                loadGroup();
            }
        });
        viewModel.getClearHistoryObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    LiveDataBus.get().with(DemoConstant.CONVERSATION_DELETE).postValue(new EaseEvent(DemoConstant.CONTACT_DECLINE, EaseEvent.TYPE.MESSAGE));
                }
            });
        });
        loadGroup();
        executorService = Executors.newSingleThreadExecutor();
        if(GroupTourApi.getInstance().isOpenLogger() && getMyTeamInfo() && getScheduleInfo()) {
            ToastUtils.showToast("队伍资料获取成功！");
        }
        else {
            ToastUtils.showToast("队伍资料获取失败！");
        }
        itemGroupSchedule.getTvContent().setText(mSchedule.getScheduleInfo());
        itemGroupDestination.getTvContent().setText(myTeam.getDestinationName());
        itemGroupDepartureDatetime.getTvContent().setText(CommonUtils.dateToString(myTeam.getDepartureDateTime(), "yyyy-MM-dd HH-mm-ss"));
        itemGroupDestinationSetting.getTvContent().setText(myTeam.getDestinationName());
        itemGroupDepartureDatetimeSetting.getTvContent().setText(CommonUtils.dateToString(myTeam.getDepartureDateTime(), "yyyy-MM-dd HH-mm-ss"));
    }

    private void loadGroup() {
        viewModel.getGroup(groupId);
        viewModel.getGroupAnnouncement(groupId);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.tv_group_member_title :// 群成员
                GroupMemberTypeActivity.actionStart(mContext, groupId, isOwner());
                break;
            case R.id.tv_group_invite ://邀请群成员
                GroupPickContactsActivity.actionStartForResult(mContext, groupId, isOwner(), REQUEST_CODE_ADD_USER);
                break;
            case R.id.item_group_name ://群名称
                showGroupNameDialog();
                break;
            case R.id.item_group_share_file ://共享文件
                GroupSharedFilesActivity.actionStart(mContext, groupId);
                break;
            case R.id.item_group_notice ://群公告
                showAnnouncementDialog();
                break;
            case R.id.item_group_introduction ://群介绍
                showIntroductionDialog();
                break;
            case R.id.item_group_schedule : // 队伍行程表
                showScheduleDialog();
                break;
            case R.id.item_group_history ://查找聊天记录
                SearchGroupChatActivity.actionStart(mContext, groupId);
                break;
            case R.id.item_group_clear_history://清空聊天记录
                showClearConfirmDialog();
                break;
            case R.id.tv_group_refund ://退出群组
                showConfirmDialog();
                break;
            case R.id.item_group_member_manage://群组管理
                GroupManageIndexActivity.actionStart(mContext, myTeam, groupId);
                break;
            case R.id.item_group_destination_setting://目的地设置
                DestinationSettingActivity.actionStart(mContext, myTeam, groupId);
                break;
            case R.id.item_group_departure_datetime_setting://出发时间设置
                showDepartureDatetimeDialog();
                break;
        }
    }

    private void showClearConfirmDialog() {
        new SimpleDialogFragment.Builder(mContext)
                .setTitle(R.string.em_chat_group_detail_clear_history_warning)
                .setOnConfirmClickListener(new DemoDialogFragment.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        viewModel.clearHistory(groupId);
                    }
                })
                .showCancelButton(true)
                .show();
    }

    private void showConfirmDialog() {
        new SimpleDialogFragment.Builder(mContext)
                .setTitle(isOwner() ? R.string.em_chat_group_detail_dissolve : R.string.em_chat_group_detail_refund)
                .setOnConfirmClickListener(new DemoDialogFragment.OnConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view) {
                        if(isOwner()) {
                            viewModel.destroyGroup(groupId);
                            // TODO: 2021/4/21 在数据库执行删除队伍操作
                            executorService = Executors.newSingleThreadExecutor();
                            Future future = executorService.submit(new Callable() {
                                public Object call() throws Exception {
                                    if(DBUtil.deleteTeam(tvGroupName.getText().toString())) {
                                        return "成功删除队伍: " + tvGroupName.getText().toString();
                                    }
                                    return "删除队伍数据失败！";
                                }
                            });
                            try {
                                ToastUtils.showToast(String.valueOf(future.get()));
                                executorService.shutdown();
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }else {
                            viewModel.leaveGroup(groupId);
                        }
                    }
                })
                .showCancelButton(true)
                .show();
    }

    @Override
    public void onCheckedChanged(SwitchItemView buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.item_group_not_disturb ://消息免打扰
                viewModel.updatePushServiceForGroup(groupId, isChecked);
                /*if(isChecked) {
                    viewModel.blockGroupMessage(groupId);
                }else {
                    viewModel.unblockGroupMessage(groupId);
                }*/
                break;
            case R.id.item_group_off_push://屏蔽离线消息推送
                viewModel.updatePushServiceForGroup(groupId, isChecked);
                break;
            case R.id.item_group_top ://消息置顶
                if(isChecked) {
                    conversation.setExtField(System.currentTimeMillis()+"");
                }else {
                    conversation.setExtField("");
                }
                LiveDataBus.get().with(DemoConstant.GROUP_CHANGE).postValue(EaseEvent.create(DemoConstant.GROUP_CHANGE, EaseEvent.TYPE.GROUP));
                break;
        }
    }

    private void showGroupNameDialog() {
        new EditTextDialogFragment.Builder(mContext)
                .setContent(group.getGroupName())
                .setConfirmClickListener(new EditTextDialogFragment.ConfirmClickListener() {
                    @Override
                    public void onConfirmClick(View view, String content) {
                        if(!TextUtils.isEmpty(content)) {
                            viewModel.setGroupName(groupId, content);
                            // TODO: 2021/4/21 在数据库执行更改队伍名称操作
                            executorService = Executors.newSingleThreadExecutor();
                            Future future = executorService.submit(new Callable() {
                                public Object call() throws Exception {
                                    if(DBUtil.updateTeam(myTeam.getTeamID(), "team_name", tvGroupName.getText().toString().trim())) {
                                        return "成功更改队伍名称: " + tvGroupName.getText().toString().trim();
                                    }
                                    return "更改队伍名称失败！";
                                }
                            });
                            try {
                                ToastUtils.showToast(String.valueOf(future.get()));
                                executorService.shutdown();
                            } catch (ExecutionException | InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .setTitle(R.string.em_chat_group_detail_name)
                .show();
    }

    private void showAnnouncementDialog() {
        GroupEditFragment.showDialog(mContext,
                getString(R.string.em_chat_group_detail_announcement),
                group.getAnnouncement(),
                getString(R.string.em_chat_group_detail_announcement_hint),
                GroupHelper.isAdmin(group) || GroupHelper.isOwner(group),
                new GroupEditFragment.OnSaveClickListener() {
                    @Override
                    public void onSaveClick(View view, String content) {
                        //修改群公告
                        viewModel.setGroupAnnouncement(groupId, content);
                    }
                });
    }

    private void showIntroductionDialog() {
        GroupEditFragment.showDialog(mContext,
                getString(R.string.em_chat_group_detail_introduction),
                group.getDescription(),
                getString(R.string.em_chat_group_detail_introduction_hint),
                GroupHelper.isAdmin(group) || GroupHelper.isOwner(group),
                new GroupEditFragment.OnSaveClickListener() {
                    @Override
                    public void onSaveClick(View view, String content) {
                        //修改群介绍
                        viewModel.setGroupDescription(groupId, content);
                        // TODO: 2021/4/21 在数据库执行更改队伍简介操作
                        executorService = Executors.newSingleThreadExecutor();
                        Future future = executorService.submit(new Callable() {
                            public Object call() throws Exception {
                                if(DBUtil.updateTeam(myTeam.getTeamID(), "description", content)) {
                                    return "成功更改队伍简介: " + content;
                                }
                                return "更改队伍简介失败！";
                            }
                        });
                        try {
                            ToastUtils.showToast(String.valueOf(future.get()));
                            executorService.shutdown();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

    private void showScheduleDialog() {
        GroupEditFragment.showDialog(mContext,
                getString(R.string.chat_group_detail_schedule),
                mSchedule.getScheduleInfo(),
                getString(R.string.chat_group_detail_schedule_hint),
                GroupHelper.isAdmin(group) || GroupHelper.isOwner(group),
                new GroupEditFragment.OnSaveClickListener() {
                    @Override
                    public void onSaveClick(View view, String content) {
                        // TODO: 2021/4/21 在数据库执行更改队伍行程表操作
                        executorService = Executors.newSingleThreadExecutor();
                        Future future = executorService.submit(new Callable() {
                            public Object call() throws Exception {
                                if(DBUtil.updateSchedule(myTeam.getScheduleID(), "schedule_info", content)) {
                                    return "成功更改队伍行程表: " + content;
                                }
                                return "更改队伍行程表失败！";
                            }
                        });
                        try {
                            ToastUtils.showToast(String.valueOf(future.get()));
                            executorService.shutdown();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                });
    }

//    private void showDepartureDatetimeDialog() {
//        new DatetimeDialogFragment.Builder(mContext)
//                .setContent(group.getGroupName())
//                .setConfirmClickListener(new DatetimeDialogFragment.ConfirmClickListener() {
//                    @Override
//                    public void onConfirmClick(View view, String content) {
//                        if(!TextUtils.isEmpty(content)) {
//                            viewModel.setGroupName(groupId, content);
//                            // TODO: 2021/4/21 在数据库执行更改队伍名称操作
//                            executorService = Executors.newSingleThreadExecutor();
//                            Future future = executorService.submit(new Callable() {
//                                public Object call() throws Exception {
//                                    if(DBUtil.deleteTeam(tvGroupName.getText().toString())) {
//                                        return "成功更改队伍名称: " + tvGroupName.getText().toString();
//                                    }
//                                    return "更改队伍名称失败！";
//                                }
//                            });
//                            try {
//                                ToastUtils.showToast(String.valueOf(future.get()));
//                                executorService.shutdown();
//                            } catch (ExecutionException | InterruptedException e) {
//                                e.printStackTrace();
//                            }
//                        }
//                    }
//                })
//                .setTitle(R.string.em_chat_group_detail_name)
//                .show();
//    }
    private void showDepartureDatetimeDialog() {
        new DatetimeDialogFragment.Builder(mContext)
                .setTitle(R.string.departure_datetime)
                .setConfirmColor(R.color.em_color_brand)
                .showCancelButton(true)
                .showMinute(true)
                .setDate(formatDate.format(myTeam.getDepartureDateTime()))
                .setTime(formatHourMinute.format(myTeam.getDepartureDateTime()))
                .setOnTimePickCancelListener(R.string.cancel, new DatetimeDialogFragment.OnTimePickCancelListener() {
                    @Override
                    public void onClickCancel(View view) {
                    }
                })
                .setOnDatePickCancelListener(R.string.cancel, new DatetimeDialogFragment.OnDatePickCancelListener() {
                    @Override
                    public void onClickCancel(View view) {

                    }
                })
                .setOnTimePickSubmitListener(R.string.confirm, new DatetimeDialogFragment.OnTimePickSubmitListener() {
                    @Override
                    public void onClickSubmit(View view, String time) {
                        departureTime = time;
                        isTimeUpdated = true;
                        mHandler.sendEmptyMessage(1);
                    }
                })
                .setOnDatePickSubmitListener(R.string.confirm, new DatetimeDialogFragment.OnDatePickSubmitListener() {
                    @Override
                    public void onClickSubmit(View view, String date) {
                        if(date ==null) {
                            departureDate = CommonUtils.dateToString(myTeam.getDepartureDateTime(), "yyyy-MM-dd ");
                        }else {
                            departureDate = date;
                        }
                        isDateUpdated = true;
                        mHandler.sendEmptyMessage(2);
                    }
                })
                .show();
    }

    private String getHour(String time) {
        return time.contains(":") ? time.substring(0, time.indexOf(":")) : time;
    }
    private String getTimeRange(int start, int end) {
        return getTimeToString(start) + "~" + getTimeToString(end);
    }

    private String getTimeToString(int hour) {
        return getDoubleDigit(hour) + ":00";
    }

    private String getDoubleDigit(int num) {
        return num > 10 ? String.valueOf(num) : "0" + num;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ADD_USER :
                    loadGroup();
                    break;
            }
        }
    }

    private void makeTextSingleLine(TextView tv) {
        tv.setMaxLines(1);
        tv.setEllipsize(TextUtils.TruncateAt.END);
    }

    @Override
    public void onBackPress(View view) {
        onBackPressed();
    }

    /**
     * 是否有邀请权限
     * @return
     */
    private boolean isCanInvite() {
        return GroupHelper.isCanInvite(group);
    }

    /**
     * 是否是管理员
     * @return
     */
    private boolean isAdmin() {
        return GroupHelper.isAdmin(group);
    }

    /**
     * 是否是群主
     * @return
     */
    private boolean isOwner() {
        return GroupHelper.isOwner(group);
    }



    private boolean getMyTeamInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Team t = DBUtil.selectTeam(tvGroupName.getText().toString());
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

    private boolean getScheduleInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Schedule s = DBUtil.selectSchedule(myTeam.getScheduleID());
                return s;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新行程表信息
                mSchedule = (Schedule) future.get();
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private class MyHandler extends Handler {
        public MyHandler() {

        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                case 2:
                    if(!isDateUpdated)
                        break;
                    else if(!isTimeUpdated)
                        break;
                    else if(isDateUpdated && isTimeUpdated) {
                        isDateUpdated = false;
                        isTimeUpdated = false;
                        // TODO: 2021/4/22 数据库更新出发时间
                        executorService = Executors.newSingleThreadExecutor();
                        Future future = executorService.submit(new Callable() {
                            public Object call() throws Exception {
                                Date departureDatetime =  CommonUtils.stringToDate(departureDate + departureTime, "yyyy-MM-dd HH:mm:ss");
                                if(DBUtil.updateTeam(myTeam.getTeamID(), "departure_datetime", departureDatetime)) {
                                    myTeam = DBUtil.selectTeam(tvGroupName.getText().toString());
                                    itemGroupDepartureDatetime.getTvContent().setText(CommonUtils.dateToString(myTeam.getDepartureDateTime(), "yyyy-MM-dd HH-mm-ss"));
                                    itemGroupDepartureDatetimeSetting.getTvContent().setText(CommonUtils.dateToString(myTeam.getDepartureDateTime(), "yyyy-MM-dd HH-mm-ss"));
                                    return "成功设置出发时间！\n" + formatDatetime.format(departureDatetime);
                                }
                                return "设置出发时间失败！";
                            }
                        });
                        try {
                            ToastUtils.showToast(String.valueOf(future.get()));
                            executorService.shutdown();
                        } catch (ExecutionException | InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
            }
        }
    }
}
