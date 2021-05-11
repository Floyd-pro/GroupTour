package com.zjnu.grouptour.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.bean.WeatherInfo;
import com.zjnu.grouptour.utils.CommonUtils;

import java.util.ArrayList;

/**
 * @author luchen
 * @Date 2021/4/23 23:51
 * @Description 天气预报适配器
 */
public class WeatherForcastRecycleAdapter extends RecyclerView.Adapter<WeatherForcastRecycleAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<WeatherInfo> list;
    private View inflater;

    // 构造方法，传入数据
    public WeatherForcastRecycleAdapter(Context context, ArrayList<WeatherInfo> list) {
        this.context = context;
        this.list = list;
    }

    public WeatherForcastRecycleAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // 创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.weather_recyclerview_item, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(inflater);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        WeatherInfo w = list.get(position);

        holder.imgWeather.setImageDrawable(w.getImgWeather());
//        String preDate = CommonUtils.dateToString(w.getDate(), "yyyy-MM-dd");
//        int month = Integer.parseInt(preDate.substring(preDate.indexOf("-")+1, preDate.lastIndexOf("-")));
//        int day = Integer.parseInt(preDate.substring(preDate.lastIndexOf("-")+1, preDate.length()));
//        String date = month + "月" + day + "日";
//        holder.tvDate.setText(date);
        holder.tvDate.setText(w.getDate());
        holder.tvDayOfWeek.setText(w.getDayOfWeek());
        String temp = w.getMaxTemp() + "℃/" + w.getMinTemp() + "℃";
        holder.tvTemp.setText(temp);
        holder.tvPhenomenon.setText(w.getWeatherPhenomenon());
        holder.tvWind.setText(w.getWindLevel());
        holder.imgWeather.setImageDrawable(w.getImgWeather());
        holder.linearForcast.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ToastUtils.showToast(w.getDate() + "的天气是 " + w.getWeatherPhenomenon());
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
        list.add(position, new WeatherInfo());

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
    class MyViewHolder extends RecyclerView.ViewHolder {

        ImageView imgWeather;
        TextView tvDate, tvDayOfWeek, tvPhenomenon, tvTemp, tvWind;
        LinearLayout linearForcast;


        public MyViewHolder(View itemView) {
            super(itemView);
            imgWeather = (ImageView) itemView.findViewById(R.id.img_weather);
            tvDate = (TextView) itemView.findViewById(R.id.tv_date);
            tvDayOfWeek = (TextView) itemView.findViewById(R.id.tv_dayOfWeek);
            tvPhenomenon = (TextView) itemView.findViewById(R.id.tv_phenomenon);
            tvTemp = (TextView) itemView.findViewById(R.id.tv_temp);
            tvWind = (TextView) itemView.findViewById(R.id.tv_wind);
            linearForcast = (LinearLayout) itemView.findViewById(R.id.ll_forcast);
        }
    }
}
