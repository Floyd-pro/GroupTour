package com.zjnu.grouptour.dialog;

import android.content.Context;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.adapter.NestViewEmbeddedRecycleAdapter;
import com.zjnu.grouptour.view.NestViewEmbeddedRecyclerView;


/**
 * @author luchen
 * @Date 2021/4/18 17:33
 * @Description 自定义的BottomSheetDialog
 */
public class CustomBottomSheetDialog extends BottomSheetDialog {
    private Context context;
    private NestViewEmbeddedRecyclerView recyclerView;
    private NestViewEmbeddedRecycleAdapter recycleAdapter;

    public CustomBottomSheetDialog(@NonNull Context context) {
        super(context);
        this.context = context;
        this.recycleAdapter = new NestViewEmbeddedRecycleAdapter(context);
        createView();
    }

    public void createView() {
        View bottomSheetView = getLayoutInflater().inflate(R.layout.custom_bottom_sheet_dialog, null);
        setContentView(bottomSheetView);

        // 注意：这里要给layout的parent设置peekHeight，而不是在layout里给layout本身设置，下面设置背景色同理，坑爹！！！
        BottomSheetBehavior bottomSheetBehavior = BottomSheetBehavior.from(((View) bottomSheetView.getParent()));
        bottomSheetBehavior.setPeekHeight(730);

        ((View) bottomSheetView.getParent()).setBackgroundColor(context.getResources().getColor(R.color.transparent));
        recyclerView = bottomSheetView.findViewById(R.id.recycler_view);

        LinearLayoutManager manager = new LinearLayoutManager(context);
        manager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(manager);
        recyclerView.setAdapter(recycleAdapter);
    }
}
