package com.zjnu.grouptour.fragment;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.constraintlayout.widget.ConstraintLayout;

import com.hyphenate.EMCallBack;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.R;
import com.hyphenate.easeim.common.widget.ArrowItemView;
import com.hyphenate.easeim.section.base.BaseInitFragment;
import com.hyphenate.easeim.section.dialog.DemoDialogFragment;
import com.hyphenate.easeim.section.dialog.SimpleDialogFragment;
import com.hyphenate.easeim.section.login.activity.LoginActivity;
import com.hyphenate.easeim.section.me.activity.AboutHxActivity;
import com.hyphenate.easeim.section.me.activity.DeveloperSetActivity;
import com.hyphenate.easeim.section.me.activity.FeedbackActivity;
import com.hyphenate.easeim.section.me.activity.SetIndexActivity;
import com.zjnu.grouptour.activity.BackgroundManageActivity;
import com.zjnu.grouptour.activity.UserDetailActivity;
import com.hyphenate.easeui.manager.EaseThreadManager;
import com.zjnu.grouptour.activity.UserInfoChangeActivity;

public class AboutMeFragment extends BaseInitFragment implements View.OnClickListener {
    private ConstraintLayout clUser;
    private TextView name;
    private ArrowItemView itemCommonSet;
    private ArrowItemView itemFeedback;
    private ArrowItemView itemAboutHx;
    private ArrowItemView itemDeveloperSet;
    private Button mBtnLogout;

    private ArrowItemView itemBackgroundManage, itemBroadcast;

    @Override
    protected int getLayoutId() {
        return R.layout.demo_fragment_about_me;
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
        mBtnLogout = findViewById(R.id.btn_logout);

        itemBackgroundManage = findViewById(R.id.item_background_manage);
        itemBroadcast = findViewById(R.id.item_broadcast);

        name.setText(DemoHelper.getInstance().getCurrentUser());
        if(DemoHelper.getInstance().getCurrentUser().equals("luchen")) {
//            itemDeveloperSet.setVisibility(View.VISIBLE);
            itemBackgroundManage.setVisibility(View.VISIBLE);
            itemBroadcast.setVisibility(View.VISIBLE);
        }

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

        itemBackgroundManage.setOnClickListener(this);
        itemBroadcast.setOnClickListener(this);
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
            case R.id.item_background_manage:
                BackgroundManageActivity.actionStart(mContext);
                break;
            case R.id.item_broadcast:
                UserInfoChangeActivity.actionStart(mContext, null, "broadcast");
                break;
        }
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
