package com.zjnu.grouptour.adapter;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.baidu.mapapi.walknavi.adapter.IWEngineInitListener;
import com.baidu.mapapi.walknavi.adapter.IWNaviStatusListener;
import com.baidu.mapapi.walknavi.adapter.IWRouteGuidanceListener;
import com.baidu.mapapi.walknavi.adapter.IWRoutePlanListener;
import com.baidu.mapapi.walknavi.adapter.IWTTSPlayer;
import com.baidu.mapapi.walknavi.model.RouteGuideKind;
import com.baidu.mapapi.walknavi.model.WalkRoutePlanError;
import com.baidu.mapapi.walknavi.params.WalkNaviLaunchParam;
import com.baidu.mapapi.walknavi.params.WalkRouteNodeInfo;
import com.baidu.platform.comapi.walknavi.WalkNaviModeSwitchListener;
import com.baidu.platform.comapi.walknavi.widget.ArCameraView;
import com.hyphenate.easeim.DemoHelper;
import com.hyphenate.easeim.common.utils.ToastUtils;
import com.hyphenate.easeim.section.contact.activity.ContactDetailActivity;
import com.hyphenate.easeui.domain.EaseUser;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.activity.UserDetailActivity;
import com.zjnu.grouptour.activity.baiduMap.LocMapActivity;
import com.zjnu.grouptour.api.GroupTourApi;
import com.zjnu.grouptour.bean.Person;
import com.zjnu.grouptour.view.NestViewEmbeddedRecyclerView;

import java.util.ArrayList;

/**
 * @author luchen
 * @Date 2021/4/18 17:52
 * @Description 用户信息底部弹窗适配器
 */
public class UserInfoRecycleAdapter extends NestViewEmbeddedRecyclerView.Adapter<UserInfoRecycleAdapter.MyViewHolder> {

    private final static String TAG = "UserInfoRecycleAdapter";

    private Context context;
    private ArrayList<Person> list;
    private LatLng startLoc, endLoc;
    private View inflater;
    private WalkNaviLaunchParam walkParam;
    private WalkNavigateHelper mNaviHelper;

    // 构造方法，传入数据
    public UserInfoRecycleAdapter(Context context, ArrayList<Person> list, LatLng startLoc, LatLng endLoc, WalkNavigateHelper mNaviHelper) {
        this.context = context;
        this.list = list;
        this.startLoc = startLoc;
        this.endLoc = endLoc;
        this.mNaviHelper = mNaviHelper;
    }

    public UserInfoRecycleAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // 创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.user_info_recyclerview_item, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(inflater);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // 将数据和控件绑定position
        Person p = list.get(position);

        if(p.getGender() != null) {
            if(p.getGender().equals("男"))
                holder.img_gender.setImageDrawable(context.getDrawable(R.drawable.male));
            else if(p.getGender().equals("女"))
                holder.img_gender.setImageDrawable(context.getDrawable(R.drawable.female));
        }else {
//            holder.img_gender.setImageDrawable(null);
        }

        holder.tv_nickname.setText(p.getNickname());
        holder.tv_tel.setText("手机号：" + p.getTel());

        holder.btn_seeMore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                EaseUser user =new EaseUser();
                user.setUsername(p.getName());
                ContactDetailActivity.actionStart(context, user, DemoHelper.getInstance().getModel().isContact(user.getUsername()));
//                UserDetailActivity.actionStart(context, p.getName());
            }
        });

        holder.btn_navi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                /*构造导航起终点参数对象*/
                WalkRouteNodeInfo walkStartNode = new WalkRouteNodeInfo();
                walkStartNode.setLocation(startLoc);
                WalkRouteNodeInfo walkEndNode = new WalkRouteNodeInfo();
                walkEndNode.setLocation(endLoc);
                walkParam = new WalkNaviLaunchParam().startNodeInfo(walkStartNode).endNodeInfo(walkEndNode);
                walkParam.extraNaviMode(0);
                /* 初始化起终点Marker */
//                initOverlay();
                startWalkNavi();
                LocMapActivity.closeUserInfoDialog();
//                if(!LocMapActivity.closeUserInfoDialog() && GroupTourApi.getInstance().isOpenLogger())
//                    ToastUtils.showToast("Dialog关闭失败");
//                else
//                    ToastUtils.showToast("成功关闭Dialog");
            }
        });

    }

    @Override
    public int getItemCount() {
        // 返回Item总条数
        return list.size();
    }

    // 添加数据
    public void addData(int position) {
        // 在list中添加数据，并通知条目加入一条
        list.add(position, new Person());

        // 添加动画
        notifyItemInserted(position);
        notifyDataSetChanged();
    }

    // 删除数据
    public void removeData(int position) {
        list.remove(position);

        // 删除动画
        notifyItemRemoved(position);
    }

    // 内部类，绑定控件
    class MyViewHolder extends NestViewEmbeddedRecyclerView.ViewHolder {

        ImageView img_gender;
        TextView tv_nickname, tv_tel;
        Button btn_seeMore, btn_navi;


        public MyViewHolder(View itemView) {
            super(itemView);
            img_gender = (ImageView) itemView.findViewById(R.id.img_gender);
            tv_nickname = (TextView) itemView.findViewById(R.id.tv_nickname);
            tv_tel = (TextView) itemView.findViewById(R.id.tv_tel);
            btn_seeMore = (Button) itemView.findViewById(R.id.btn_seeMore);
            btn_navi = (Button) itemView.findViewById(R.id.btn_navi);
        }
    }

    /**
     * 开始步行导航
     */
    private void startWalkNavi() {
        Log.d(TAG, "startWalkNavi");
        try {
            WalkNavigateHelper.getInstance().initNaviEngine(getActivityFromContext(context), new IWEngineInitListener() {
                @Override
                public void engineInitSuccess() {
                    Log.d(TAG, "WalkNavi engineInitSuccess");
                    routePlanWithWalkParam();
                }

                @Override
                public void engineInitFail() {
                    Log.d(TAG, "WalkNavi engineInitFail");
                    WalkNavigateHelper.getInstance().unInitNaviEngine();
                }
            });
        } catch (Exception e) {
            Log.d(TAG, "startBikeNavi Exception");
            e.printStackTrace();
        }
    }

    /**
     * 发起步行导航算路
     */
    private void routePlanWithWalkParam() {
        WalkNavigateHelper.getInstance().routePlanWithRouteNode(walkParam, new IWRoutePlanListener() {
            @Override
            public void onRoutePlanStart() {
                Log.d(TAG, "WalkNavi onRoutePlanStart");
            }

            @Override
            public void onRoutePlanSuccess() {

                Log.d(TAG, "onRoutePlanSuccess");

//                Intent intent = new Intent();
//                intent.setClass(LocMapActivity.this, WalkNaviGuideActivity.class);
//                startActivity(intent);

                try {
                    View view = mNaviHelper.onCreate(getActivityFromContext(context));
                    if (view != null) {
                        getActivityFromContext(context).setContentView(view);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                mNaviHelper.setWalkNaviStatusListener(new IWNaviStatusListener() {
                    @Override
                    public void onWalkNaviModeChange(int mode, WalkNaviModeSwitchListener listener) {
                        Log.d(TAG, "onWalkNaviModeChange : " + mode);
                        mNaviHelper.switchWalkNaviMode(getActivityFromContext(context), mode, listener);
                    }

                    @Override
                    public void onNaviExit() {
                        Log.d(TAG, "onNaviExit");
                    }
                });

                mNaviHelper.setTTsPlayer(new IWTTSPlayer() {
                    @Override
                    public int playTTSText(final String s, boolean b) {
                        Log.d(TAG, "tts: " + s);
                        return 0;
                    }
                });

                boolean startResult = mNaviHelper.startWalkNavi(getActivityFromContext(context));
                Log.e(TAG, "startWalkNavi result : " + startResult);

                mNaviHelper.setRouteGuidanceListener(getActivityFromContext(context), new IWRouteGuidanceListener() {
                    @Override
                    public void onRouteGuideIconUpdate(Drawable icon) {

                    }

                    @Override
                    public void onRouteGuideKind(RouteGuideKind routeGuideKind) {
                        Log.d(TAG, "onRouteGuideKind: " + routeGuideKind);
                    }

                    @Override
                    public void onRoadGuideTextUpdate(CharSequence charSequence, CharSequence charSequence1) {
                        Log.d(TAG, "onRoadGuideTextUpdate   charSequence=: " + charSequence + "   charSequence1 = : " +
                                charSequence1);

                    }

                    @Override
                    public void onRemainDistanceUpdate(CharSequence charSequence) {
                        Log.d(TAG, "onRemainDistanceUpdate: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onRemainTimeUpdate(CharSequence charSequence) {
                        Log.d(TAG, "onRemainTimeUpdate: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onGpsStatusChange(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onGpsStatusChange: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onRouteFarAway(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onRouteFarAway: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onRoutePlanYawing(CharSequence charSequence, Drawable drawable) {
                        Log.d(TAG, "onRoutePlanYawing: charSequence = :" + charSequence);

                    }

                    @Override
                    public void onReRouteComplete() {

                    }

                    @Override
                    public void onArriveDest() {

                    }

                    @Override
                    public void onIndoorEnd(Message msg) {

                    }

                    @Override
                    public void onFinalEnd(Message msg) {

                    }

                    @Override
                    public void onVibrate() {

                    }
                });

            }

            @Override
            public void onRoutePlanFail(WalkRoutePlanError error) {
                Log.d(TAG, "WalkNavi onRoutePlanFail");
                if(GroupTourApi.getInstance().isOpenLogger()) {
                    if("DISTANCE_MORE_THAN_50KM".equals(error.name()))
                        ToastUtils.showToast("离目的地超过50千米，发起步行导航失败！");
                }
            }

        });
    }

    public static @Nullable Activity getActivityFromContext(@Nullable Context context) {
        if (context == null) {
            return null;
        }

        if (context instanceof Activity) {
            return (Activity) context;
        }

        if (context instanceof Application || context instanceof Service) {
            return null;
        }

        Context c = context;
        while (c != null) {
            if (c instanceof ContextWrapper) {
                c = ((ContextWrapper) c).getBaseContext();

                if (c instanceof Activity) {
                    return (Activity) c;
                }
            } else {
                return null;
            }
        }

        return null;
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == ArCameraView.WALK_AR_PERMISSION) {
//            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_DENIED) {
//                Toast.makeText(LocMapActivity.this, "没有相机权限,请打开后重试", Toast.LENGTH_SHORT).show();
//            } else if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                mNaviHelper.startCameraAndSetMapView(LocMapActivity.this);
//            }
//        }
//    }
}
