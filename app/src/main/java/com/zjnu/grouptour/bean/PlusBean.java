package com.zjnu.grouptour.bean;

import android.graphics.drawable.Drawable;

/**
 * @author luchen
 * @Date 2021/4/18 17:56
 * @Description Plus探索模块Bean
 */
public class PlusBean {
    private String name1;
    private String name2;
    private String name3;
    private String name4;
    private Drawable image1;
    private Drawable image2;
    private Drawable image3;
    private Drawable image4;

    public PlusBean() {

    }

    public PlusBean(String name1, Drawable image1) {
        this.name1 = name1;
        this.image1 = image1;
    }

    public PlusBean(String name1, String name2, Drawable image1, Drawable image2) {
        this.name1 = name1;
        this.name2 = name2;
        this.image1 = image1;
        this.image2 = image2;
    }

    public PlusBean(String name1, String name2, String name3, Drawable image1, Drawable image2, Drawable image3) {
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
    }

    public PlusBean(String name1, String name2, String name3, String name4, Drawable image1, Drawable image2, Drawable image3, Drawable image4) {
        this.name1 = name1;
        this.name2 = name2;
        this.name3 = name3;
        this.name4 = name4;
        this.image1 = image1;
        this.image2 = image2;
        this.image3 = image3;
        this.image4 = image4;
    }

    public String getName1() {
        return name1;
    }

    public void setName1(String name1) {
        this.name1 = name1;
    }

    public String getName2() {
        return name2;
    }

    public void setName2(String name2) {
        this.name2 = name2;
    }

    public String getName3() {
        return name3;
    }

    public void setName3(String name3) {
        this.name3 = name3;
    }

    public String getName4() {
        return name4;
    }

    public void setName4(String name4) {
        this.name4 = name4;
    }

    public Drawable getImage1() {
        return image1;
    }

    public void setImage1(Drawable image1) {
        this.image1 = image1;
    }

    public Drawable getImage2() {
        return image2;
    }

    public void setImage2(Drawable image2) {
        this.image2 = image2;
    }

    public Drawable getImage3() {
        return image3;
    }

    public void setImage3(Drawable image3) {
        this.image3 = image3;
    }

    public Drawable getImage4() {
        return image4;
    }

    public void setImage4(Drawable image4) {
        this.image4 = image4;
    }
}
