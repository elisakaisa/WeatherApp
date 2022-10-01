package com.example.lab1.viewModel;

import android.app.Application;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lab1.data.JSONParser;
import com.example.lab1.data.MeteoList;
import com.example.lab1.data.MeteoModel;
import com.example.lab1.network.Downloader;

import org.json.JSONArray;

import java.util.List;

public class WeatherViewModel extends AndroidViewModel {
    public WeatherViewModel(@NonNull Application application) {
        super(application);
        mRequestQueue = Volley.newRequestQueue(application.getApplicationContext());
    }

    private final JSONParser cParser = new JSONParser();
    private final RequestQueue mRequestQueue;
    private WeatherForecastInterface listener;

    private MutableLiveData<List<MeteoModel>> weatherForecast;

    public LiveData<List<MeteoModel>> getWeatherForecast() {
        if (weatherForecast == null) {
            weatherForecast = new MutableLiveData<>();
        }
        return weatherForecast;
    }

    public void setWeatherListener(WeatherForecastInterface listener) {
        // Assign the listener implementing events interface that will receive the events
        this.listener = listener;
    }

    public void loadWeatherForecast(String cityName) {
        // init
        Downloader downloader = new Downloader();
        // city to coordinates
        String mCityUrl = downloader.setCityURL(cityName);
        JsonArrayRequest cityRequest = new JsonArrayRequest(Request.Method.GET,
                mCityUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            String[] mCoordinates;
                            mCoordinates = cParser.getCity(response);
                            String mWeatherUrl = downloader.setWeatherURL(mCoordinates);

                            // get weather
                            JsonObjectRequest weatherRequest = new JsonObjectRequest(Request.Method.GET,
                                    mWeatherUrl,
                                    null,
                                    response1 -> {
                                        try {
                                            List<MeteoModel> meteoList = MeteoList.getInstance();
                                            List<MeteoModel> newWeather = cParser.getMeteo(response1);
                                            if (meteoList != null) {
                                                meteoList.clear();
                                                meteoList.addAll(newWeather);
                                            } else meteoList = newWeather;
                                            // weather displayed in app + serialization
                                            listener.onWeatherFetched(meteoList);

                                        } catch (Exception e) {
                                            Log.i("error whilst parsing", e.toString());
                                            //createMsgDialog("Parsing error", "Corrupt data").show();
                                        }
                                    },
                                    errorListener);
                            weatherRequest.setTag(this);
                            mRequestQueue.add(weatherRequest);
                        } catch (Exception e) {
                            Log.i("error whilst parsing", e.toString());
                            //createMsgDialog("Location out of bounds", "Please enter valid location").show();
                        }
                    }
                },
                errorListener);
        cityRequest.setTag(this);
        mRequestQueue.add(cityRequest);
    }

    private final Response.ErrorListener errorListener = error -> {
        Log.i("Volley error", error.toString());
    };


}
