package com.example.lab1.viewModel;

import com.example.lab1.data.MeteoModel;

import java.util.List;

public interface WeatherForecastInterface {
    void onWeatherFetched(List<MeteoModel> list);
}
