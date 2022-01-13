package com.example.lab1.network;

/*
Class with urls form where to download both coordinates and weather
 */

import android.content.Context;
import android.util.Log;
import com.example.lab1.data.JSONParser;


public class Downloader {
    private static final String LOG_TAG = Downloader.class.getSimpleName();

    private String mWeatherUrl;
    private String mCityUrl;
    private Context mCtx;

    private JSONParser parser;


    public Downloader(Context ctx) {
        parser = new JSONParser();
        mCtx = ctx;
    }

    //sets URL for download of coordinates
    public String setWeatherURL(String[] coordinates) {
        // for testing
        //mWeatherUrl = "https://maceo.sth.kth.se/weather/forecast?lonLat=lon/" + coordinates[0] + "/lat/" + coordinates[1];
        //mWeatherUrl = "https://maceo.sth.kth.se/weather/forecast?lonLat=lon/14.333/lat/60.383";

        // real data
        mWeatherUrl = "https://opendata-download-metfcst.smhi.se/api/category/pmp3g/version/2/geotype/point/lon/" + coordinates[0] + "/lat/" + coordinates[1] + "/data.json";

        Log.d(LOG_TAG, "Coordinate link accessed");
        return mWeatherUrl;
    }

    //sets URL for download of weather JSON
    public String setCityURL(String cityName) {

        // for testing
        //mCityUrl = "https://maceo.sth.kth.se/weather/search?location=Sigfridstorp";
        //mCityUrl = "https://maceo.sth.kth.se/weather/search?location=" + cityName;

        // real data
        mCityUrl = "https://www.smhi.se/wpt-a/backend_solr/autocomplete/search/" + cityName;

        Log.d(LOG_TAG, "Weather link accessed");

        return mCityUrl;
    }
}

