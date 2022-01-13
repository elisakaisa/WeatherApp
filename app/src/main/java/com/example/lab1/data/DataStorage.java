package com.example.lab1.data;
/*
for serialization
 */

import java.io.Serializable;
import java.util.List;

public class DataStorage implements Serializable {
    private List<Meteo> mMeteoList;

    public DataStorage(List<Meteo> MeteoList){ mMeteoList = MeteoList; }
    public List<Meteo> getMeteoList(){return mMeteoList;}

}
