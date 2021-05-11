package com.zjnu.grouptour.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.youth.banner.adapter.BannerAdapter;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.bean.BannerDataBean;
import com.zjnu.grouptour.viewholder.ImageTitleHolder;

import java.util.List;

/**
 * @author luchen
 * @Date 2021/4/23 14:16
 * @Description 轮播图适配器
 */
public class ImageTitleAdapter extends BannerAdapter<BannerDataBean, ImageTitleHolder> {

    public ImageTitleAdapter(List<BannerDataBean> mDatas) {
        super(mDatas);
    }

    @Override
    public ImageTitleHolder onCreateHolder(ViewGroup parent, int viewType) {
        return new ImageTitleHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.banner_image_title, parent, false));
    }

    @Override
    public void onBindView(ImageTitleHolder holder, BannerDataBean data, int position, int size) {
        holder.imageView.setImageResource(data.imageRes);
        holder.title.setText(data.title);
    }

}
