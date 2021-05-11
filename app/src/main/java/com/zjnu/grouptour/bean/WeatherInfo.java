package com.zjnu.grouptour.bean;

import android.graphics.drawable.Drawable;

import java.util.Date;

/**
 * @author luchen
 * @Date 2021/4/23 23:53
 * @Description 天气信息
 */
public class WeatherInfo {
    private String date;
    private String dayOfWeek;
    private int minTemp;
    private int maxTemp;
    private String weatherPhenomenon;
    private String windLevel;
    private Drawable imgWeather;

    public WeatherInfo() {
        this.date = "获取天气预报数据失败！";
        this.dayOfWeek = "";
        this.minTemp = 0;
        this.maxTemp = 0;
        this.weatherPhenomenon = "";
        this.windLevel = "";
        this.imgWeather = null;
    }

    public WeatherInfo(String date, String dayOfWeek, int minTemp, int maxTemp, String weatherPhenomenon, String windLevel, Drawable imgWeather) {
        this.date = date;
        this.dayOfWeek = dayOfWeek;
        this.minTemp = minTemp;
        this.maxTemp = maxTemp;
        this.weatherPhenomenon = weatherPhenomenon;
        this.windLevel = windLevel;
        this.imgWeather = imgWeather;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDayOfWeek() {
        return dayOfWeek;
    }

    public void setDayOfWeek(String dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public int getMinTemp() {
        return minTemp;
    }

    public void setMinTemp(int minTemp) {
        this.minTemp = minTemp;
    }

    public int getMaxTemp() {
        return maxTemp;
    }

    public void setMaxTemp(int maxTemp) {
        this.maxTemp = maxTemp;
    }

    public String getWeatherPhenomenon() {
        return weatherPhenomenon;
    }

    public void setWeatherPhenomenon(String weatherPhenomenon) {
        this.weatherPhenomenon = weatherPhenomenon;
    }

    public String getWindLevel() {
        return windLevel;
    }

    public void setWind(String windLevel) {
        this.windLevel = windLevel;
    }

    public Drawable getImgWeather() {
        return imgWeather;
    }

    public void setImgWeather(Drawable imgWeather) {
        this.imgWeather = imgWeather;
    }
}
