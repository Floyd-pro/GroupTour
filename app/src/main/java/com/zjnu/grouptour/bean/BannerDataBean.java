package com.zjnu.grouptour.bean;

import com.zjnu.grouptour.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * @author luchen
 * @Date 2021/4/23 14:39
 * @Description Banner图片数据类
 */
public class BannerDataBean {
    public Integer imageRes;
    public String imageUrl;
    public String title;
    public int viewType;

    public BannerDataBean(Integer imageRes, String title, int viewType) {
        this.imageRes = imageRes;
        this.title = title;
        this.viewType = viewType;
    }

    public BannerDataBean(String imageUrl, String title, int viewType) {
        this.imageUrl = imageUrl;
        this.title = title;
        this.viewType = viewType;
    }

    public static List<BannerDataBean> getTestData() {
        List<BannerDataBean> list = new ArrayList<>();
        list.add(new BannerDataBean(R.drawable.image_hangzhou, "杭州西湖黄昏之美", 1));
        list.add(new BannerDataBean(R.drawable.image_zhangjiajie, "张家界，被雪覆盖的台阶", 1));
        list.add(new BannerDataBean(R.drawable.image_suzhou, "苏州狮子林，元代园林代表作", 1));
        list.add(new BannerDataBean(R.drawable.image_jiuzhaigou, "九寨沟，诺日朗瀑布", 1));
        list.add(new BannerDataBean(R.drawable.image_neimenggu, "内蒙古大草原美景", 1));
        list.add(new BannerDataBean(R.drawable.image_huangshan, "黄山云海日出", 1));
        return list;
    }

    public static List<String> getColors(int size) {
        List<String> list = new ArrayList<>();
        for(int i = 0; i < size; i++) {
            list.add(getRandColor());
        }
        return list;
    }

    /**
     * 获取十六进制的颜色代码.例如  "#5A6677"
     * 分别取R、G、B的随机值，然后加起来即可
     *
     * @return String
     */
    public static String getRandColor() {
        String R, G, B;
        Random random = new Random();
        R = Integer.toHexString(random.nextInt(256)).toUpperCase();
        G = Integer.toHexString(random.nextInt(256)).toUpperCase();
        B = Integer.toHexString(random.nextInt(256)).toUpperCase();

        R = R.length() == 1 ? "0" + R : R;
        G = G.length() == 1 ? "0" + G : G;
        B = B.length() == 1 ? "0" + B : B;

        return "#" + R + G + B;
    }
}
