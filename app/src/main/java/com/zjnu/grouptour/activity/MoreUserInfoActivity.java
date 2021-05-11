package com.zjnu.grouptour.activity;


import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMPushConfigs;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.me.activity.AccountSecurityActivity;
import com.hyphenate.easeim.section.me.activity.CommonSettingsActivity;
import com.hyphenate.easeim.section.me.activity.MessageReceiveSetActivity;
import com.hyphenate.easeim.section.me.activity.PrivacyIndexActivity;
import com.hyphenate.exceptions.HyphenateException;
import com.zjnu.grouptour.R;
import com.hyphenate.easeim.common.widget.ArrowItemView;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.login.activity.LoginActivity;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author luchen
 * @Date 2021/4/18 0:11
 * @Description 个人资料更多界面
 */
public class MoreUserInfoActivity extends BaseInitActivity implements View.OnClickListener {
    private EaseTitleBar titleBar;
    private Button btnLogout;

    private ArrowItemView itemGender, itemSignature, itemEmail, itemRealName, itemIdentityNum;
    private Person me;
    private boolean isFriend;
    private ExecutorService executorService;

//    public static void actionStart(Context context, Person person) {
//        Intent intent = new Intent(context, MoreUserInfoActivity.class);
//        intent.putExtra("person", person);
//        context.startActivity(intent);
//    }

    public static void actionStart(Context context, Person person, boolean isFriend) {
        Intent intent = new Intent(context, MoreUserInfoActivity.class);
        intent.putExtra("person", person);
        intent.putExtra("isFriend", isFriend);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(MoreUserInfoActivity.this, true);
        me = getIntent().getExtras().getParcelable("person");
        isFriend = getIntent().getBooleanExtra("isFriend", false);
    }

    @Override
    protected void onResume() {
        super.onResume();
        executorService = Executors.newSingleThreadExecutor();
        if(getMyInfo())
            ToastUtils.showToast("更多信息获取成功！");
        else
            ToastUtils.showToast("更多信息获取失败！");
    }

    @Override
    protected void onPause() {
        super.onPause();
        executorService.shutdown();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_more_user_info;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        itemGender = findViewById(R.id.item_gender);
        itemSignature = findViewById(R.id.item_signature);
        itemEmail = findViewById(R.id.item_email);
        itemRealName = findViewById(R.id.item_real_name);
        itemIdentityNum = findViewById(R.id.item_identity_num);
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
        itemGender.setOnClickListener(this);
        itemSignature.setOnClickListener(this);
        itemEmail.setOnClickListener(this);
        itemRealName.setOnClickListener(this);
        itemIdentityNum.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            // TODO: 2021/4/18 性别和个性签名界面待优化
            case R.id.item_gender:
                Person person = new Person(me.getId(), me.getName(), me.getPsw(),
                        me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                        me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                        me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "gender");
                break;
            case R.id.item_signature:
                person = new Person(me.getId(), me.getName(), me.getPsw(),
                        me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                        me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                        me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "signature");
                break;
            case R.id.item_email:
                person = new Person(me.getId(), me.getName(), me.getPsw(),
                    me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                    me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                    me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "email");
                break;
            case R.id.item_identity_num:
                person = new Person(me.getId(), me.getName(), me.getPsw(),
                        me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                        me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                        me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "identity_number");
                break;
            case R.id.item_real_name:
                person = new Person(me.getId(), me.getName(), me.getPsw(),
                        me.getLongitude(), me.getLatitude(), me.getTeamID(), me.getGender(),
                        me.getNickname(), me.getTel(), me.getWechat(), me.getQq(),
                        me.getEmail(), me.getSignature(), me.getRealName(), me.getIdentityNum());
                UserInfoChangeActivity.actionStart(mContext, person, "real_name");
                break;
            default:
                break;
        }
    }

    private boolean getMyInfo() {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                Person p = DBUtil.selectPerson(me.getName());
                return p;
            }
        });
        try {
            if(future.get() != null) {
                // TODO: 2021/4/17 更新用户信息
                me = (Person) future.get();
                itemGender.getTvContent().setText(me.getGender());
                itemSignature.getTvContent().setText(me.getSignature());
                itemEmail.getTvContent().setText(me.getEmail());
                if(!isFriend) {
                    itemRealName.getTvContent().setText(me.getRealName());
                    itemIdentityNum.getTvContent().setText(me.getIdentityNum());
                }else {
                    itemGender.setArrowVisible(false);
                    itemGender.setClickable(false);
                    itemSignature.setArrowVisible(false);
                    itemSignature.setClickable(false);
                    itemEmail.setArrowVisible(false);
                    itemEmail.setClickable(false);
                    itemRealName.setClickable(false);
                    itemIdentityNum.setClickable(false);
                    itemRealName.setVisibility(View.GONE);
                    itemIdentityNum.setVisibility(View.GONE);
                }
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
