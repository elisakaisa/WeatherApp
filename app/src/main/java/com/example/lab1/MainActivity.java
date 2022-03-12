package com.example.lab1;
/*
Elisa Perini
KTH, sports technology
HT 2021, CM2001 Mobile applications
Lab1: weather app
 */

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.toolbox.JsonArrayRequest;
import com.example.lab1.data.DataStorage;
import com.example.lab1.data.JSONParser;
import com.example.lab1.data.MeteoModel;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lab1.data.MeteoList;
import com.example.lab1.network.Downloader;
import com.example.lab1.recyclerview.MeteoAdapter;
import com.example.lab1.recyclerview.WeatherRecycler;

import org.json.JSONArray;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity {
    //log variable
    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    // data variables
    private List<MeteoModel> meteoList;
    private String mCity;
    private static long lastDownload = 0;
    private String[] mCoordinates;

    // parser & storage
    private JSONParser parser;
    private DataStorage mDataStorage;

    // ui variables
    private TextView approvedTimeView;
    private RecyclerView recyclerView;
    private TextView textViewNet;
    private TextView textViewLoc;
    private AutoCompleteTextView inputCity;

    // Volley
    private RequestQueue mRequestQueue;

    // networking variables
    public Downloader downloader;
    private static boolean isConnected; //=true if connected, otherwise false
    private static final int DOWNLOAD_UPDATE_INTERVAL = 3600000; //ms (update every 1h)
    private static final int NETWORK_CHECK_INTERVAL = 2000; //ms (check network every 2 seconds)
    private final Handler timerHandler = new Handler();

    // runs in onStart
    private final Runnable timerRunnable = new Runnable() {
        //check network connection
        @Override
        public void run() {
            ConnectivityManager connectivityManager = (ConnectivityManager) getApplication()
                    .getApplicationContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
            isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();

            // inform user of internet connection
            if (isConnected) textViewNet.setText(R.string.net);
            else textViewNet.setText(R.string.nonet);

            // update weather data
            if (isConnected &&  (System.currentTimeMillis() - lastDownload) > DOWNLOAD_UPDATE_INTERVAL) {
                if (mDataStorage != null){
                    // update with stored data
                    fillRecyclerView(mDataStorage.getMeteoList());
                    Log.d(LOG_TAG, "weather updated from serialization, is connected");
                    printApprovedTime(mDataStorage.getMeteoList());
                    printCityName(mDataStorage.getMeteoList());
                    lastDownload = System.currentTimeMillis();
                    Toast.makeText(getApplicationContext(), "Weather updated with old data", Toast.LENGTH_SHORT).show();
                }
            } else if (!isConnected &&  (System.currentTimeMillis() - lastDownload) > DOWNLOAD_UPDATE_INTERVAL)
                if (mDataStorage != null){
                    fillRecyclerView(mDataStorage.getMeteoList());
                    Log.d(LOG_TAG, "weather updated from serialization, is not connected");
                    printApprovedTime(mDataStorage.getMeteoList());
                    printCityName(mDataStorage.getMeteoList());
                    Toast.makeText(getApplicationContext(), "Weather updated with old data",  Toast.LENGTH_SHORT).show();
                }

            timerHandler.postDelayed(this, NETWORK_CHECK_INTERVAL);
            //Log.d(LOG_TAG, "Timer: Is connected? " + isConnected);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // data
        meteoList = MeteoList.getInstance(); // get the singleton list

        // ui
        approvedTimeView = findViewById(R.id.approvedtime_view);
        recyclerView = findViewById(R.id.recycler_view);
        textViewNet = findViewById(R.id.netView);
        textViewLoc = findViewById(R.id.textView_loc);
        inputCity = findViewById(R.id.editText_city_input);

        //Autocomplete city array
        String[] cities = getResources().getStringArray(R.array.cities_array);
        ArrayAdapter autoCompleteAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, cities);
        inputCity.setAdapter(autoCompleteAdapter);
        inputCity.setThreshold(0);

        // Volley
        mRequestQueue = Volley.newRequestQueue(this);
        downloader = new Downloader(this);

        // parser
        parser = new JSONParser();

        // deserialization
        deserialiseData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        lastDownload = 0; //when screen rotates, weather updated from serialization
        timerHandler.postDelayed(timerRunnable, 0);
        deserialiseData();
    }

    @Override
    protected void onPause() {
        super.onPause();
        lastDownload = 0;
        timerHandler.removeCallbacks(timerRunnable);
        mRequestQueue.cancelAll(this);
    }

    public void onSet(View view) {
        // first check connection
        if (isConnected) {
            mCity = inputCity.getText().toString(); //update city
            // check if there is any input
            if (mCity.isEmpty()) Toast.makeText(this, "No location entered", Toast.LENGTH_SHORT).show();
            else {
                postVolleyRequest(mCity);
                lastDownload = System.currentTimeMillis();
            }
        } else Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show();
    }

    // volley request, called in onSet
    public void postVolleyRequest(String cityName) {
        // city to coordinates
        String mCityUrl = downloader.setCityURL(cityName);
        JsonArrayRequest cityRequest = new JsonArrayRequest(Request.Method.GET,
                mCityUrl,
                null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            mCoordinates = parser.getCity(response);
                            String mWeatherUrl = downloader.setWeatherURL(mCoordinates);

                            // get weather
                            JsonObjectRequest weatherRequest = new JsonObjectRequest(Request.Method.GET,
                                    mWeatherUrl,
                                    null,
                                    response1 -> {
                                        try {
                                            List<MeteoModel> newWeather = parser.getMeteo(response1);
                                            if (meteoList != null) {
                                                meteoList.clear();
                                                meteoList.addAll(newWeather);
                                            } else meteoList = newWeather;
                                            // weather displayed in app + serialization
                                            fillRecyclerView(meteoList);
                                            serialiseData(meteoList);

                                            printApprovedTime(meteoList);
                                            printCityName(meteoList);

                                            Toast.makeText(getApplicationContext(), "Download completed", Toast.LENGTH_SHORT).show();

                                        } catch (Exception e) {
                                            Log.i("error whilst parsing", e.toString());
                                            createMsgDialog("Parsing error", "Corrupt data").show();
                                        }
                                    },
                                    errorListener);
                            weatherRequest.setTag(this);
                            mRequestQueue.add(weatherRequest);
                        } catch (Exception e) {
                            Log.i("error whilst parsing", e.toString());
                            createMsgDialog("Location out of bounds", "Please enter valid location").show();
                        }
                    }
                },
                errorListener);
        cityRequest.setTag(this);
        mRequestQueue.add(cityRequest);
    }

    // fills the recycler view with the weather
    private void fillRecyclerView(List<MeteoModel> meteoData) {
        ArrayList<MeteoList> itemList = new ArrayList<>();
        for (MeteoModel instantMeteo : meteoData) {
            String time = instantMeteo.getTimestamp();
            String temperature = instantMeteo.getTemperature() + "°C";
            String cloud = instantMeteo.getCloud();
            int weatherSymbol = instantMeteo.getSymbol();
            String rain = instantMeteo.getRain() + " mm/h"; // mm/h = kg/m²/h
            String precipitation = instantMeteo.getPrecipitation();
            String wind = "Wind: "+ instantMeteo.getWind() + " m/s";
            int color = instantMeteo.getTemperatureColor();
            itemList.add(new WeatherRecycler(time, temperature, color, cloud, weatherSymbol, rain, wind, precipitation));
        }
        RecyclerView.Adapter<MeteoAdapter.ViewHolder> adapter = new MeteoAdapter(itemList);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.d(LOG_TAG, "Filled recycler view");
    }

    //output approved time
    private void printApprovedTime(List<MeteoModel> meteoData) {
        MeteoModel firstMeteo = meteoData.get(0);
        String approvedTime = firstMeteo.getApprovedTime();
        String date = approvedTime.substring(0, 10);
        String time = approvedTime.substring(11, 19);
        String approved_time = getString(R.string.approvedTime) + "\n"+ date + "\n" +time;
        approvedTimeView.setText(approved_time);
        Log.d(LOG_TAG, "approved time printed");
    }

    // output city name
    private void printCityName(List<MeteoModel> meteoData) {
        MeteoModel firstMeteo = meteoData.get(0);
        String city = firstMeteo.getCityName();
        textViewLoc.setText(city);
        Log.d(LOG_TAG, "Printed location");
    }

    private final Response.ErrorListener errorListener = error -> {
        Log.i("Volley error", error.toString());
        createMsgDialog("Network error", "Couldn't download the data").show();
    };

    /*--------------- DATA STORAGE -------------------------*/
    private void serialiseData(List<MeteoModel> ml){
        //adapted from joshuadonloan.gitbooks.io/../serializable.html
        DataStorage datastorage = new DataStorage(ml);
        try{
            FileOutputStream fos = openFileOutput("datastorage.ser", Context.MODE_PRIVATE);
            // Wrapping our file stream
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            // Writing the serializable object to the file
            oos.writeObject(datastorage);
            // Closing our object stream which also closes the wrapped stream.
            oos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deserialiseData(){
        //adapted from joshuadonloan.gitbooks.io/../serializable.html
        try{
            FileInputStream fin = openFileInput("datastorage.ser");
            // Wrapping our stream
            ObjectInputStream oin = new ObjectInputStream(fin);
            // Reading in our object
            mDataStorage = (DataStorage)oin.readObject();
            // Closing our object stream which also closes the wrapped stream
            oin.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // Alert dialogs for error messages
    private AlertDialog createMsgDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", (dialog, id) -> {});
        return builder.create();
    }

}

