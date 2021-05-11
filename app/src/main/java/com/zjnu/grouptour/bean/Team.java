package com.zjnu.grouptour.bean;

import android.os.Parcel;
import android.os.Parcelable;

import com.zjnu.grouptour.utils.CommonUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author luchen
 * @Date 2021/4/20 21:55
 * @Description 队伍类
 */
public class Team implements Parcelable {
    private int teamID;
    private String teamName;
    private String description;
    private String destinationName;
    private String destinationCity;
    private double destinationLongitude;
    private double destinationLatitude;
    private Date departureDateTime;
    private int principalID;
    private String principalName;
    private String principalTel;
    private int scheduleID;

    private final String formatDateType1 = "yyyy-MM-dd";
    private final String formatDateType2 = "yyyy-MM-dd HH:mm:ss";

    public Team() {

    }

    public Team(String teamName) {
        this.teamName = teamName;
    }

    public Team(String teamName, String description) {
        this.teamName = teamName;
        this.description = description;
    }

    public Team(String teamName, String description, String principalName) {
        this.teamName = teamName;
        this.description = description;
        this.principalName = principalName;
    }

    public Team(int teamID, String teamName, String description, String destinationName, String destinationCity,
                double longitude, double latitude, Date departureDateTime, int principalID, String principalName,
                String principalTel, int scheduleID) {
        this.teamID = teamID;
        this.teamName = teamName;
        this.description = description;
        this.destinationName = destinationName;
        this.destinationCity = destinationCity;
        this.destinationLongitude = longitude;
        this.destinationLatitude = latitude;
        this.departureDateTime = departureDateTime;
        this.principalID = principalID;
        this.principalName = principalName;
        this.principalTel = principalTel;
        this.scheduleID = scheduleID;
    }

    protected Team(Parcel source) {
        teamID = source.readInt();
        teamName = source.readString();
        description = source.readString();
        destinationName = source.readString();
        destinationCity = source.readString();
        destinationLongitude = source.readDouble();
        destinationLatitude = source.readDouble();
        try {
            departureDateTime = CommonUtils.longToDate(source.readLong(), formatDateType2);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        principalID = source.readInt();
        principalName = source.readString();
        principalTel = source.readString();
        scheduleID = source.readInt();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(teamID);
        parcel.writeString(teamName);
        parcel.writeString(description);
        parcel.writeString(destinationName);
        parcel.writeString(destinationCity);
        parcel.writeDouble(destinationLongitude);
        parcel.writeDouble(destinationLatitude);
        parcel.writeLong(CommonUtils.dateToLong(departureDateTime));
        parcel.writeInt(principalID);
        parcel.writeString(principalName);
        parcel.writeString(principalTel);
        parcel.writeInt(scheduleID);
    }

    public static final Creator<Team> CREATOR = new Creator<Team>() {

        /**
         * 供外部类反序列化本类数组使用
         */
        @Override
        public Team[] newArray(int size) {
            return new Team[size];
        }

        /**
         * 从Parcel中读取数据
         */
        @Override
        public Team createFromParcel(Parcel source) {
            return new Team(source);
        }
    };

    @Override
    public String toString() {
        return "Team{ " +
                "teamID=" + teamID +
                " , teamName='" + teamName + '\'' +
                " , description='" + description + '\'' +
                " , destinationName='" + destinationName + '\'' +
                " , destinationCity='" + destinationCity + '\'' +
                " , destinationLongitude='" + destinationLongitude + '\'' +
                " , destinationLatitude='" + destinationLatitude + '\'' +
                " , departureDatetime='" + CommonUtils.dateToString(departureDateTime, formatDateType2) + '\'' +
                " , principalID=" + principalID +
                " , principalName='" + principalName + '\'' +
                " , principalTel='" + principalTel + '\'' +
                " , scheduleID=" + scheduleID +
                " }";
    }

    public int getTeamID() {
        return teamID;
    }

    public void setTeamID(int teamID) {
        this.teamID = teamID;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDestinationName() {
        return destinationName;
    }

    public void setDestinationName(String destinationName) {
        this.destinationName = destinationName;
    }

    public String getDestinationCity() {
        return destinationCity;
    }

    public void setDestinationCity(String destinationCity) {
        this.destinationCity = destinationCity;
    }

    public double getDestinationLongitude() {
        return destinationLongitude;
    }

    public void setDestinationLongitude(double longitude) {
        this.destinationLongitude = longitude;
    }

    public double getDestinationLatitude() {
        return destinationLatitude;
    }

    public void setDestinationLatitude(double latitude) {
        this.destinationLatitude = latitude;
    }

    public Date getDepartureDateTime() {
        return departureDateTime;
    }

    public void setDepartureDateTime(Date departureDateTime) {
        this.departureDateTime = departureDateTime;
    }

    public int getPrincipalID() {
        return principalID;
    }

    public void setPrincipalID(int principalID) {
        this.principalID = principalID;
    }

    public String getPrincipalName() {
        return principalName;
    }

    public void setPrincipalName(String principalName) {
        this.principalName = principalName;
    }

    public String getPrincipalTel() {
        return principalTel;
    }

    public void setPrincipalTel(String principalTel) {
        this.principalTel = principalTel;
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(int scheduleID) {
        this.scheduleID = scheduleID;
    }
}
