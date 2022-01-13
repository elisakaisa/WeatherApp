package com.example.lab1.data;
/*
based on https://gits-15.sys.kth.se/anderslm/Android-Volley-RecyclerView/blob/master/app/src/main/java/se/kth/anderslm/jokes/model/JokesList.java
 */

import com.example.lab1.data.Meteo;
import java.util.ArrayList;
import java.util.List;

public class MeteoList {

    private static List<Meteo> theMeteo;

    // private constructor to force the use of getInstance() to get an/the object
    public MeteoList() {
    }

    public static List<Meteo> getInstance() {
        if (theMeteo == null)
            theMeteo = new ArrayList<>();
        return theMeteo;
    }
}
