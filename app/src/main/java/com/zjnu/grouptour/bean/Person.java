package com.zjnu.grouptour.bean;

import android.graphics.Picture;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author luchen
 * @Date 2021/4/17 15:07
 * @Description 登录用户类
 */
public class Person implements Parcelable {
    private int id;
    private String name;
    private String psw;
    private double longitude;
    private double latitude;
    private int teamID;
    private String gender;
    private String nickname;
    private String tel;
    private String wechat;
    private String qq;
    private String email;
    private String signature;
    private String realName;
    private String identityNum;
    private Picture avatar;

    public Person() {

    }

    public Person(String name) {
        this.name = name;
    }

    public Person(String name, String psw) {
        this.name = name;
        this.psw = psw;
    }

    public Person(int id, String name, String psw) {
        this.id = id;
        this.name = name;
        this.psw = psw;
    }

    public Person(int id, String name, String psw, double longitude, double latitude,
                  int teamID, String gender, String nickname, String tel, String wechat,
                  String qq, String email, String signature, String realName, String identityNum) {
        this.id = id;
        this.name = name;
        this.psw = psw;
        this.longitude = longitude;
        this.latitude = latitude;
        this.teamID = teamID;
        this.gender = gender;
        this.nickname = nickname;
        this.tel = tel;
        this.wechat = wechat;
        this.qq = qq;
        this.email = email;
        this.signature = signature;
        this.realName = realName;
        this.identityNum = identityNum;
    }

    public Person(int id, String name, String psw, double longitude, double latitude,
                  int teamID, String gender, String nickname, String tel, String wechat,
                  String qq, String email, String signature, String realName, String identityNum, Picture avatar) {
        this.id = id;
        this.name = name;
        this.psw = psw;
        this.longitude = longitude;
        this.latitude = latitude;
        this.teamID = teamID;
        this.gender = gender;
        this.nickname = nickname;
        this.tel = tel;
        this.wechat = wechat;
        this.qq = qq;
        this.email = email;
        this.signature = signature;
        this.realName = realName;
        this.identityNum = identityNum;
        this.avatar = avatar;
    }

    public Person(Parcel source) {
        id = source.readInt();
        name = source.readString();
        psw = source.readString();
        longitude = source.readDouble();
        latitude = source.readDouble();
        teamID = source.readInt();
        gender = source.readString();
        nickname = source.readString();
        tel = source.readString();
        wechat = source.readString();
        qq = source.readString();
        email = source.readString();
        signature = source.readString();
        realName = source.readString();
        identityNum = source.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(id);
        parcel.writeString(name);
        parcel.writeString(psw);
        parcel.writeDouble(longitude);
        parcel.writeDouble(latitude);
        parcel.writeInt(teamID);
        parcel.writeString(gender);
        parcel.writeString(nickname);
        parcel.writeString(tel);
        parcel.writeString(wechat);
        parcel.writeString(qq);
        parcel.writeString(email);
        parcel.writeString(signature);
        parcel.writeString(realName);
        parcel.writeString(identityNum);
    }

    public static final Creator<Person> CREATOR = new Creator<Person>() {

        /**
         * 供外部类反序列化本类数组使用
         */
        @Override
        public Person[] newArray(int size) {
            return new Person[size];
        }

        /**
         * 从Parcel中读取数据
         */
        @Override
        public Person createFromParcel(Parcel source) {
            return new Person(source);
        }
    };

    @Override
    public String toString() {
        return "Person{ " +
                "id=" + id +
                " , name='" + name + '\'' +
                " , psw='" + psw + '\'' +
                " , longitude='" + longitude + '\'' +
                " , latitude='" + latitude + '\'' +
                " , teamID=" + teamID +
                " , gender=" + teamID +
                " , nickname='" + nickname + '\'' +
                " , tel='" + tel + '\'' +
                " , wechat='" + wechat + '\'' +
                " , qq=" + qq +
                " , email='" + email + '\'' +
                " , signature='" + signature + '\'' +
                " , realName='" + realName + '\'' +
                " , identityNum='" + identityNum + '\'' +
                " }";
    }

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

    public String getPsw() {
        return psw;
    }

    public void setPsw(String psw) {
        this.psw = psw;
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
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

    public String getWechat() {
        return wechat;
    }

    public void setWechat(String wechat) {
        this.wechat = wechat;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getIdentityNum() {
        return identityNum;
    }

    public void setIdentityNum(String identityNum) {
        this.identityNum = identityNum;
    }

    public Picture getAvatar() {
        return avatar;
    }

    public void setAvatar(Picture avatar) {
        this.avatar = avatar;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
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
}
