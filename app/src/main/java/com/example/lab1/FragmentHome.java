package com.example.lab1;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.lab1.data.DataStorage;
import com.example.lab1.data.MeteoList;
import com.example.lab1.data.MeteoModel;
import com.example.lab1.recyclerview.MeteoAdapter;
import com.example.lab1.recyclerview.WeatherRecycler;
import com.example.lab1.viewModel.ConnectivityVM;
import com.example.lab1.viewModel.WeatherViewModel;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link FragmentHome#newInstance} factory method to
 * create an instance of this fragment.
 */
public class FragmentHome extends Fragment {

    // data variables
    private static long lastDownload = 0;
    private static final int DOWNLOAD_UPDATE_INTERVAL = 3600000; //ms (update every 1h)

    /*------ PARSER & STORAGE --------*/
    private DataStorage mDataStorage;

    /*---------- HOOKS --------------*/
    private TextView approvedTimeView;
    private RecyclerView recyclerView;
    private TextView textViewLoc;
    private AutoCompleteTextView inputCity;

    /*---------- VM --------------*/
    private ConnectivityVM connectivityVM;
    private WeatherViewModel weatherVM;

    // networking variables
    private boolean isConnected; //=true if connected, otherwise false

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

        /*------------ HOOKS ---------------*/
        approvedTimeView = view.findViewById(R.id.approvedtime_view);
        recyclerView = view.findViewById(R.id.recycler_view);
        textViewLoc = view.findViewById(R.id.textView_loc);
        inputCity = view.findViewById(R.id.editText_city_input);
        Button set = view.findViewById(R.id.set);

        /*------------ VM ---------------*/
        AtomicInteger counter = new AtomicInteger();
        connectivityVM = new ViewModelProvider(requireActivity()).get(ConnectivityVM.class);
        weatherVM = new ViewModelProvider(requireActivity()).get(WeatherViewModel.class);
        connectivityVM.getConnection().observe(requireActivity(), connected -> {
            isConnected = connected;

            if (counter.get() == 0) { // makes sure it only happens once and not every 10 secs
                // deserialization
                deserialiseData();
                updateUIWithDefaultData(connected);
            }
            counter.set(counter.get() + 1);
        });

        //Autocomplete city array
        String[] cities = getResources().getStringArray(R.array.cities_array);
        ArrayAdapter autoCompleteAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1, cities);
        inputCity.setAdapter(autoCompleteAdapter);
        inputCity.setThreshold(0);

        /*-------------- LISTENERS --------------*/
        set.setOnClickListener(v -> onSet());

        weatherVM.setWeatherListener((list) -> {
            fillRecyclerView(list);
            serialiseData(list);
            printTimeAndCity(list);
            Toast.makeText(getActivity(), "Download completed", Toast.LENGTH_SHORT).show();
        });

        return view;
    }

    public void updateUIWithDefaultData(Boolean conn) {
        // if internet connection, update with default city, else with old data
        if (conn) {
            String defaultCity = "Huddinge";
            weatherVM.loadWeatherForecast(defaultCity);
            lastDownload = System.currentTimeMillis();
        } else {
            if (mDataStorage != null) {
                // update with stored data
                fillRecyclerView(mDataStorage.getMeteoList());
                Log.d("frag", "weather updated from serialization, is connected");
                printTimeAndCity(mDataStorage.getMeteoList());
                Toast.makeText(getActivity(), "Weather updated with old data", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onSet(){
        if (isConnected) { // check connection
            String mCity = inputCity.getText().toString(); //update city
            // check if there is any input
            if (mCity.isEmpty()) Toast.makeText(getActivity(), "No location entered", Toast.LENGTH_SHORT).show();
            else {
                weatherVM.loadWeatherForecast(mCity);
                lastDownload = System.currentTimeMillis();
            }
        } else Toast.makeText(getActivity(), "No internet connection", Toast.LENGTH_SHORT).show();
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
    }

    //output approved time & city name
    private void printTimeAndCity(List<MeteoModel> meteoData) {
        MeteoModel firstMeteo = meteoData.get(0);
        String approvedTime = firstMeteo.getApprovedTime();
        String date = approvedTime.substring(0, 10);
        String time = approvedTime.substring(11, 19);
        String approved_time = getString(R.string.approvedTime) + "\n"+ date + "\n" +time;
        approvedTimeView.setText(approved_time);
        String city = firstMeteo.getCityName();
        textViewLoc.setText(city);
    }

    /*--------------- DATA STORAGE -------------------------*/
    private void serialiseData(List<MeteoModel> ml){
        //adapted from joshuadonloan.gitbooks.io/../serializable.html
        DataStorage datastorage = new DataStorage(ml);
        try{
            FileOutputStream fos = getContext().openFileOutput("datastorage.ser", Context.MODE_PRIVATE);
            // Wrapping our file stream
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            // Writing the serializable object to the file
            oos.writeObject(datastorage);
            // Closing our object stream which also closes the wrapped stream.
            oos.close();
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void deserialiseData(){
        //adapted from joshuadonloan.gitbooks.io/../serializable.html
        try{
            FileInputStream fin = getContext().openFileInput("datastorage.ser");
            // Wrapping our stream
            ObjectInputStream oin = new ObjectInputStream(fin);
            // Reading in our object
            mDataStorage = (DataStorage)oin.readObject();
            // Closing our object stream which also closes the wrapped stream
            oin.close();
        } catch (Exception e) { e.printStackTrace(); }
    }
}