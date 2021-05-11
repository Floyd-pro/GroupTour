package com.zjnu.grouptour.bean;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * @author luchen
 * @Date 2021/4/20 21:55
 * @Description 队伍行程表类
 */
public class Schedule implements Parcelable {
    private int scheduleID;
    private String scheduleName;
    private String scheduleInfo;

    public Schedule() {

    }

    public Schedule(String name) {
        this.scheduleName = name;
    }

    public Schedule(int scheduleID, String scheduleName, String scheduleInfo) {
        this.scheduleID = scheduleID;
        this.scheduleName = scheduleName;
        this.scheduleInfo = scheduleInfo;
    }


    protected Schedule(Parcel in) {
        scheduleID = in.readInt();
        scheduleName = in.readString();
        scheduleInfo = in.readString();
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(scheduleID);
        parcel.writeString(scheduleName);
        parcel.writeString(scheduleInfo);
    }

    public static final Creator<Schedule> CREATOR = new Creator<Schedule>() {

        /**
         * 供外部类反序列化本类数组使用
         */
        @Override
        public Schedule[] newArray(int size) {
            return new Schedule[size];
        }

        /**
         * 从Parcel中读取数据
         */
        @Override
        public Schedule createFromParcel(Parcel in) {
            return new Schedule(in);
        }
    };

    @Override
    public String toString() {
        return "Schedule{ " +
                "scheduleID=" + scheduleID +
                " , scheduleName='" + scheduleName + '\'' +
                " , scheduleInfo='" + scheduleInfo + '\'' +
                " }";
    }

    public int getScheduleID() {
        return scheduleID;
    }

    public void setScheduleID(int scheduleID) {
        this.scheduleID = scheduleID;
    }

    public String getScheduleName() {
        return scheduleName;
    }

    public void setScheduleName(String scheduleName) {
        this.scheduleName = scheduleName;
    }

    public String getScheduleInfo() {
        return scheduleInfo;
    }

    public void setScheduleInfo(String scheduleInfo) {
        this.scheduleInfo = scheduleInfo;
    }
}
