package com.zjnu.grouptour;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import com.baidu.mapapi.SDKInitializer;
import com.google.android.material.bottomnavigation.BottomNavigationItemView;
import com.google.android.material.bottomnavigation.BottomNavigationMenuView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.hyphenate.easecallkit.base.EaseCallType;
import com.hyphenate.easecallkit.ui.EaseMultipleVideoActivity;
import com.hyphenate.easecallkit.ui.EaseVideoCallActivity;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.HMSPushHelper;
import com.hyphenate.easeim.common.constant.DemoConstant;
import com.hyphenate.easeim.common.enums.SearchType;
import com.hyphenate.easeim.common.permission.PermissionsManager;
import com.hyphenate.easeim.common.permission.PermissionsResultAction;
import com.hyphenate.easeim.common.utils.PushUtils;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.MainViewModel;
import com.hyphenate.easeim.section.base.BaseInitActivity;
import com.hyphenate.easeim.section.chat.ChatPresenter;
import com.hyphenate.easeim.section.contact.activity.AddContactActivity;
import com.hyphenate.easeim.section.contact.activity.GroupContactManageActivity;
import com.hyphenate.easeim.section.contact.fragment.ContactListFragment;
import com.hyphenate.easeim.section.contact.viewmodels.ContactsViewModel;
import com.hyphenate.easeim.section.conversation.ConversationListFragment;
import com.zjnu.grouptour.bean.PlusBean;
import com.zjnu.grouptour.fragment.HomeFragment;
import com.hyphenate.easeim.section.group.activity.GroupPrePickActivity;
import com.zjnu.grouptour.fragment.AboutMeFragment;
import com.hyphenate.easeui.model.EaseEvent;
import com.hyphenate.easeui.ui.base.EaseBaseFragment;
import com.hyphenate.easeui.widget.EaseTitleBar;
import com.zjnu.grouptour.dialog.PlusBottomSheetDialog;
import com.zjnu.grouptour.utils.StatusBarUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;


public class MainActivity extends BaseInitActivity implements BottomNavigationView.OnNavigationItemSelectedListener {
    private BottomNavigationView navView;
    private EaseTitleBar mTitleBar;
    private EaseBaseFragment mConversationListFragment, mFriendsFragment, mHomeFragment, mAboutMeFragment;
    private EaseBaseFragment mCurrentFragment;
    private TextView mTvMainHomeMsg, mTvMainFriendsMsg, mTvMainDiscoverMsg, mTvMainAboutMeMsg;
    private int[] badgeIds = {R.layout.demo_badge_discover, R.layout.demo_badge_home, R.layout.demo_badge_friends, R.layout.demo_badge_about_me};
    private int[] msgIds = {R.id.tv_main_discover_msg, R.id.tv_main_home_msg, R.id.tv_main_friends_msg, R.id.tv_main_about_me_msg};
    private MainViewModel viewModel;
    private boolean showMenu = true;//?????????????????????

    private Handler mHandler;

    private SDKReceiver mReceiver;
    private String permissionInfo;
    private final int SDK_PERMISSION_REQUEST = 0;

    private boolean isImgPlusFirstClicked = false;
    private ImageView imgPlus;
    private PlusBottomSheetDialog plusBottomSheetDialog;
    private ArrayList<PlusBean> list = new ArrayList<PlusBean>();

    public static void startAction(Context context) {
        Intent starter = new Intent(context, MainActivity.class);
        context.startActivity(starter);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if(mCurrentFragment != null) {
            if(mCurrentFragment instanceof ContactListFragment) {
                menu.findItem(R.id.action_group).setVisible(false);
                menu.findItem(R.id.action_friend).setVisible(false);
                menu.findItem(R.id.action_search_friend).setVisible(true);
                menu.findItem(R.id.action_search_group).setVisible(true);
            }else {
                menu.findItem(R.id.action_group).setVisible(true);
                menu.findItem(R.id.action_friend).setVisible(true);
                menu.findItem(R.id.action_search_friend).setVisible(false);
                menu.findItem(R.id.action_search_group).setVisible(false);
            }
        }
        return showMenu;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.demo_conversation_menu, menu);
        return true;
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StatusBarUtils.setTranslucentStatusTextMode(MainActivity.this, true);

        mHandler = new MyHandler(MainActivity.this);

        // ?????? SDK ???????????????
        IntentFilter iFilter = new IntentFilter();
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK);
        iFilter.addAction(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR);
        iFilter.addAction(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR);
        mReceiver = new SDKReceiver();
        registerReceiver(mReceiver, iFilter);

        list.add(new PlusBean("????????????", "????????????", "????????????", "????????????",  getDrawable(R.drawable.plus_open_map), getDrawable(R.drawable.plus_free_navigation), getDrawable(R.drawable.plus_weather), getDrawable(R.drawable.plus_travel_note)));
        list.add(new PlusBean("????????????", "????????????", getDrawable(R.drawable.plus_publish), getDrawable(R.drawable.plus_more)));
        plusBottomSheetDialog = new PlusBottomSheetDialog(MainActivity.this, list);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_video :
                break;
            case R.id.action_group :
                GroupPrePickActivity.actionStart(mContext);
                break;
            case R.id.action_friend :
            case R.id.action_search_friend :
                AddContactActivity.startAction(mContext, SearchType.CHAT);
                break;
            case R.id.action_search_group :
                GroupContactManageActivity.actionStart(mContext, true);
                break;
            case R.id.action_scan :
                showToast("?????????");
                break;
        }
        return true;
    }

    /**
     * ??????menu???icon????????????????????????menu???icon??????
     * @param featureId
     * @param menu
     * @return
     */
    @Override
    public boolean onMenuOpened(int featureId, Menu menu) {
        if(menu != null) {
            if(menu.getClass().getSimpleName().equalsIgnoreCase("MenuBuilder")) {
                try {
                    Method method = menu.getClass().getDeclaredMethod("setOptionalIconsVisible", Boolean.TYPE);
                    method.setAccessible(true);
                    method.invoke(menu, true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }
        return super.onMenuOpened(featureId, menu);
    }

    @Override
    protected void initSystemFit() {
        setFitSystemForTheme(false);
    }

    @Override
    protected void initView(Bundle savedInstanceState) {
        super.initView(savedInstanceState);
        navView = findViewById(R.id.nav_view);
        imgPlus = findViewById(R.id.img_plus);
        mTitleBar = findViewById(R.id.title_bar_main);
        mTitleBar.getToolbar().setVisibility(View.GONE); // ??????????????? ??????????????????????????????
        navView.setItemIconTintList(null);
        // ??????????????????????????????tab
//        navView.getMenu().findItem(R.id.em_main_nav_me).setVisible(false);
//        switchToConversation();
        switchToHome();
        checkIfShowSavedFragment(savedInstanceState);
        addTabBadge();
    }

    @Override
    protected void initListener() {
        super.initListener();
        navView.setOnNavigationItemSelectedListener(this);
    }

    @Override
    protected void initData() {
        super.initData();
        initViewModel();
        requestPermissions();
        checkUnreadMsg();
        ChatPresenter.getInstance().init();
        // ???????????? HMS ?????? token
        HMSPushHelper.getInstance().getHMSToken(this);

        //???????????????????????????
        if(PushUtils.isRtcCall){
            if (EaseCallType.getfrom(PushUtils.type) != EaseCallType.CONFERENCE_CALL) {
                    EaseVideoCallActivity callActivity = new EaseVideoCallActivity();
                    Intent intent = new Intent(getApplicationContext(), callActivity.getClass()).addFlags(FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
                } else {
                    EaseMultipleVideoActivity callActivity = new EaseMultipleVideoActivity();
                    Intent intent = new Intent(getApplication().getApplicationContext(), callActivity.getClass()).addFlags(FLAG_ACTIVITY_NEW_TASK);
                getApplicationContext().startActivity(intent);
            }
            PushUtils.isRtcCall  = false;
        }
    }

    private void initViewModel() {
        viewModel = new ViewModelProvider(mContext).get(MainViewModel.class);
        viewModel.getSwitchObservable().observe(this, response -> {
            if(response == null || response == 0) {
                return;
            }
            if(response == R.string.em_main_title_me) { // ?????????response?????????
                mTitleBar.setVisibility(View.GONE);
            }else {
                mTitleBar.setVisibility(View.VISIBLE);
                mTitleBar.setTitle(getResources().getString(response));
            }
        });

        viewModel.homeUnReadObservable().observe(this, readCount -> {
            if(!TextUtils.isEmpty(readCount)) {
                mTvMainHomeMsg.setVisibility(View.VISIBLE);
                mTvMainHomeMsg.setText(readCount);
            }else {
                mTvMainHomeMsg.setVisibility(View.GONE);
            }
        });
        //???????????????
        ContactsViewModel contactsViewModel = new ViewModelProvider(mContext).get(ContactsViewModel.class);
        contactsViewModel.loadContactList();

        viewModel.messageChangeObservable().with(DemoConstant.GROUP_CHANGE, EaseEvent.class).observe(this, this::checkUnReadMsg);
        viewModel.messageChangeObservable().with(DemoConstant.NOTIFY_CHANGE, EaseEvent.class).observe(this, this::checkUnReadMsg);
        viewModel.messageChangeObservable().with(DemoConstant.MESSAGE_CHANGE_CHANGE, EaseEvent.class).observe(this, this::checkUnReadMsg);

        viewModel.messageChangeObservable().with(DemoConstant.CONVERSATION_DELETE, EaseEvent.class).observe(this, this::checkUnReadMsg);
        viewModel.messageChangeObservable().with(DemoConstant.CONTACT_CHANGE, EaseEvent.class).observe(this, this::checkUnReadMsg);
        viewModel.messageChangeObservable().with(DemoConstant.CONVERSATION_READ, EaseEvent.class).observe(this, this::checkUnReadMsg);

    }

    private void checkUnReadMsg(EaseEvent event) {
        if(event == null) {
            return;
        }
        viewModel.checkUnreadMsg();
    }

    /**
     * ??????BottomNavigationView?????????item??????????????????
     */
    private void addTabBadge() {
        BottomNavigationMenuView menuView = (BottomNavigationMenuView) navView.getChildAt(0);
        int childCount = menuView.getChildCount();
        Log.e("TAG", "bottom child count = "+childCount);
        BottomNavigationItemView itemTab;
        for(int i = 0; i < childCount; i++) {
            if(i == 2) break;
            itemTab = (BottomNavigationItemView) menuView.getChildAt(i);
            View badge = LayoutInflater.from(mContext).inflate(badgeIds[i], menuView, false);
            switch (i) {
                case 0 :
                    mTvMainDiscoverMsg = badge.findViewById(msgIds[0]); // ??????
                    break;
                case 1 :
                    mTvMainHomeMsg = badge.findViewById(msgIds[1]); // ??????
                    break;
//                case 2 :                                            // plus????????????????????????imgPlus??????
//                    break;
                case 3 :
                    mTvMainFriendsMsg = badge.findViewById(msgIds[2]); // ?????????
                    break;
                case 4 :
                    mTvMainAboutMeMsg = badge.findViewById(msgIds[3]); // ???
                    break;
//                case 0 :
//                    mTvMainHomeMsg = badge.findViewById(msgIds[0]);
//                    break;
//                case 1 :
//                    mTvMainFriendsMsg = badge.findViewById(msgIds[1]);
//                    break;
//                case 2 :
//                    mTvMainDiscoverMsg = badge.findViewById(msgIds[2]);
//                    break;
//                case 3 :
//                    mTvMainAboutMeMsg = badge.findViewById(msgIds[3]);
//                    break;
            }
            itemTab.addView(badge);
        }
    }

    /**
     * ?????????????????????????????????Fragment
     * @param savedInstanceState
     */
    private void checkIfShowSavedFragment(Bundle savedInstanceState) {
        if(savedInstanceState != null) {
            String tag = savedInstanceState.getString("tag");
            if(!TextUtils.isEmpty(tag)) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(tag);
                if(fragment instanceof EaseBaseFragment) {
                    replace((EaseBaseFragment) fragment, tag);
                }
            }
        }
    }

    private void switchToHome() {
        if(mHomeFragment == null) {
            mHomeFragment = new HomeFragment();
        }
        replace(mHomeFragment, "home");
    }

    private void switchToConversation() {
        if(mConversationListFragment == null) {
            mConversationListFragment = new ConversationListFragment();
        }
        replace(mConversationListFragment, "conversation");
    }

    private void switchToFriends() {
        if(mFriendsFragment == null) {
            mFriendsFragment = new ContactListFragment();
        }
        replace(mFriendsFragment, "contact");
    }

    private void switchToAboutMe() {
        if(mAboutMeFragment == null) {
            mAboutMeFragment = new AboutMeFragment();
        }
        replace(mAboutMeFragment, "me");
    }

    private void replace(EaseBaseFragment fragment, String tag) {
        if(mCurrentFragment != fragment) {
            FragmentTransaction t = getSupportFragmentManager().beginTransaction();
            if(mCurrentFragment != null) {
                t.hide(mCurrentFragment);
            }
            mCurrentFragment = fragment;
            if(!fragment.isAdded()) {
                t.add(R.id.fl_main_fragment, fragment, tag).show(fragment).commit();
            }else {
                t.show(fragment).commit();
            }
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        mTitleBar.setVisibility(View.VISIBLE);
        if(mTitleBar.getToolbar().getVisibility() == View.GONE) // initView()????????????gone????????????????????????????????????????????????visible
            mTitleBar.getToolbar().setVisibility(View.VISIBLE);
        showMenu = true;
        boolean showNavigation = false;
        switch (menuItem.getItemId()) {
            case R.id.em_main_nav_discover :
                switchToHome();
                mTitleBar.setTitle(getResources().getString(R.string.main_home));
                showMenu = false;
                showNavigation = true;
                break;
            case R.id.em_main_nav_home :
                switchToConversation();
                mTitleBar.setTitle(getResources().getString(R.string.em_main_title_home));
                showNavigation = true;
                break;
            case R.id.em_main_nav_plus :
                ToastUtils.showToast("??????????????????????????????");
                showNavigation = true;
                break;
            case R.id.em_main_nav_friends :
                switchToFriends();
                mTitleBar.setTitle(getResources().getString(R.string.em_main_title_friends));
                showNavigation = true;
                invalidateOptionsMenu();
                break;
            case R.id.em_main_nav_me :
                switchToAboutMe();
                mTitleBar.setTitle(getResources().getString(R.string.em_main_title_me));
                showMenu = false; // ????????????????????????????????????????????????????????????
                showNavigation = true;
                break;
        }
        invalidateOptionsMenu();
        return showNavigation;
    }

    private void checkUnreadMsg() {
        viewModel.checkUnreadMsg();
    }

    @Override
    protected void onResume() {
        super.onResume();
        DemoHelper.getInstance().showNotificationPermissionDialog();
        if(plusBottomSheetDialog.isShowing())
            plusBottomSheetDialog.hide();

        mHandler.sendEmptyMessage(1);
        /**
         * ??????????????? R.id.img_plus ?????????
         * ????????????????????????????????????????????????????????????????????????
         * ??????????????????????????????????????????????????????????????????????????? onNavigationItemSelected(MenuItem)???
         * ???????????? imgPlusClick(View)???
         * ???????????? R.id.img_plus ??????????????????????????????CardFragment?????????????????????????????????
         * (onResume?????????2?????????1???getViewHeight(navView) == 0?????????????????????????????????????????????????????????????????????)
         * PS?????????????????????? ???: ?????????????????????????????????????????????????????????Demo??????????????????????????????????????????????????????????????????
         */
//        if(getViewWidth(navView) != 0)
//            setViewLayoutParams(imgPlus, getViewWidth(navView) / 5, getViewHeight(navView));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if(mCurrentFragment != null) {
            outState.putString("tag", mCurrentFragment.getTag());
        }
    }

    // add by luchen
    public void imgPlusClick(View v) {
        // TODO: 2021/4/15 ??????BottomSheetDialog
        plusBottomSheetDialog.setOwnerActivity(this);
        plusBottomSheetDialog.show();
        if(!isImgPlusFirstClicked) {
            isImgPlusFirstClicked = true;
            ToastUtils.showToast("???????????????????????????~");
        }
    }

    /**
     * ????????????
     */
    private void requestPermissions() {
        PermissionsManager.getInstance()
                .requestAllManifestPermissionsIfNecessary(mContext, new PermissionsResultAction() {
                    @Override
                    public void onGranted() {

                    }

                    @Override
                    public void onDenied(String permission) {
                        ToastUtils.showToast(permission);
                    }
                });

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//
//            ArrayList<String> permissionsList = new ArrayList<String>();
//
//            /***
//             * ??????????????????????????????????????????????????????????????????????????????
//             */
//            // ??????????????????
//            if (checkSelfPermission(android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                permissionsList.add(android.Manifest.permission.ACCESS_FINE_LOCATION);
//            }
//            if (checkSelfPermission(android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
//                permissionsList.add(android.Manifest.permission.ACCESS_COARSE_LOCATION);
//            }
//
//            String[] permissions = {
//                    android.Manifest.permission.ACCESS_NETWORK_STATE,
//                    android.Manifest.permission.INTERNET,
//                    android.Manifest.permission.ACCESS_WIFI_STATE,
//                    android.Manifest.permission.CHANGE_WIFI_STATE,
//                    android.Manifest.permission.CHANGE_WIFI_MULTICAST_STATE,
//                    android.Manifest.permission.RECORD_AUDIO,
//                    android.Manifest.permission.MODIFY_AUDIO_SETTINGS,
//                    android.Manifest.permission.WRITE_SETTINGS,
//                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                    Manifest.permission.READ_EXTERNAL_STORAGE,
//            };
//            for (String perm : permissions) {
//                if (PackageManager.PERMISSION_GRANTED != checkSelfPermission(perm)) {
//                    permissionsList.add(perm);
//                    // ?????????????????????????????????.
//                }
//            }
//            /*
//             * ????????????????????????????????????????????????(????????????)???????????????????????????????????????????????????????????????
//             */
//            // ????????????
//            if (addPermission(permissionsList, android.Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
//                permissionInfo += "Manifest.permission.WRITE_EXTERNAL_STORAGE Deny \n";
//            }
//            if (addPermission(permissionsList, android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
//                permissionInfo += "Manifest.permission.READ_EXTERNAL_STORAGE Deny \n";
//            }
//            // ????????????????????????
//            if (addPermission(permissionsList, Manifest.permission.READ_PHONE_STATE)) {
//                permissionInfo += "Manifest.permission.READ_PHONE_STATE Deny \n";
//            }
//
////            ToastUtils.showToast(permissionInfo);
//
//            if (permissionsList.size() > 0) {
//                requestPermissions(permissionsList.toArray(new String[permissionsList.size()]), SDK_PERMISSION_REQUEST);
//            }
//        }
    }

    @TargetApi(23)
    private boolean addPermission(ArrayList<String> permissionsList, String permission) {
        // ????????????????????????????????????,?????????????????????,??????????????????
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
     * ?????????????????????????????? SDK key ??????????????????????????????
     */
    public class SDKReceiver extends BroadcastReceiver {

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (TextUtils.isEmpty(action)) {
                return;
            }
            if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_ERROR)) {
                Log.e("luchen", "key ????????????! ????????? :" + intent.getIntExtra
                        (SDKInitializer.SDK_BROADTCAST_INTENT_EXTRA_INFO_KEY_ERROR_CODE, 0)
                        +  " ; ?????? AndroidManifest.xml ??????????????? key ??????");
            } else if (action.equals(SDKInitializer.SDK_BROADTCAST_ACTION_STRING_PERMISSION_CHECK_OK)) {
                Log.e("luchen", "key ????????????! ????????????????????????");
            } else if (action.equals(SDKInitializer.SDK_BROADCAST_ACTION_STRING_NETWORK_ERROR)) {
                Log.e("luchen", "????????????");
            }
        }
    }

    // add by luchen
    /**
     * ???ConstraintLayout???????????? view ?????????
     */
    public int getViewWidth(BottomNavigationView view) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        return params.getConstraintWidget().getWidth();
    }
    public int getViewHeight(BottomNavigationView view) {
        ConstraintLayout.LayoutParams params = (ConstraintLayout.LayoutParams) view.getLayoutParams();
        return params.getConstraintWidget().getHeight();
    }
    /**
     * ???RelativeLayout???????????? view ?????????
     */
//    public int getViewHeight(ImageView view) { // 90 ?????????168
//        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) view.getLayoutParams();
//        return params.height;
//    }
    /**
     * ???LinearLayout???????????? view ?????????
     */
    public int getViewWidth(ImageView view) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        return params.width;
    }
    public int getViewHeight(ImageView view) {
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        return params.height;
    }
    /**
     * ?????? view ?????????
     */
    public void setViewLayoutParams(View view, int nWidth, int nHeight) {
        ViewGroup.LayoutParams lp = view.getLayoutParams();
        if (lp.height != nHeight || lp.width != nWidth) {
            lp.width = nWidth;
            lp.height = nHeight;
            view.setLayoutParams(lp);
        }
    }

    private class MyHandler extends Handler {
        // TODO: 2021/4/17 ????????? ???????????? ?????????
        WeakReference<MainActivity> weakReference;

        public MyHandler(MainActivity activity) {
            weakReference = new WeakReference<>(activity);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            switch (msg.what) {
                case 1:
                    if(weakReference.get() != null)
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if(getViewWidth(navView) != 0)
                                    setViewLayoutParams(imgPlus, getViewWidth(navView) / 5, getViewHeight(navView));
                            }
                        });
                    break;
                default:
                    if(weakReference.get() != null)
                        ToastUtils.showToast("MainActivity: ????????????????????????");
                    break;
            }
        }
    }
}
