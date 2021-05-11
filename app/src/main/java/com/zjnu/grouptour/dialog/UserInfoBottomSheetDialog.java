package com.zjnu.grouptour.dialog;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.walknavi.WalkNavigateHelper;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.view.NestViewEmbeddedRecyclerView;
import com.zjnu.grouptour.adapter.UserInfoRecycleAdapter;
import com.zjnu.grouptour.bean.Person;

import java.util.ArrayList;

/**
 * @author luchen
 * @Date 2021/4/25 18:38
 * @Description 用户浏览地图时点击队员图标，打开的队员信息底部弹窗
 */
public class UserInfoBottomSheetDialog extends BottomSheetDialog {
    private Context context;
    private NestViewEmbeddedRecyclerView recyclerView;
    private UserInfoRecycleAdapter recycleAdapter;

    public UserInfoBottomSheetDialog(@NonNull Context context, ArrayList<Person> list, LatLng startLoc, LatLng endLoc, WalkNavigateHelper mNaviHelper) {
        super(context);
        this.context = context;
        this.recycleAdapter = new UserInfoRecycleAdapter(context, list, startLoc, endLoc, mNaviHelper);
//        createView();

        View bottomSheetView = getLayoutInflater().inflate(R.layout.custom_bottom_sheet_dialog, null);
        setContentView(bottomSheetView);
        setCancelable(false);

        // 注意：这里要给layout的parent设置peekHeight，而不是在layout里给layout本身设置，下面设置背景色同理
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) bottomSheetView.getParent()));
        bottomSheetBehavior.setPeekHeight(700);

        ((View) bottomSheetView.getParent()).setBackground(context.getResources().getDrawable(R.drawable.bg_radius_10));
//        ((View) bottomSheetView.getParent()).setBackgroundColor(context.getResources().getColor(R.color.azure));

        ImageView img_down = findViewById(R.id.img_down);
        if (img_down != null) {
            img_down.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    UserInfoBottomSheetDialog.this.hide();
                }
            });
        }

        recyclerView = bottomSheetView.findViewById(R.id.recycler_view);

        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(recycleAdapter);
        recyclerView.setNestedScrollingEnabled(false);
    }

    // 为了在Dialog隐藏时，把状态栏文字颜色变回去
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        int screenHeight = getScreenHeight(getOwnerActivity());
        int statusBarHeight = getStatusBarHeight(getContext());
        int dialogHeight = screenHeight - statusBarHeight;
        if (getWindow() != null)
            getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, dialogHeight == 0 ? ViewGroup.LayoutParams.MATCH_PARENT : screenHeight);
    }

    private static int getScreenHeight(Activity activity) {
        DisplayMetrics displaymetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        return displaymetrics.heightPixels;
    }

    private static int getStatusBarHeight(Context context) {
        int statusBarHeight = 0;
        Resources res = context.getResources();
        int resourceId = res.getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            statusBarHeight = res.getDimensionPixelSize(resourceId);
        }
        return statusBarHeight;
    }
}
