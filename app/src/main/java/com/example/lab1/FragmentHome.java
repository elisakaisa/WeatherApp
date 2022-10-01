package com.example.lab1;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.example.lab1.data.DataStorage;
import com.example.lab1.data.JSONParser;
import com.example.lab1.data.MeteoList;
import com.example.lab1.data.MeteoModel;
import com.example.lab1.network.Downloader;
import com.example.lab1.recyclerview.MeteoAdapter;
import com.example.lab1.recyclerview.WeatherRecycler;
import com.example.lab1.viewModel.ConnectivityVM;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends Fragment {

    // data variables
    private List<MeteoModel> meteoList;
    private String mCity;
    private static long lastDownload = 0;
    private String[] mCoordinates;

    /*------ PARSER & STORAGE --------*/
    private JSONParser parser;
    private DataStorage mDataStorage;

    /*---------- HOOKS --------------*/
    private TextView approvedTimeView;
    private RecyclerView recyclerView;
    private TextView textViewNet;
    private TextView textViewLoc;
    private AutoCompleteTextView inputCity;

    // Volley
    private RequestQueue mRequestQueue;

    private ConnectivityVM connectivityVM;

    // networking variables
    public Downloader downloader;
    private static boolean connected; //=true if connected, otherwise false
    /*
            // update weather data
            if (isConnected &&  (System.currentTimeMillis() - lastDownload) > DOWNLOAD_UPDATE_INTERVAL) {
                if (mDataStorage != null){
                    // update with stored data
                    fillRecyclerView(mDataStorage.getMeteoList());
                    Log.d("frag", "weather updated from serialization, is connected");
                    printTimeAndCity(mDataStorage.getMeteoList());
                    lastDownload = System.currentTimeMillis();
                    Toast.makeText(getActivity(), "Weather updated with old data", Toast.LENGTH_SHORT).show();
                }
            } else if (!isConnected &&  (System.currentTimeMillis() - lastDownload) > DOWNLOAD_UPDATE_INTERVAL)
                if (mDataStorage != null){
                    fillRecyclerView(mDataStorage.getMeteoList());
                    Log.d("Frag", "weather updated from serialization, is not connected");
                    printTimeAndCity(mDataStorage.getMeteoList());
                    Toast.makeText(getActivity(), "Weather updated with old data",  Toast.LENGTH_SHORT).show();
        }
    }; */

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public FragmentHome() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FragmentHome.
     */
    // TODO: Rename and change types and number of parameters
    public static FragmentHome newInstance(String param1, String param2) {
        FragmentHome fragment = new FragmentHome();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);
        // Inflate the layout for this fragment

        /*---------- DATA -------------*/
        meteoList = MeteoList.getInstance(); // get the singleton list
        parser = new JSONParser();

        /*------------ HOOKS ---------------*/
        approvedTimeView = view.findViewById(R.id.approvedtime_view);
        recyclerView = view.findViewById(R.id.recycler_view);
        textViewLoc = view.findViewById(R.id.textView_loc);
        inputCity = view.findViewById(R.id.editText_city_input);
        Button set = view.findViewById(R.id.set);

        /*------------ VM ---------------*/
        connectivityVM = new ViewModelProvider(requireActivity()).get(ConnectivityVM.class);

        //Autocomplete city array
        String[] cities = getResources().getStringArray(R.array.cities_array);
        //ArrayAdapter autoCompleteAdapter = new ArrayAdapter(this, android.R.layout.simple_list_item_1, cities);
        //inputCity.setAdapter(autoCompleteAdapter);
        //inputCity.setThreshold(0);

        // Volley
        mRequestQueue = Volley.newRequestQueue(getActivity());
        downloader = new Downloader();

        // deserialization
        deserialiseData();

        /*-------------- LISTENERS --------------*/
        set.setOnClickListener(v -> { onSet(); });

        return view;
    }

    public void onSet(){
        connected = connectivityVM.getIsConnected();
        if (connected) { // check connection
            mCity = inputCity.getText().toString(); //update city
            // check if there is any input
            if (mCity.isEmpty()) Toast.makeText(getActivity(), "No location entered", Toast.LENGTH_SHORT).show();
            else {
                postVolleyRequest(mCity);
                lastDownload = System.currentTimeMillis();
            }
        } else Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
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
                                            printTimeAndCity(meteoList);
                                            Toast.makeText(getActivity(), "Download completed", Toast.LENGTH_SHORT).show();

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
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        Log.d("frag", "Filled recycler view");
    }

    //output approved time & city name
    private void printTimeAndCity(List<MeteoModel> meteoData) {
        MeteoModel firstMeteo = meteoData.get(0);
        String approvedTime = firstMeteo.getApprovedTime();
        String date = approvedTime.substring(0, 10);
        String time = approvedTime.substring(11, 19);
        String approved_time = getString(R.string.approvedTime) + "\n"+ date + "\n" +time;
        approvedTimeView.setText(approved_time);
        Log.d("frag", "approved time printed");
        String city = firstMeteo.getCityName();
        textViewLoc.setText(city);
        Log.d("frag", "Printed location");
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
            /*FileOutputStream fos = openFileOutput("datastorage.ser", Context.MODE_PRIVATE);
            // Wrapping our file stream
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            // Writing the serializable object to the file
            oos.writeObject(datastorage);
            // Closing our object stream which also closes the wrapped stream.
            oos.close();*/
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deserialiseData(){
        //adapted from joshuadonloan.gitbooks.io/../serializable.html
        try{
           /* FileInputStream fin = openFileInput("datastorage.ser");
            // Wrapping our stream
            ObjectInputStream oin = new ObjectInputStream(fin);
            // Reading in our object
            mDataStorage = (DataStorage)oin.readObject();
            // Closing our object stream which also closes the wrapped stream
            oin.close();*/
        } catch (Exception e) { e.printStackTrace(); }
    }

    // Alert dialogs for error messages
    private AlertDialog createMsgDialog(String title, String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title);
        builder.setMessage(message);
        builder.setPositiveButton("Ok", (dialog, id) -> {});
        return builder.create();
    }
}