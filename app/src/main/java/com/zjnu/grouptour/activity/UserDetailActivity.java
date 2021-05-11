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
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.widget.ArrowItemView;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.hyphenate.exceptions.HyphenateException;
import com.zjnu.grouptour.api.GroupTourApi;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserDetailActivity extends BaseInitActivity implements View.OnClickListener {
    private EaseTitleBar titleBar;
    private ArrowItemView itemHxId;
    private ArrowItemView itemNickname;

    private ArrowItemView itemTel, itemWechat, itemQq, itemMore;
    private Person me;
    private ExecutorService executorService;

    private boolean isFriend; // 是否从好友界面进入

    public static void actionStart(Context context) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        context.startActivity(intent);
    }

    // 好友信息入口
    public static void actionStart(Context context, String name) {
        Intent intent = new Intent(context, UserDetailActivity.class);
        intent.putExtra("friendName", name);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(UserDetailActivity.this, true);
    }

    @Override
    protected void onResume() {
        super.onResume();
        executorService = Executors.newSingleThreadExecutor();
        if(GroupTourApi.getInstance().isOpenLogger() && getMyInfo())
            ToastUtils.showToast("个人资料获取成功！");
        else
            ToastUtils.showToast("个人资料获取失败！");
    }

    @Override
    protected void onPause() {
        super.onPause();
        executorService.shutdown();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_detail;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        itemHxId = findViewById(R.id.item_hx_id);
        itemNickname = findViewById(R.id.item_nickname);

        itemTel = findViewById(R.id.item_tel);
        itemWechat = findViewById(R.id.item_wechat);
        itemQq = findViewById(R.id.item_qq);
        itemMore = findViewById(R.id.item_more);
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
        itemNickname.setOnClickListener(this);
        itemTel.setOnClickListener(this);
        itemWechat.setOnClickListener(this);
        itemQq.setOnClickListener(this);
        itemMore.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.item_nickname:
                Person person = new Person(me.getId(), me.getName(), me.getPsw(),
                        me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                        me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                        me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "nickname");
                break;
            case R.id.item_tel:
                person = new Person(me.getId(), me.getName(), me.getPsw(),
                        me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                        me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                        me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "tel");
                break;
            case R.id.item_wechat:
                person = new Person(me.getId(), me.getName(), me.getPsw(),
                        me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                        me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                        me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "wechat");
                break;
            case R.id.item_qq:
                person = new Person(me.getId(), me.getName(), me.getPsw(),
                        me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                        me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                        me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "qq");
                break;
            case R.id.item_more:
                person = new Person(me.getName());
                if(!isFriend)
                    MoreUserInfoActivity.actionStart(mContext, person, false);
                else
                    MoreUserInfoActivity.actionStart(mContext, person, true);
                break;
            default:
                break;
        }
    }

    @Override
    protected void initData() {
        super.initData();
        if(getIntent().hasExtra("friendName")) { // 好友信息初始化
            isFriend = true;
            itemHxId.getTvContent().setText(getIntent().getStringExtra("friendName"));
            itemNickname.setArrowVisible(false);
            itemNickname.setClickable(false);
            itemTel.setArrowVisible(false);
            itemTel.setClickable(false);
            itemWechat.setArrowVisible(false);
            itemWechat.setClickable(false);
            itemQq.setArrowVisible(false);
            itemQq.setClickable(false);
        }
        else { // 本人信息初始化
            isFriend = false;
            itemHxId.getTvContent().setText(DemoHelper.getInstance().getCurrentUser());
            getNickname();
            LiveDataBus.get().with(DemoConstant.REFRESH_NICKNAME, Boolean.class).observe(this, event -> {
                if(event == null) {
                    return;
                }
                if(event) { // 这里是进不去的
                    getNickname();
                }
            });
        }
    }

    private void getNickname() {
        EMPushConfigs configs = null;
        try {
            configs = EMClient.getInstance().pushManager().getPushConfigsFromServer();
            String nickname = configs.getDisplayNickname();
            if(!TextUtils.isEmpty(nickname)) {
                itemNickname.getTvContent().setText(nickname);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    private boolean getMyInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Person p = DBUtil.selectPerson(itemHxId.getTvContent().getText().toString());
                return p;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新用户信息
                me = (Person) future.get();
                itemTel.getTvContent().setText(me.getTel());
                itemWechat.getTvContent().setText(me.getWechat());
                itemQq.getTvContent().setText(String.valueOf(me.getQq()));
                itemNickname.getTvContent().setText(String.valueOf(me.getNickname()));
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
