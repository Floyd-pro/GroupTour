package com.zjnu.grouptour.activity;

/**
 * @author luchen
 * @Date 2021/4/17 21:02
 * @Description 更改用户信息
 */
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMPushConfigs;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.MainActivity;
import com.zjnu.grouptour.R;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.me.viewmodels.OfflinePushSetViewModel;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.utils.DBUtil;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class UserInfoChangeActivity extends BaseInitActivity implements OnClickListener, TextWatcher {
    private EaseTitleBar titleBar;
    private EditText et_input;
    private TextView description;
    private Button btn_save;
    private OfflinePushSetViewModel viewModel;

    private Person me;
    private String changeField;
    private ExecutorService executorService;

    public static void actionStart(Context context, Person person, String changeField) {
        Intent intent = new Intent(context, UserInfoChangeActivity.class);
        intent.putExtra("person", person);
        intent.putExtra("changeField", changeField);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(UserInfoChangeActivity.this, true);
        me = getIntent().getExtras().getParcelable("person");
        changeField = getIntent().getStringExtra("changeField");
        switch (changeField) {
            case "nickname":
                et_input.setHint(R.string.input_new_nick_hint);
                description.setText(R.string.nick_description);
                break;
            case "tel":
                et_input.setHint(R.string.input_new_tel_hint);
                description.setText(R.string.tel_description);
                break;
            case "wechat":
                et_input.setHint(R.string.input_new_wechat_hint);
                description.setText(R.string.wechat_description);
                break;
            case "qq":
                et_input.setHint(R.string.input_new_qq_hint);
                description.setText(R.string.qq_description);
                break;
            case "gender":
                et_input.setHint(R.string.input_new_gender_hint);
                description.setText(R.string.gender_description);
                break;
            case "signature":
                et_input.setHint(R.string.input_new_signature_hint);
                description.setText(R.string.signature_description);
                break;
            case "email":
                et_input.setHint(R.string.input_new_email_hint);
                description.setText(R.string.email_description);
                break;
            case "real_name":
                et_input.setHint(R.string.input_new_real_name_hint);
                description.setText(R.string.real_name_description);
                break;
            case "identity_num":
                et_input.setHint(R.string.input_new_identity_num_hint);
                description.setText(R.string.identity_num_description);
                break;
            case "broadcast":
                et_input.setHint(R.string.input_broadcast_hint);
                description.setText(R.string.broadcast_description);
                titleBar.setTitle("系统广播");
                btn_save.setText("发送");
                break;
            default:
                break;
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    protected void onPause() {
        super.onPause();
        executorService.shutdown();
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_user_info_change;
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        titleBar = findViewById(R.id.title_bar);
        et_input = (EditText) findViewById(R.id.et_input_info);
        btn_save = (Button) findViewById(R.id.btn_save);
        description = (TextView) findViewById(R.id.tv_info_description);
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
        btn_save.setOnClickListener(this);
        et_input.addTextChangedListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_save:
                switch (changeField) {
                    case "nickname":
                        showChangeUserInfoResult(me.getId(), "nickname", et_input.getText().toString());
                        DemoHelper.getInstance().getUserProfileManager().updateCurrentUserNickName(et_input.getText().toString());
                        break;
                    case "tel":
                        showChangeUserInfoResult(me.getId(), "tel", Integer.parseInt(et_input.getText().toString()));
                        break;
                    case "wechat":
                        showChangeUserInfoResult(me.getId(), "wechat", et_input.getText().toString());
                        break;
                    case "qq":
                        showChangeUserInfoResult(me.getId(), "qq", Integer.parseInt(et_input.getText().toString()));
                        break;
                    case "gender":
                        showChangeUserInfoResult(me.getId(), "gender", et_input.getText().toString());
                        break;
                    case "signature":
                        showChangeUserInfoResult(me.getId(), "signature", et_input.getText().toString());
                        break;
                    case "email":
                        showChangeUserInfoResult(me.getId(), "email", et_input.getText().toString());
                        break;
                    case "real_name":
                        showChangeUserInfoResult(me.getId(), "real_name", et_input.getText().toString());
                        break;
                    case "identity_number":
                        showChangeUserInfoResult(me.getId(), "identity_number", et_input.getText().toString());
                        break;
                    case "broadcast":
                        // TODO: 2021/5/3 向全体用户发送广播
                        ToastUtils.showToast("该功能正在开发中，敬请期待~");
                        break;
                    default:
                        ToastUtils.showToast("目标字段不在范围内！");
                        break;
                }
                break;
            default:
                break;
        }
//        viewModel.updatePushNickname(et_input.getText().toString());
    }

    @Override
    protected void initData() {
        super.initData();
        viewModel = new ViewModelProvider(this).get(OfflinePushSetViewModel.class);
        viewModel.getConfigsObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<EMPushConfigs>() {
                @Override
                public void onSuccess(EMPushConfigs data) {
                    if(data != null && !TextUtils.isEmpty(data.getDisplayNickname())) {
                        et_input.setText(data.getDisplayNickname());
                    }
                }
            });
        });
        viewModel.getUpdatePushNicknameObservable().observe(this, response -> {
            parseResource(response, new OnResourceParseCallback<Boolean>() {
                @Override
                public void onSuccess(Boolean data) {
                    showToast(R.string.demo_offline_nickname_update_success);
                    LiveDataBus.get().with(DemoConstant.REFRESH_NICKNAME).postValue(true);
                    finish();
                }

                @Override
                public void onLoading(Boolean data) {
                    super.onLoading(data);
                    showLoading();
                }

                @Override
                public void hideLoading() {
                    super.hideLoading();
                    dismissLoading();
                }
            });
        });
        viewModel.getPushConfigs();
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {
        if (s.toString().length() > 0) {
            description.setTextColor(Color.RED);
        }else{
            description.setTextColor(Color.parseColor("#cccccc"));
        }
    }

    @Override
    public void afterTextChanged(Editable s) {

    }

    private boolean updateInfo(int id, String key, int value) {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                return DBUtil.updatePerson(id, key, value);
            }
        });
        try {
            if((Boolean)future.get()) {
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean updateInfo(int id, String key, String value) {
        Future future = executorService.submit(new Callable() {
            public Object call() throws Exception {
                return DBUtil.updatePerson(id, key, value);
            }
        });
        try {
            if((Boolean)future.get()) {
                return true;
            }
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void showChangeUserInfoResult(int id, String key, int value) {
        if(updateInfo(id, key, value))
            ToastUtils.showToast("保存成功！");
        else
            ToastUtils.showToast("保存失败，请咨询管理员！");
    }

    private void showChangeUserInfoResult(int id, String key, String value) {
        if(updateInfo(id, key, value))
            ToastUtils.showToast("保存成功！");
        else
            ToastUtils.showToast("保存失败，请咨询管理员！");
    }
}

