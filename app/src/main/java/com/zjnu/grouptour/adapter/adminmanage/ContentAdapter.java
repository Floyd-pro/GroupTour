package com.zjnu.grouptour.adapter.adminmanage;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeim.section.base.WebViewActivity;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.bean.AdminInfoManage;
import com.zjnu.grouptour.view.CustomHorizontalScrollView;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author luchen
 * @Date 2021/5/3 13:42
 * @Description
 */
public class ContentAdapter extends RecyclerView.Adapter<ContentAdapter.ItemViewHolder> {


    private Context context;
    private List<AdminInfoManage> datas;
    private List<ItemViewHolder> mViewHolderList = new ArrayList<>();
    public int offestX = 0;
    private OnContentScrollListener onContentScrollListener;

    public interface OnContentScrollListener {
        void onScroll(MotionEvent event);
    }

    public void setOnContentScrollListener(OnContentScrollListener onContentScrollListener) {
        this.onContentScrollListener = onContentScrollListener;
    }


    public ContentAdapter(Context context) {
        this.context = context;
    }

    public void setDatas(List<AdminInfoManage> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(context).inflate(R.layout.layout_item_content, viewGroup, false);
        return new ItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ItemViewHolder itemViewHolder, int i) {
        itemViewHolder.tvLeftTitle.setText(datas.get(i).getLeftTitle());
        //右边滑动部分
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(context);
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        itemViewHolder.rvItemRight.setLayoutManager(linearLayoutManager);
        itemViewHolder.rvItemRight.setHasFixedSize(true);
        RightScrollAdapter rightScrollAdapter = new RightScrollAdapter(context);
        rightScrollAdapter.setDatas(datas.get(i).getRightDatas());
        itemViewHolder.rvItemRight.setAdapter(rightScrollAdapter);
        //缓存当前holder
        if (!mViewHolderList.contains(itemViewHolder)) {
            mViewHolderList.add(itemViewHolder);
        }
        //滑动回调地狱
//        //滑动单个ytem的rv时,右侧整个区域的联动
//        itemViewHolder.horItemScrollview.setOnCustomScrollChangeListener(new CustomHorizontalScrollView.OnCustomScrollChangeListener() {
//            @Override
//            public void onCustomScrollChange(CustomHorizontalScrollView listener, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
//                for (int i = 0; i < mViewHolderList.size(); i++) {
//                    ItemViewHolder touchViewHolder = mViewHolderList.get(i);
//                    if (touchViewHolder != itemViewHolder) {
//                        touchViewHolder.horItemScrollview.scrollTo(scrollX, 0);
//                    }
//                }
//                //记录滑动距离,便于处理下拉刷新之后的还原操作
//                if (null != onContentScrollListener) onContentScrollListener.onScroll(scrollX);
//                offestX = scrollX;
//            }
//        });
        itemViewHolder.horItemScrollview.setEventListener(new CustomHorizontalScrollView.EventListener() {
            @Override
            public void onEvent(MotionEvent event) {
                if (null != onContentScrollListener) onContentScrollListener.onScroll(event);
            }
        });
        //由于viewHolder的缓存,在1级缓存取出来是2个viewholder,并且不会被重新赋值,所以这里需要处理缓存的viewholder的位移
        itemViewHolder.horItemScrollview.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (!itemViewHolder.isLayoutFinish()) {
                    itemViewHolder.horItemScrollview.scrollTo(offestX, 0);
                    itemViewHolder.setLayoutFinish(true);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return null == datas ? 0 : datas.size();
    }

    public List<ItemViewHolder> getViewHolderCacheList() {
        return mViewHolderList;
    }

    public int getOffestX() {
        return offestX;
    }

    public class ItemViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.tv_left_title)
        TextView tvLeftTitle;
        @BindView(R.id.rv_item_right)
        RecyclerView rvItemRight;
        @BindView(R.id.hor_item_scrollview)
        public CustomHorizontalScrollView horItemScrollview;
        private boolean isLayoutFinish;//自定义字段,用于标记layout

        public boolean isLayoutFinish() {
            return isLayoutFinish;
        }

        public void setLayoutFinish(boolean layoutFinish) {
            isLayoutFinish = layoutFinish;
        }

        public ItemViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
