package com.zjnu.grouptour.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author luchen
 * @Date 2021/4/19 19:09
 * @Description 组员定位信息类
 */
public class GroupmateInfo implements Parcelable {
    private int id;
    private String name;
    private double longitude;
    private double latitude;
    private String nickname;
    private String tel;
    private String gender;

    public GroupmateInfo() {

    }

    public GroupmateInfo(int id, String name, double longitude, double latitude, String nickname, String tel, String gender) {
        this.id = id;
        this.name = name;
        this.longitude = longitude;
        this.latitude = latitude;
        this.nickname = nickname;
        this.tel = tel;
        this.gender = gender;
    }

    public GroupmateInfo(Parcel source) {
        id = source.readInt();
        name = source.readString();
        longitude = source.readDouble();
        latitude = source.readDouble();
        nickname = source.readString();
        tel = source.readString();
        gender = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeString(nickname);
        parcel.writeString(tel);
        parcel.writeString(gender);
    }

    public static final Creator<GroupmateInfo> CREATOR = new Creator<GroupmateInfo>() {

        /**
         * 供外部类反序列化本类数组使用
         */
        @Override
        public GroupmateInfo[] newArray(int size) {
            return new GroupmateInfo[size];
        }

        /**
         * 从Parcel中读取数据
         */
        @Override
        public GroupmateInfo createFromParcel(Parcel source) {
            return new GroupmateInfo(source);
        }
    };

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }
}
