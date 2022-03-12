package com.example.lab1.data;

/*
parses JSON file into a list
based on https://gits-15.sys.kth.se/anderslm/Android-Volley-RecyclerView/blob/master/app/src/main/java/se/kth/anderslm/jokes/parser/JokeParser.java
 */

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.util.Log;

import com.example.lab1.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class JSONParser {

    private static final String
            LOG_TAG = JSONParser.class.getSimpleName(),
            REFERENCE_TIME = "referenceTime",
            VALID_TIME = "validTime",
            APPROVED_TIME = "approvedTime",
            LATITUDE = "lat",
            LONGITUDE = "lon",
            //CLOUD = "tcc_mean",
            TEMPERATURE = "t",
            WEATHER = "Wsymb2",
            RAIN = "pmean",
            PRECIPITATION = "pcat",
            WIND = "ws"; // wind speed
    private String mCityName;

    //method to get the coordinates form the city name
    public String[] getCity(JSONArray cityArray) throws JSONException{
        String[] coordinates = new String[2];
        JSONObject cityObject = cityArray.getJSONObject(0);
        mCityName = cityObject.getString("place");
        coordinates[0] = String.valueOf(cityObject.getDouble(LONGITUDE));
        coordinates[1] = String.valueOf(cityObject.getDouble(LATITUDE));

        //trim coordinates
        if (coordinates[0].length() > 6)
            coordinates[0] = coordinates[0].substring(0, 6);
        if (coordinates[1].length() > 6)
            coordinates[1] = coordinates[1].substring(0, 6);

        return coordinates;
    }

    // method to get a list with all the weather data
    public List<MeteoModel> getMeteo(JSONObject meteoObj) throws JSONException, ParseException {

        String referencetime = meteoObj.getString(REFERENCE_TIME);
        String approvedtime = meteoObj.getString(APPROVED_TIME);
        Log.i(LOG_TAG, "ref time" + referencetime);

        JSONArray timeSeries = meteoObj.getJSONArray("timeSeries");

        List<MeteoModel> meteoData = new ArrayList<>();
        Log.d(LOG_TAG, "parser initialized ");

        for (int i = 0; i < timeSeries.length(); i++) {
            JSONObject parametersAtTime = timeSeries.getJSONObject(i);
            String time = parametersAtTime.getString(VALID_TIME);
            //Log.d(LOG_TAG, "valid Time: " + time);

            JSONArray parameters = parametersAtTime.getJSONArray("parameters");

            MeteoModel instantWeather = new MeteoModel();
            meteoData.add(instantWeather);
            instantWeather.setTimestamp(getCleanTime(time));
            instantWeather.setApprovedTime(approvedtime);
            instantWeather.setReferenceTime(referencetime);
            instantWeather.setCityName(mCityName);

            for (int j = 0; j < parameters.length(); j++) {
                JSONObject parameter = parameters.getJSONObject(j);
                String name = parameter.getString("name");
                JSONArray valueArray = parameter.getJSONArray("values");

                if (TEMPERATURE.equals(name)) {
                    instantWeather.setTemperature(valueArray.getDouble(0));
                    int color;
                    if (valueArray.getDouble(0) < 0) color = Color.parseColor("#0000FF"); //blue
                    if (valueArray.getDouble(0) >= 20) color = Color.parseColor("#FF0000"); //red
                    else color = Color.parseColor("#000000"); //black
                    instantWeather.setTemperatureColor(color);
                }
                if (WEATHER.equals(name)) {
                    instantWeather.setCloud(getTextCloudCoverage(valueArray.getInt(0)));
                    instantWeather.setSymbol(getSymbol(valueArray.getInt(0), instantWeather.getTimestamp()));
                }
                if (RAIN.equals(name)) instantWeather.setRain(valueArray.getDouble(0));
                if (PRECIPITATION.equals(name)) instantWeather.setPrecipitation(getTextPrecipitation(valueArray.getInt(0)));
                if (WIND.equals(name)) instantWeather.setWind(valueArray.getDouble(0));
            }
        }
        return meteoData;
    }

    // changes date and time to a more readable format
    private static String getCleanTime(String validOriginalTime) throws ParseException {
        String oldFormat = "yyyy-MM-dd";
        String newFormat = "EEEE\nMMM d";
        @SuppressLint("SimpleDateFormat") SimpleDateFormat sdFormat = new SimpleDateFormat(oldFormat);
        Date validDate = sdFormat.parse(validOriginalTime.substring(0, 10));
        sdFormat.applyPattern(newFormat);
        String validDateS = sdFormat.format(validDate);
        String validTime = validOriginalTime.substring(11,16);
        String validCleanTime = validDateS + " " + validTime;
        return validCleanTime;
    }

    private static String getTextPrecipitation(int precipitationValue) {
        switch (precipitationValue){
            case 0:
                return "No precipitation";
            case 1:
                return "Snow";
            case 2:
                return "Snow and rain";
            case 3:
                return "Rain";
            case 4:
                return "Drizzle";
            case 5:
                return "Freezing rain";
            case 6:
                return "Freezing drizzle";
            default:
                return "No data";
        }
    }

    // funny/dumb text to input the cloud coverage
    private static String getTextCloudCoverage(int cloudValue) {
        switch (cloudValue) {
            case 1:
                return "Sunny one so true, I love you";
            case 2:
                return "Here comes the sun";
            case 3:
                return "I've got a pocketful of sunshine";
            case 4:
                return "Halfclear sky";
            case 5:
                return "Cloudy sky";
            case 6:
                return "take your D vitamins";
            case 7:
                return "Zero visibility";
            case 8:
                return "Don't rain on my parade";
            case 9:
                return "Come on with the rain, I've a smile on my face";
            case 10:
                return "I'm laughing at clouds, so dark up above";
            case 11:
                return "Let the stormy clouds chase everyone from the place";
            case 12:
                return "Light sleet showers";
            case 13:
                return "Moderate sleet showers";
            case 14:
                return "Oh the weather outside is frightful";
            case 15:
                return "Light snow showers";
            case 16:
                return "The snow glows white on the mountains tonight";
            case 17:
                return "But baby it's cold outside";
            case 18:
                return "Raindrops on roses and whiskers on kittens";
            case 19:
                return "I'm singing in the rain";
            case 20:
                return "It's raining men";
            case 21:
                return "Thunder, aah, thunder, aah";
            case 22:
                return "useless weather";
            case 23:
                return "Let the storm rage on";
            case 24:
                return "How I'll hate going out in the storm";
            case 25:
                return "Let is snow (x3)";
            case 26:
                return "The cold never bothered me anyways";
            case 27:
                return "It doesn't show signs of stopping";
            default:
                return "What's going on?";
        }
    }

    // sets weather symbols
    private static int getSymbol(int cloudValue, String time) {
        boolean isDay = Integer.parseInt(time.substring(time.length()-5, time.length()-3)) > 6
                && Integer.parseInt(time.substring(time.length()-5, time.length()-3)) < 18;
        if (isDay) {
            switch (cloudValue) {
                case 1:
                    return R.drawable.day_1;
                case 2:
                    return R.drawable.day_2;
                case 3:
                    return R.drawable.day_3;
                case 4:
                    return R.drawable.day_4;
                case 5:
                    return R.drawable.day_5;
                case 6:
                    return R.drawable.day_6;
                case 7:
                    return R.drawable.day_7;
                case 8:
                    return R.drawable.day_8;
                case 9:
                    return R.drawable.day_9;
                case 10:
                    return R.drawable.day_10;
                case 11:
                    return R.drawable.day_11;
                case 12:
                    return R.drawable.day_12;
                case 13:
                    return R.drawable.day_13;
                case 14:
                    return R.drawable.day_14;
                case 15:
                    return R.drawable.day_15;
                case 16:
                    return R.drawable.day_16;
                case 17:
                    return R.drawable.day_17;
                case 18:
                    return R.drawable.day_18;
                case 19:
                    return R.drawable.day_19;
                case 20:
                    return R.drawable.day_20;
                case 21:
                    return R.drawable.day_21;
                case 22:
                    return R.drawable.day_22;
                case 23:
                    return R.drawable.day_23;
                case 24:
                    return R.drawable.day_24;
                case 25:
                    return R.drawable.day_25;
                case 26:
                    return R.drawable.day_26;
                case 27:
                    return R.drawable.day_27;
                default:
                    return R.drawable.nodata;
            }
        } else {
            switch (cloudValue) {
                case 1:
                    return R.drawable.night_1;
                case 2:
                    return R.drawable.night_2;
                case 3:
                    return R.drawable.night_3;
                case 4:
                    return R.drawable.night_4;
                case 5:
                    return R.drawable.night_5;
                case 6:
                    return R.drawable.night_6;
                case 7:
                    return R.drawable.night_7;
                case 8:
                    return R.drawable.night_8;
                case 9:
                    return R.drawable.night_9;
                case 10:
                    return R.drawable.night_10;
                case 11:
                    return R.drawable.night_11;
                case 12:
                    return R.drawable.night_12;
                case 13:
                    return R.drawable.night_13;
                case 14:
                    return R.drawable.night_14;
                case 15:
                    return R.drawable.night_15;
                case 16:
                    return R.drawable.night_16;
                case 17:
                    return R.drawable.night_17;
                case 18:
                    return R.drawable.night_18;
                case 19:
                    return R.drawable.night_19;
                case 20:
                    return R.drawable.night_20;
                case 21:
                    return R.drawable.night_21;
                case 22:
                    return R.drawable.night_22;
                case 23:
                    return R.drawable.night_23;
                case 24:
                    return R.drawable.night_24;
                case 25:
                    return R.drawable.night_25;
                case 26:
                    return R.drawable.night_26;
                case 27:
                    return R.drawable.night_27;
                default:
                    return R.drawable.nodata;
            }
        }
    }

}