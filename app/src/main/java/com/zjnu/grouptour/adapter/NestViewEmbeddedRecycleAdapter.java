package com.zjnu.grouptour.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.hyphenate.easeim.common.utils.ToastUtils;
import com.zjnu.grouptour.R;
import com.zjnu.grouptour.TestMainActivity;
import com.zjnu.grouptour.activity.DestinationSettingActivity;
import com.zjnu.grouptour.activity.baiduMap.LocMapActivity;
import com.zjnu.grouptour.activity.baiduMap.TestMapActivity;
import com.zjnu.grouptour.activity.baiduMap.WalkRouteSearchActivity;
import com.zjnu.grouptour.bean.PlusBean;
import com.zjnu.grouptour.view.NestViewEmbeddedRecyclerView;

import java.util.ArrayList;

/**
 * @author luchen
 * @Date 2021/4/18 17:52
 * @Description 底部弹窗适配器
 */
public class NestViewEmbeddedRecycleAdapter extends NestViewEmbeddedRecyclerView.Adapter<NestViewEmbeddedRecycleAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<PlusBean> list;
    private View inflater;

    // 构造方法，传入数据
    public NestViewEmbeddedRecycleAdapter(Context context, ArrayList<PlusBean> list) {
        this.context = context;
        this.list = list;
    }

    public NestViewEmbeddedRecycleAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        // 创建ViewHolder，返回每一项的布局
        inflater = LayoutInflater.from(context).inflate(R.layout.nestview_embedded_recyclerview_item, viewGroup, false);
        MyViewHolder myViewHolder = new MyViewHolder(inflater);
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        // 将数据和控件绑定position
        PlusBean plusBean = list.get(position);

        holder.img_first.setImageDrawable(plusBean.getImage1());
        holder.img_second.setImageDrawable(plusBean.getImage2());
        holder.img_third.setImageDrawable(plusBean.getImage3());
        holder.img_fourth.setImageDrawable(plusBean.getImage4());

        holder.tv_first.setText(plusBean.getName1());
        holder.tv_second.setText(plusBean.getName2());
        holder.tv_third.setText(plusBean.getName3());
        holder.tv_fourth.setText(plusBean.getName4());

        if (holder.tv_first.getText().toString() != null) {
            startActivity(holder, 1, holder.tv_first.getText().toString());
        }
        if (holder.tv_second.getText().toString() != null) {
            startActivity(holder, 2, holder.tv_second.getText().toString());
        }
        if (holder.tv_third.getText().toString() != null) {
            startActivity(holder, 3, holder.tv_third.getText().toString());
        }
        if (holder.tv_fourth.getText().toString() != null) {
            startActivity(holder, 4, holder.tv_fourth.getText().toString());
        }

    }

    // TODO: 2021/4/19 跳转界面待设置
    public void startActivity(@NonNull MyViewHolder holder, int num, String str) {
        switch (str) {
            case "打开地图":
                switch (num) {
                    case 1:
                        holder.linear_first.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, LocMapActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 2:
                        holder.linear_second.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, LocMapActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 3:
                        holder.linear_third.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, LocMapActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 4:
                        holder.linear_fourth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, LocMapActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    default:
                        break;
                }
                break;
            case "自由导航":
                switch (num) {
                    case 1:
                        holder.linear_first.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, WalkRouteSearchActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 2:
                        holder.linear_second.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, WalkRouteSearchActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 3:
                        holder.linear_third.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, WalkRouteSearchActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 4:
                        holder.linear_fourth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, WalkRouteSearchActivity.class);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    default:
                        break;
                }
                break;
            case "天气查询":
                switch (num) {
                    case 1:
                        holder.linear_first.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, DestinationSettingActivity.class);
                                intent.putExtra("isSetting", false);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 2:
                        holder.linear_second.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, DestinationSettingActivity.class);
                                intent.putExtra("isSetting", false);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 3:
                        holder.linear_third.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, DestinationSettingActivity.class);
                                intent.putExtra("isSetting", false);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 4:
                        holder.linear_fourth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent(context, DestinationSettingActivity.class);
                                intent.putExtra("isSetting", false);
                                context.startActivity(intent);
                            }
                        });
                        break;
                    default:
                        break;
                }
                break;
            case "发表游记":
            case "一键发布":
            case "更多精彩":
                switch (num) {
                    case 1:
                        holder.linear_first.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ToastUtils.showToast("该功能正在开发中，敬请期待~");
//                                Intent intent = new Intent(context, TestMapActivity.class);
//                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 2:
                        holder.linear_second.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ToastUtils.showToast("该功能正在开发中，敬请期待~");
//                                Intent intent = new Intent(context, TestMapActivity.class);
//                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 3:
                        holder.linear_third.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ToastUtils.showToast("该功能正在开发中，敬请期待~");
//                                Intent intent = new Intent(context, TestMapActivity.class);
//                                context.startActivity(intent);
                            }
                        });
                        break;
                    case 4:
                        holder.linear_fourth.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                ToastUtils.showToast("该功能正在开发中，敬请期待~");
//                                Intent intent = new Intent(context, TestMapActivity.class);
//                                context.startActivity(intent);
                            }
                        });
                        break;
                    default:
                        break;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public int getItemCount() {
        // 返回Item总条数
        return list.size();
    }

    // 添加数据
    public void addData(int position) {
        // 在list中添加数据，并通知条目加入一条
        list.add(position, new PlusBean());

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

        ImageView img_first, img_second, img_third, img_fourth;
        TextView tv_first, tv_second, tv_third, tv_fourth;
        LinearLayout linear_first, linear_second, linear_third, linear_fourth;


        public MyViewHolder(View itemView) {
            super(itemView);
            img_first = (ImageView) itemView.findViewById(R.id.img_first);
            img_second = (ImageView) itemView.findViewById(R.id.img_second);
            img_third = (ImageView) itemView.findViewById(R.id.img_third);
            img_fourth = (ImageView) itemView.findViewById(R.id.img_fourth);
            tv_first = (TextView) itemView.findViewById(R.id.tv_first);
            tv_second = (TextView) itemView.findViewById(R.id.tv_second);
            tv_third = (TextView) itemView.findViewById(R.id.tv_third);
            tv_fourth = (TextView) itemView.findViewById(R.id.tv_fourth);
            linear_first = (LinearLayout) itemView.findViewById(R.id.linear_first);
            linear_second = (LinearLayout) itemView.findViewById(R.id.linear_second);
            linear_third = (LinearLayout) itemView.findViewById(R.id.linear_third);
            linear_fourth = (LinearLayout) itemView.findViewById(R.id.linear_fourth);
        }
    }
}
