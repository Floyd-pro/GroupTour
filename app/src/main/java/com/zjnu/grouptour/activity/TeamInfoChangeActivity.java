package com.zjnu.grouptour.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProvider;

import com.hyphenate.chat.EMPushConfigs;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.interfaceOrImplement.OnResourceParseCallback;
import com.hyphenate.easeim.common.livedatas.LiveDataBus;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.me.viewmodels.OfflinePushSetViewModel;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.zjnu.grouptour.R;
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
 * @Date 2021/4/20 20:30
 * @Description 更改队伍信息
 */
public class TeamInfoChangeActivity extends BaseInitActivity implements View.OnClickListener, TextWatcher {
    private EaseTitleBar titleBar;
    private EditText et_input;
    private TextView description;
    private Button btn_save;
    private OfflinePushSetViewModel viewModel;

    private Team myTeam;
    private String changeField;
    private ExecutorService executorService;

    public static void actionStart(Context context, Team team, String changeField) {
        Intent intent = new Intent(context, TeamInfoChangeActivity.class);
        intent.putExtra("team", team);
        intent.putExtra("changeField", changeField);
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(TeamInfoChangeActivity.this, true);
        myTeam = getIntent().getExtras().getParcelable("team");
        changeField = getIntent().getStringExtra("changeField");
        switch (changeField) {
            case "team_name":
                et_input.setHint(R.string.input_new_team_name_hint);
                description.setText(R.string.team_name_description);
                break;
            case "destination_name":
                et_input.setHint(R.string.input_new_destination_name_hint);
                description.setText(R.string.destination_name_description);
                break;
            case "principal_name":
                et_input.setHint(R.string.input_new_principal_name_hint);
                description.setText(R.string.principal_name_description);
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
        return R.layout.activity_team_info_change;
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
                    case "team_name":
                        showChangeTeamInfoResult(myTeam.getTeamID(), "team_name", et_input.getText().toString());
                        break;
                    case "destination_name":
                        showChangeTeamInfoResult(myTeam.getTeamID(), "destination_name", Integer.parseInt(et_input.getText().toString()));
                        break;
                    case "principal_name":
                        showChangeTeamInfoResult(myTeam.getTeamID(), "principal_name", et_input.getText().toString());
                        break;
                    default:
                        ToastUtils.showToast("目标更改字段不在范围内！");
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
                return DBUtil.updateTeam(id, key, value);
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
                return DBUtil.updateTeam(id, key, value);
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

    private void showChangeTeamInfoResult(int id, String key, int value) {
        if(updateInfo(id, key, value))
            ToastUtils.showToast("保存成功！");
        else
            ToastUtils.showToast("保存失败，请咨询管理员！");
    }

    private void showChangeTeamInfoResult(int id, String key, String value) {
        if(updateInfo(id, key, value))
            ToastUtils.showToast("保存成功！");
        else
            ToastUtils.showToast("保存失败，请咨询管理员！");
    }
}

