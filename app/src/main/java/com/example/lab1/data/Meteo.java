package com.example.lab1.data;

/*
Helpful functions for the json Parser
based on https://gits-15.sys.kth.se/anderslm/Android-Volley-RecyclerView/blob/master/app/src/main/java/se/kth/anderslm/jokes/model/Joke.java
 */

import java.io.Serializable;
import java.util.Objects;

public class Meteo implements Serializable {
    // time and city variables
    private String mTimestamp;
    private String mApprovedTime;
    private String mReferenceTime;
    private String mCityName;
    //weather variables
    private String mCloud;
    private int mSymbol;
    private double mTemperature;
    private double mRain;
    private String mPrecipitation;
    private double mWind;

    public void setTimestamp(String timestamp) {
        mTimestamp = timestamp;
    }
    public void setApprovedTime(String approvedTime) {
        mApprovedTime = approvedTime;
    }
    public void setReferenceTime(String referenceTime) {
        mReferenceTime = referenceTime;
    }
    public void setCityName(String cityName) {mCityName = cityName;}

    public void setCloud(String cloud) {
        mCloud = cloud;
    }
    public void setSymbol(int symbol) {
        mSymbol = symbol;
    }

    public void setTemperature(double temperature) {
        mTemperature = temperature;
    }
    public void setRain(double rain) { mRain = rain; }
    public void setPrecipitation(String precipitation) {mPrecipitation = precipitation;}
    public void setWind(double wind) { mWind = wind; }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Meteo meteo = (Meteo) o;
        return mTimestamp == meteo.mTimestamp;
    }

    @Override
    public int hashCode() {
        return Objects.hash(mTimestamp);
    }

    public String getCloud() { return mCloud; }
    public int getSymbol() {
        return mSymbol;
    }

    public int getTemperature() { return (int) mTemperature; }
    public double getRain() { return (double) mRain;}
    public String getPrecipitation() {return mPrecipitation;}
    public double getWind() { return (double) mWind;}

    public String getTimestamp() { return mTimestamp; }
    public String getApprovedTime() { return mApprovedTime; }
    public String getReferenceTime() { return mReferenceTime; }
    public String getCityName() { return mCityName; }
}