package com.zjnu.grouptour.view;

import androidx.recyclerview.widget.RecyclerView;

/**
 * @author luchen
 * @Date 2021/4/18 17:27
 * @Description 实现被ScrollView嵌套的RecyclerView类
 */
public class NestViewEmbeddedRecyclerView extends RecyclerView {
    public NestViewEmbeddedRecyclerView(android.content.Context context, android.util.AttributeSet attrs){
        super(context, attrs);
    }
    /**
     * 设置不滚动(ListView需处理该问题，此问题在RecyclerView中已被解决)
     */
//    public void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
//    {
//        int expandSpec = MeasureSpec.makeMeasureSpec(2, MeasureSpec.AT_MOST);
//        super.onMeasure(widthMeasureSpec, expandSpec);
//    }
}
