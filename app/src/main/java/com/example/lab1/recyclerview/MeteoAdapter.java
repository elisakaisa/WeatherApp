package com.example.lab1.recyclerview;
/*
Simple implementation of a RecyclerView.Adapter
loosely based on https://gits-15.sys.kth.se/anderslm/Android-Volley-RecyclerView/blob/master/app/src/main/java/se/kth/anderslm/jokes/JokeAdapter.java
 */

import static android.graphics.Color.*;

import static java.lang.Integer.parseInt;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.lab1.R;
import com.example.lab1.data.MeteoList;


public class MeteoAdapter extends RecyclerView.Adapter<MeteoAdapter.ViewHolder> {
    private ArrayList<MeteoList> mWeather;

    // We use a Singleton class to get a reference to the model object,
    // the same list object managed by MainActivity (and Volley)
    //private final List<Meteo> theMeteo = MeteoList.getInstance();

    // Provide a reference to the views for each data item
    // Complex data items may need more than one view per item, and
    // you provide access to all the views for a data item in a view holder
    public static class ViewHolder extends RecyclerView.ViewHolder {

        public TextView timeTextView;
        public TextView temperatureTextView;
        public TextView cloudTextView;
        public TextView rainTextView;
        public TextView windTextView;
        public TextView precipitationTextView;
        public ImageView weatherSymbolView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            // define click listener for the ViewHolder's view
            timeTextView = itemView.findViewById(R.id.time_text);
            temperatureTextView = itemView.findViewById(R.id.temperature_text);
            cloudTextView = itemView.findViewById(R.id.cloud_text);
            rainTextView = itemView.findViewById(R.id.rain_text);
            windTextView = itemView.findViewById(R.id.wind_text);
            precipitationTextView = itemView.findViewById(R.id.precipitation_text);
            weatherSymbolView = itemView.findViewById(R.id.imageView_weatherSymbol);

        }
    }

    // initialize dataset of the adapter
    public MeteoAdapter(ArrayList<MeteoList> weatherItemsList){
        mWeather = weatherItemsList;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new item view
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.meteo_item, parent, false);
        return new ViewHolder(itemView);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // get elements from dataset & replace the contents of the view with that element
        WeatherRecycler currentmeteo = (WeatherRecycler) mWeather.get(position);
        holder.timeTextView.setText(currentmeteo.getTextTime());
        holder.temperatureTextView.setText(currentmeteo.getTextTemperature());
        holder.temperatureTextView.setTextColor(currentmeteo.getTextTemperatureColor());
        holder.cloudTextView.setText(currentmeteo.getTextCloud());
        holder.rainTextView.setText(currentmeteo.getTextRain());
        holder.windTextView.setText(currentmeteo.getTextWind());
        holder.precipitationTextView.setText(currentmeteo.getTextPrecipitation());
        holder.weatherSymbolView.setImageResource(currentmeteo.getWeatherSymbol());

    }

    // return size of dataset (invoked by layout manager)
    @Override
    public int getItemCount() {
        return mWeather.size();
    }
}
