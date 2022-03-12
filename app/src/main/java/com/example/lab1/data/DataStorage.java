package com.example.lab1.data;
/*
for serialization
 */

import java.io.Serializable;
import java.util.List;

public class DataStorage implements Serializable {
    private List<MeteoModel> mMeteoList;

    public DataStorage(List<MeteoModel> MeteoList){ mMeteoList = MeteoList; }
    public List<MeteoModel> getMeteoList(){return mMeteoList;}

}
