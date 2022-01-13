package com.example.lab1.recyclerview;

// Stuff displayed in the Recycler view, used in MeteoAdapter

import com.example.lab1.data.MeteoList;

public class WeatherRecycler extends MeteoList {
    private final String mTextTime;
    private final String mTextTemperature;
    private final String mTextCloud;
    private final int mWeatherSymbol;
    private final String mTextRain;
    private final String mTextWind;
    private final String mTextPrecipitation;


    public WeatherRecycler(String textTime, String textTemperature, String textCloud, int weatherSymbol, String textRain, String textWind, String textPrecipitation) {

        mTextTime = textTime;
        mTextTemperature = textTemperature;
        mTextCloud = textCloud;
        mWeatherSymbol = weatherSymbol;
        mTextRain = textRain;
        mTextWind = textWind;
        mTextPrecipitation = textPrecipitation;
    }

    public String getTextTime() {
        return mTextTime;
    }
    public String getTextTemperature() {
        return mTextTemperature;
    }
    public String getTextCloud() {
        return mTextCloud;
    }
    public int getWeatherSymbol() {
        return mWeatherSymbol;
    }
    public String getTextRain(){ return mTextRain;}
    public String getTextWind(){ return mTextWind;}
    public String getTextPrecipitation(){ return mTextPrecipitation;}

}
