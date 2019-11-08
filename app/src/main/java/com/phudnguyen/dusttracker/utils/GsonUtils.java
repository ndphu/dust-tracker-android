package com.phudnguyen.dusttracker.utils;


import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class GsonUtils {
    public static final Gson GSON = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX")
            .create();


}
